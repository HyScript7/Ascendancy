package io.github.hyscript7.ascendancy.data.store;

import io.github.hyscript7.ascendancy.BaseRegistry;
import io.github.hyscript7.ascendancy.api.data.store.DataScope;
import io.github.hyscript7.ascendancy.api.registry.RegistryIdentifierCollisionException;

/**
 * The scope registry, which additionally tells the store about each scope as it is registered.
 * <p>
 * The store needs a scope's residency policy on paths as hot as a flush. Mirroring the policy across
 * at registration time keeps those paths off the registry, whose {@code get} logs a warning on every
 * miss — useful for gameplay lookups, ruinous for the log if a flush asks about an unregistered
 * scope every few minutes.
 */
public class DataScopeRegistry extends BaseRegistry<DataScope> {
    private final BaseDataStore store;

    /**
     * @param store The store to notify as scopes are registered
     * @throws IllegalArgumentException If the store is null
     */
    public DataScopeRegistry(BaseDataStore store) {
        if (store == null) {
            throw new IllegalArgumentException("Store cannot be null");
        }
        this.store = store;
    }

    /**
     * Registers a scope and mirrors its residency policy into the store.
     *
     * @param value The scope to register
     * @throws RegistryIdentifierCollisionException If the identifier is already registered
     */
    @Override
    public void register(DataScope value) throws RegistryIdentifierCollisionException {
        super.register(value);
        store.onScopeRegistered(value);
    }
}
