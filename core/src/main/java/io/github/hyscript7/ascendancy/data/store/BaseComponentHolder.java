package io.github.hyscript7.ascendancy.data.store;

import io.github.hyscript7.ascendancy.api.data.component.ComponentHolder;
import io.github.hyscript7.ascendancy.api.data.component.ComponentMigrator;
import io.github.hyscript7.ascendancy.api.data.component.ComponentType;
import io.github.hyscript7.ascendancy.api.data.component.ComponentTypeNotRegisteredException;
import io.github.hyscript7.ascendancy.api.data.store.DataKey;
import io.github.hyscript7.ascendancy.api.data.value.DataCodecException;
import io.github.hyscript7.ascendancy.api.data.value.DataValue;
import io.github.hyscript7.ascendancy.api.registry.Identifier;
import io.github.hyscript7.ascendancy.data.backend.StoredComponent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * The in-memory form of one entity.
 * <p>
 * Components are held as raw {@link DataValue} and decoded lazily, only when somebody asks for one
 * by {@link ComponentType}. This is the mechanism that lets data from an uninstalled content pack
 * survive: an unrecognised identifier is never looked up, never fails to decode, and gets written
 * back verbatim, because nothing ever touches it.
 * <p>
 * Encoding happens eagerly on {@link #set}, so the raw form is always authoritative. That keeps
 * {@link #snapshot()} a plain copy and means a background flush never has to run content pack code.
 */
public class BaseComponentHolder implements ComponentHolder {
    private final DataKey key;

    /** The authoritative stored form of every component, including unrecognised ones. */
    private final Map<Identifier, StoredComponent> raw = new ConcurrentHashMap<>();

    /** Decoded values, to avoid paying for a decode on every read. */
    private final Map<Identifier, Object> decoded = new ConcurrentHashMap<>();

    private final AtomicBoolean dirty = new AtomicBoolean(false);

    /**
     * Tests whether a component identifier has been registered. Only writes are gated on it — reads
     * of an unregistered type are harmless, and gating them would break the very round-trip that
     * keeps an uninstalled pack's data alive.
     */
    private final Predicate<Identifier> registrationCheck;

    /**
     * @param key               The entity this holder represents
     * @param components        The stored components to seed it with, may be empty
     * @param registrationCheck Tests whether a component identifier has been registered
     * @throws IllegalArgumentException If any argument is null
     */
    public BaseComponentHolder(
            DataKey key, Map<Identifier, StoredComponent> components, Predicate<Identifier> registrationCheck) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (components == null) {
            throw new IllegalArgumentException("Components cannot be null");
        }
        if (registrationCheck == null) {
            throw new IllegalArgumentException("Registration check cannot be null");
        }
        this.key = key;
        this.registrationCheck = registrationCheck;
        this.raw.putAll(components);
    }

    @Override
    public DataKey getKey() {
        return key;
    }

    @Override
    public <T> T get(ComponentType<T> type) {
        return find(type).orElseGet(type::defaultValue);
    }

    @Override
    public <T> Optional<T> find(ComponentType<T> type) {
        requireType(type);
        Identifier identifier = type.getIdentifier();

        Object cached = decoded.get(identifier);
        if (cached != null) {
            return Optional.of(type.valueType().cast(cached));
        }

        StoredComponent stored = raw.get(identifier);
        if (stored == null) {
            return Optional.empty();
        }

        T value = type.decode(bringForward(type, stored).data());
        decoded.put(identifier, value);
        return Optional.of(value);
    }

    /**
     * Brings a component's stored data up to the type's current version, if it is behind.
     * <p>
     * A successful migration is written back into {@link #raw} and marks the entity dirty, so the
     * upgrade is paid for once rather than on every read. That does mean a pure read can schedule a
     * write — which is the intended behaviour, since the alternative is migrating the same data
     * forever.
     *
     * @param <T>    The component's value type
     * @param type   The component being read
     * @param stored The data as it sits in storage
     * @throws DataCodecException If the data is newer than the running code, or older with no
     *                            migration path, or the migration itself fails
     * @return The data at the type's current version
     */
    private <T> StoredComponent bringForward(ComponentType<T> type, StoredComponent stored) {
        int current = type.version();
        if (stored.version() == current) {
            return stored;
        }
        Identifier identifier = type.getIdentifier();

        if (stored.version() > current) {
            // Someone downgraded the pack. Guessing at a shape from the future would corrupt it.
            throw new DataCodecException("Component " + identifier + " was stored at version " + stored.version()
                    + " but this build only understands version " + current
                    + "; the content pack appears to have been downgraded");
        }

        ComponentMigrator migrator = type.migrator();
        if (migrator == null) {
            throw new DataCodecException("Component " + identifier + " was stored at version " + stored.version()
                    + " but is now version " + current + ", and declares no migrator to bridge the two");
        }

        DataValue migrated = migrator.migrate(stored.data(), stored.version());
        if (migrated == null) {
            throw new DataCodecException("Migrator for component " + identifier + " returned null");
        }

        StoredComponent upgraded = new StoredComponent(current, migrated);
        raw.put(identifier, upgraded);
        dirty.set(true);
        return upgraded;
    }

    @Override
    public <T> void set(ComponentType<T> type, T value) {
        requireType(type);
        if (value == null) {
            throw new IllegalArgumentException("Component value cannot be null; use remove() instead");
        }
        Identifier identifier = type.getIdentifier();
        if (!registrationCheck.test(identifier)) {
            throw new ComponentTypeNotRegisteredException("Component type " + identifier
                    + " must be registered with Persistence#componentTypes() before it can store data");
        }
        // Encoded now rather than at flush time, so a failure surfaces at the call site that caused
        // it instead of on a background thread ten minutes later.
        raw.put(identifier, new StoredComponent(type.version(), type.encode(value)));
        decoded.put(identifier, value);
        dirty.set(true);
    }

    @Override
    public boolean has(ComponentType<?> type) {
        requireType(type);
        return raw.containsKey(type.getIdentifier());
    }

    @Override
    public boolean remove(ComponentType<?> type) {
        requireType(type);
        Identifier identifier = type.getIdentifier();
        decoded.remove(identifier);
        boolean removed = raw.remove(identifier) != null;
        if (removed) {
            dirty.set(true);
        }
        return removed;
    }

    @Override
    public Set<Identifier> componentIds() {
        return Set.copyOf(raw.keySet());
    }

    @Override
    public boolean isDirty() {
        return dirty.get();
    }

    /**
     * Takes a copy of the stored form for a background thread to serialize.
     * <p>
     * Safe because every {@link DataValue} is immutable — the writer thread cannot observe a
     * half-updated component even if gameplay code mutates the entity mid-flush. Handing over the
     * live map instead would be a data race that only shows up under load.
     *
     * @return An independent copy of every stored component
     */
    Map<Identifier, StoredComponent> snapshot() {
        return new LinkedHashMap<>(raw);
    }

    /**
     * Clears the dirty flag, but only if nothing was written since the given snapshot was taken.
     * <p>
     * Compare-and-set rather than a plain clear: a {@link #set} that lands while a flush is in
     * flight must survive to the next flush, not be swallowed by it.
     *
     * @return True if the flag was cleared
     */
    boolean clearDirty() {
        return dirty.compareAndSet(true, false);
    }

    /**
     * Marks this entity as having unsaved changes, used when a flush fails and the data has to be
     * retried.
     */
    void markDirty() {
        dirty.set(true);
    }

    /**
     * @return True if this entity has no components at all, and so is not worth writing to disk
     */
    boolean isEmpty() {
        return raw.isEmpty();
    }

    private static void requireType(ComponentType<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("Component type cannot be null");
        }
    }
}
