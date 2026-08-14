package io.github.hyscript7.ascendancy.api.data;

import io.github.hyscript7.ascendancy.api.data.component.ComponentHolder;
import io.github.hyscript7.ascendancy.api.data.component.ComponentType;
import io.github.hyscript7.ascendancy.api.data.store.DataScope;
import io.github.hyscript7.ascendancy.api.data.store.DataStore;
import io.github.hyscript7.ascendancy.api.data.store.ResidencyPolicy;
import io.github.hyscript7.ascendancy.api.registry.Registry;

/**
 * The entry point to Ascendancy's persistence layer, reached via
 * {@code AscendancyAPI.get().persistence()}.
 * <p>
 * These three pieces are grouped behind one accessor so that the root API does not accumulate a
 * method per persistence concept, and so later additions have somewhere to live that is not
 * {@code AscendancyAPI}.
 * <p>
 * Register scopes and component types from an
 * {@code AscendancyEnabledEvent} handler. Core loads {@link ResidencyPolicy#EAGER} scopes only
 * <em>after</em> that event has been dispatched, so a scope registered any later than that will not
 * be populated at startup.
 */
public interface Persistence {
    /**
     * @return The store used to read and write entity data
     */
    DataStore store();

    /**
     * Component types must be registered before they can store anything —
     * {@link ComponentHolder#set(ComponentType, Object)} refuses an unregistered type. The registry
     * exists to catch two packs claiming the same {@code Identifier}, which would otherwise silently
     * corrupt each other's data, and that guard only works if registration is unavoidable.
     *
     * @return The registry of known component types
     */
    Registry<ComponentType<?>> componentTypes();

    /**
     * Registering a scope declares its {@link ResidencyPolicy}. Unlike component types this is not
     * enforced: an unregistered scope still works and is treated as {@link ResidencyPolicy#LAZY},
     * which degrades gracefully — enumeration falls back to querying the backend instead of reading
     * memory. Register any scope that needs {@link ResidencyPolicy#EAGER}.
     *
     * @return The registry of known scopes
     */
    Registry<DataScope> scopes();
}
