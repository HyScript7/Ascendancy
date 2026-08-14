package io.github.hyscript7.ascendancy.data;

import io.github.hyscript7.ascendancy.api.data.Persistence;
import io.github.hyscript7.ascendancy.api.data.component.ComponentType;
import io.github.hyscript7.ascendancy.api.data.store.DataScope;
import io.github.hyscript7.ascendancy.api.data.store.DataScopes;
import io.github.hyscript7.ascendancy.api.data.store.DataStore;
import io.github.hyscript7.ascendancy.api.registry.Registry;
import io.github.hyscript7.ascendancy.data.backend.JsonFileStorageBackend;
import io.github.hyscript7.ascendancy.data.backend.StorageBackend;
import io.github.hyscript7.ascendancy.data.store.BaseDataStore;
import io.github.hyscript7.ascendancy.data.store.ComponentTypeRegistry;
import io.github.hyscript7.ascendancy.data.store.DataScopeRegistry;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

/**
 * The {@link Persistence} implementation, wiring a store, a backend and the two registries together.
 */
@Slf4j
public class BasePersistence implements Persistence {
    private final BaseDataStore store;
    private final ComponentTypeRegistry componentTypes;
    private final DataScopeRegistry scopes;

    /**
     * Builds a persistence layer backed by JSON files under the given directory.
     *
     * @param dataDirectory Where entity data is stored
     * @throws IllegalArgumentException If the directory is null
     */
    public BasePersistence(Path dataDirectory) {
        this(new JsonFileStorageBackend(dataDirectory));
    }

    /**
     * Builds a persistence layer over an explicit backend, which is how a future SQLite or MySQL
     * backend gets swapped in.
     *
     * @param backend Where entity data is stored
     * @throws IllegalArgumentException If the backend is null
     */
    public BasePersistence(StorageBackend backend) {
        this(backend, BaseDataStore.DEFAULT_COLD_READ_WARN_MILLIS);
    }

    /**
     * @param backend            Where entity data is stored
     * @param coldReadWarnMillis How slow a main-thread cold read must be to earn a warning; zero or
     *                           less silences it
     * @throws IllegalArgumentException If the backend is null
     */
    public BasePersistence(StorageBackend backend, long coldReadWarnMillis) {
        this.store = new BaseDataStore(backend, coldReadWarnMillis);
        this.componentTypes = new ComponentTypeRegistry(store);
        this.scopes = new DataScopeRegistry(store);
        registerBuiltInScopes();
    }

    @Override
    public DataStore store() {
        return store;
    }

    @Override
    public Registry<ComponentType<?>> componentTypes() {
        return componentTypes;
    }

    @Override
    public Registry<DataScope> scopes() {
        return scopes;
    }

    /**
     * Registers the scopes Core owns, before any content pack gets a chance to run.
     */
    private void registerBuiltInScopes() {
        for (DataScope scope : DataScopes.all()) {
            scopes.register(scope);
        }
        log.debug("Registered {} built-in data scopes", DataScopes.all().length);
    }

    /**
     * Loads every eager scope. Must be called after {@code AscendancyEnabledEvent}, since that is
     * when content packs register scopes of their own.
     */
    public void loadEagerScopes() {
        store.loadEagerScopes();
    }

    /**
     * Flushes everything and releases the backend. Called once during plugin disable.
     *
     * @param timeoutSeconds How long to wait for pending writes before giving up
     */
    public void shutdown(long timeoutSeconds) {
        store.shutdown(timeoutSeconds);
    }
}
