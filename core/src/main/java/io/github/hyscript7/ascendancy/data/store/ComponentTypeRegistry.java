package io.github.hyscript7.ascendancy.data.store;

import io.github.hyscript7.ascendancy.BaseRegistry;
import io.github.hyscript7.ascendancy.api.data.component.ComponentType;
import io.github.hyscript7.ascendancy.api.registry.RegistryIdentifierCollisionException;

/**
 * The component type registry, which additionally tells the store about each type as it is
 * registered.
 * <p>
 * Same reasoning as {@link DataScopeRegistry}: the store checks registration on every write, and
 * routing that through {@code BaseRegistry.get()} would log a warning per miss.
 */
public class ComponentTypeRegistry extends BaseRegistry<ComponentType<?>> {
    private final BaseDataStore store;

    /**
     * @param store The store to notify as component types are registered
     * @throws IllegalArgumentException If the store is null
     */
    public ComponentTypeRegistry(BaseDataStore store) {
        if (store == null) {
            throw new IllegalArgumentException("Store cannot be null");
        }
        this.store = store;
    }

    /**
     * Registers a component type and mirrors it into the store.
     *
     * @param value The component type to register
     * @throws RegistryIdentifierCollisionException If the identifier is already registered
     */
    @Override
    public void register(ComponentType<?> value) throws RegistryIdentifierCollisionException {
        super.register(value);
        store.onComponentTypeRegistered(value);
    }
}
