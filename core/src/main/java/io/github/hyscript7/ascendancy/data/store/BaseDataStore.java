package io.github.hyscript7.ascendancy.data.store;

import io.github.hyscript7.ascendancy.api.data.component.ComponentHolder;
import io.github.hyscript7.ascendancy.api.data.component.ComponentType;
import io.github.hyscript7.ascendancy.api.data.store.DataKey;
import io.github.hyscript7.ascendancy.api.data.store.DataScope;
import io.github.hyscript7.ascendancy.api.data.store.DataStorageException;
import io.github.hyscript7.ascendancy.api.data.store.DataStore;
import io.github.hyscript7.ascendancy.api.data.store.ResidencyPolicy;
import io.github.hyscript7.ascendancy.api.registry.Identifier;
import io.github.hyscript7.ascendancy.data.backend.StorageBackend;
import io.github.hyscript7.ascendancy.data.backend.StoredComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;

/**
 * The {@link DataStore} implementation, holding resident entities in memory and pushing writes to a
 * {@link StorageBackend} on a background thread.
 * <p>
 * Reads and writes are synchronous so that gameplay code on the main thread can treat entity data
 * as ordinary memory. Only the disk is asynchronous.
 */
@Slf4j
public class BaseDataStore implements DataStore {
    /** Roughly a fifth of a tick — slow enough to matter, generous enough not to cry wolf. */
    public static final long DEFAULT_COLD_READ_WARN_MILLIS = 5L;

    private final StorageBackend backend;

    /** Resident entities. Which of these stay resident is decided by their scope's policy. */
    private final Map<DataKey, BaseComponentHolder> loaded = new ConcurrentHashMap<>();

    /**
     * Scope residency, mirrored out of the registry as scopes are registered.
     * <p>
     * Kept here rather than looked up per call because the registry logs a warning on every failed
     * lookup, and asking it about an unregistered scope on every flush would bury the log.
     */
    private final Map<Identifier, DataScope> scopes = new ConcurrentHashMap<>();

    /**
     * Registered component identifiers, mirrored out of the registry for the same reason as
     * {@link #scopes}. Consulted on every write, which is what makes registration enforceable rather
     * than merely advised.
     */
    private final Set<Identifier> registeredComponents = ConcurrentHashMap.newKeySet();

    /**
     * Single-threaded on purpose: it serializes writes to the same entity without any per-key
     * locking, and persistence is nowhere near hot enough to need more.
     */
    private final ExecutorService ioExecutor;

    /**
     * How long a main-thread cold read may take before it is worth complaining about, in
     * milliseconds. Zero or less disables the warning.
     */
    private final long coldReadWarnMillis;

    /**
     * Builds a store that warns about main-thread cold reads slower than
     * {@value #DEFAULT_COLD_READ_WARN_MILLIS}ms.
     *
     * @param backend Where entity data is stored
     * @throws IllegalArgumentException If the backend is null
     */
    public BaseDataStore(StorageBackend backend) {
        this(backend, DEFAULT_COLD_READ_WARN_MILLIS);
    }

    /**
     * @param backend            Where entity data is stored
     * @param coldReadWarnMillis How slow a main-thread cold read must be to earn a warning; zero or
     *                           less silences it
     * @throws IllegalArgumentException If the backend is null
     */
    public BaseDataStore(StorageBackend backend, long coldReadWarnMillis) {
        if (backend == null) {
            throw new IllegalArgumentException("Storage backend cannot be null");
        }
        this.coldReadWarnMillis = coldReadWarnMillis;
        this.backend = backend;
        this.ioExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "Ascendancy-Persistence");
            // Daemon so a botched shutdown cannot wedge the JVM. The explicit shutdown flush is what
            // actually guarantees the data lands.
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Records a scope's residency policy. Called by the scope registry as packs register scopes.
     *
     * @param scope The newly registered scope
     */
    void onScopeRegistered(DataScope scope) {
        scopes.put(scope.getIdentifier(), scope);
    }

    /**
     * Records that a component type may be written. Called by the component type registry.
     *
     * @param type The newly registered component type
     */
    void onComponentTypeRegistered(ComponentType<?> type) {
        registeredComponents.add(type.getIdentifier());
    }

    /**
     * @param identifier The component identifier to test
     * @return True if a component type with that identifier has been registered
     */
    boolean isComponentRegistered(Identifier identifier) {
        return registeredComponents.contains(identifier);
    }

    /**
     * Loads every entity of every {@link ResidencyPolicy#EAGER} scope.
     * <p>
     * Must run <em>after</em> {@code AscendancyEnabledEvent} has been dispatched, since that is when
     * content packs register their scopes.
     */
    public void loadEagerScopes() {
        for (DataScope scope : List.copyOf(scopes.values())) {
            if (scope.residency() != ResidencyPolicy.EAGER) {
                continue;
            }
            int count = 0;
            for (DataKey key : backend.keys(scope.getIdentifier())) {
                try {
                    loadInto(key);
                    count++;
                } catch (DataStorageException exception) {
                    // One unreadable entity should not stop the rest of the scope from loading.
                    log.error("Failed to load entity {} while populating eager scope {}", key, scope, exception);
                }
            }
            log.info("Loaded {} entities for eager scope {}", count, scope.getIdentifier());
        }
    }

    @Override
    public ComponentHolder get(DataKey key) {
        requireKey(key);
        BaseComponentHolder resident = loaded.get(key);
        if (resident != null) {
            return resident;
        }
        // Not resident, so this call is about to hit the disk on whatever thread asked.
        long startedAt = System.nanoTime();
        BaseComponentHolder holder = loaded.computeIfAbsent(key, this::read);
        reportColdRead(key, System.nanoTime() - startedAt);
        return holder;
    }

    /**
     * Reports a read that had to reach the storage backend.
     * <p>
     * Warns only when such a read blocks the main thread for longer than
     * {@link #coldReadWarnMillis}. Warning on every cold main-thread read would be useless noise:
     * chunk data is deliberately read on demand rather than preloaded, because the overwhelming
     * majority of chunks never carry any Ascendancy data. A threshold keeps the log quiet in normal
     * operation and loud exactly when the read is actually costing tick time.
     *
     * @param key     The entity that was read
     * @param elapsed How long the read took, in nanoseconds
     */
    private void reportColdRead(DataKey key, long elapsed) {
        long millis = elapsed / 1_000_000L;
        if (coldReadWarnMillis > 0 && millis >= coldReadWarnMillis && onMainThread()) {
            log.warn(
                    "Cold read of {} blocked the main thread for {}ms. Consider DataStore#preload(key) ahead of time.",
                    key,
                    millis);
        } else {
            log.debug("Cold read of {} took {}ms", key, millis);
        }
    }

    /**
     * @return True if the caller is on the server's main thread, and false when there is no server
     *         at all — which is how this class stays usable from tooling outside a running Paper
     */
    private static boolean onMainThread() {
        return Bukkit.getServer() != null && Bukkit.isPrimaryThread();
    }

    @Override
    public Optional<ComponentHolder> getIfLoaded(DataKey key) {
        requireKey(key);
        return Optional.ofNullable(loaded.get(key));
    }

    @Override
    public CompletableFuture<ComponentHolder> preload(DataKey key) {
        requireKey(key);
        ComponentHolder resident = loaded.get(key);
        if (resident != null) {
            return CompletableFuture.completedFuture(resident);
        }
        return CompletableFuture.supplyAsync(() -> get(key), ioExecutor);
    }

    @Override
    public boolean exists(DataKey key) {
        requireKey(key);
        return loaded.containsKey(key) || backend.exists(key);
    }

    @Override
    public Collection<DataKey> keys(Identifier scope) {
        if (scope == null) {
            throw new IllegalArgumentException("Scope cannot be null");
        }
        DataScope registered = scopes.get(scope);
        if (registered != null && registered.residency() == ResidencyPolicy.EAGER) {
            // The entire point of the eager policy: enumeration never touches the disk.
            return residentKeysOf(scope);
        }
        // Union, so an entity created this tick but not yet flushed still shows up.
        Collection<DataKey> keys = new LinkedHashSet<>(backend.keys(scope));
        keys.addAll(residentKeysOf(scope));
        return keys;
    }

    @Override
    public boolean delete(DataKey key) {
        requireKey(key);
        loaded.remove(key);
        return backend.delete(key);
    }

    @Override
    public CompletableFuture<Void> flush(DataKey key) {
        requireKey(key);
        BaseComponentHolder holder = loaded.get(key);
        if (holder == null) {
            return CompletableFuture.completedFuture(null);
        }
        return flushHolder(holder);
    }

    @Override
    public CompletableFuture<Void> flushAll() {
        List<CompletableFuture<Void>> pending = new ArrayList<>();
        for (BaseComponentHolder holder : loaded.values()) {
            pending.add(flushHolder(holder));
        }
        return CompletableFuture.allOf(pending.toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletableFuture<Void> unload(DataKey key) {
        requireKey(key);
        BaseComponentHolder holder = loaded.get(key);
        if (holder == null) {
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<Void> flushed = flushHolder(holder);
        DataScope scope = scopes.get(key.scope());
        if (scope != null && scope.residency() == ResidencyPolicy.EAGER) {
            // Evicting would defeat the policy, so an eager entity is only ever flushed.
            return flushed;
        }
        return flushed.whenComplete((ignored, failure) -> loaded.computeIfPresent(
                key,
                // Checking dirtiness and evicting have to happen as one step, under the map's own
                // lock. A write landing between a separate check and remove — a player who rejoined
                // before their quit-save completed, typically — would otherwise be evicted and lost.
                (ignoredKey, existing) -> existing == holder && !existing.isDirty() ? null : existing));
    }

    /**
     * Flushes everything synchronously and shuts the I/O thread down. Called once, during plugin
     * disable, where blocking the main thread is both acceptable and necessary.
     *
     * @param timeoutSeconds How long to wait for pending writes before giving up
     */
    public void shutdown(long timeoutSeconds) {
        try {
            flushAll().get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while flushing entity data during shutdown", exception);
        } catch (Exception exception) {
            log.error("Failed to flush all entity data during shutdown", exception);
        }

        ioExecutor.shutdown();
        try {
            if (!ioExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                log.warn(
                        "Persistence I/O did not finish within {}s; some data may not have been saved", timeoutSeconds);
                ioExecutor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            ioExecutor.shutdownNow();
        }

        backend.close();
        loaded.clear();
    }

    /**
     * Snapshots a dirty entity on the calling thread and writes it on the I/O thread.
     * <p>
     * The dirty flag is cleared <em>before</em> the snapshot is taken, so a write that lands while
     * this flush is in flight leaves the entity dirty and survives to the next flush. The reverse
     * order would swallow it.
     *
     * @param holder The entity to flush
     * @return A future completing once the write lands
     */
    private CompletableFuture<Void> flushHolder(BaseComponentHolder holder) {
        if (!holder.clearDirty()) {
            return CompletableFuture.completedFuture(null);
        }
        DataKey key = holder.getKey();
        Map<Identifier, StoredComponent> snapshot = holder.snapshot();

        return CompletableFuture.runAsync(
                        () -> {
                            if (snapshot.isEmpty()) {
                                // An entity stripped of every component should not leave a husk behind.
                                backend.delete(key);
                            } else {
                                backend.write(key, snapshot);
                            }
                        },
                        ioExecutor)
                .whenComplete((ignored, failure) -> {
                    if (failure != null) {
                        // Put it back in the queue rather than losing the change entirely.
                        holder.markDirty();
                        log.error("Failed to save entity {}; it will be retried on the next flush", key, failure);
                    }
                });
    }

    /**
     * @param key The entity to read
     * @throws DataStorageException If the backend cannot be read
     * @return A holder seeded with whatever was stored, or an empty one for a new entity
     */
    private BaseComponentHolder read(DataKey key) {
        Map<Identifier, StoredComponent> stored = backend.read(key).orElseGet(Map::of);
        return new BaseComponentHolder(key, stored, this::isComponentRegistered);
    }

    /**
     * Loads an entity into residency without going through {@link #get(DataKey)}, for eager startup.
     *
     * @param key The entity to load
     * @throws DataStorageException If the backend cannot be read
     */
    private void loadInto(DataKey key) {
        loaded.computeIfAbsent(key, this::read);
    }

    private Collection<DataKey> residentKeysOf(Identifier scope) {
        return loaded.keySet().stream().filter(key -> key.scope().equals(scope)).toList();
    }

    private static void requireKey(DataKey key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
    }
}
