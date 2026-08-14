package io.github.hyscript7.ascendancy.data.backend;

import io.github.hyscript7.ascendancy.api.data.component.ComponentType;
import io.github.hyscript7.ascendancy.api.data.value.DataValue;

/**
 * One component as it sits in storage: its data, plus the {@link ComponentType#version()} that wrote
 * it.
 * <p>
 * Deliberately a {@code :core} type. The version is bookkeeping between the store and the backend —
 * content packs express versioning through {@link ComponentType#version()} and
 * {@link ComponentType#migrator()} and never handle this directly, so it stays out of {@code :api}.
 * <p>
 * Crucially, a component belonging to an uninstalled pack keeps its version here untouched, so
 * removing a pack for one restart cannot silently downgrade its data on the next save.
 *
 * @param version The component version that produced this data, at least 1
 * @param data    The stored data
 */
public record StoredComponent(int version, DataValue data) {
    /**
     * @throws IllegalArgumentException If the version is below 1 or the data is null
     */
    public StoredComponent {
        if (version < 1) {
            throw new IllegalArgumentException("Stored component version must be at least 1, got " + version);
        }
        if (data == null) {
            throw new IllegalArgumentException("Stored component data cannot be null");
        }
    }

    /**
     * @param data The stored data
     * @throws IllegalArgumentException If the data is null
     * @return The data at version 1, for components that never declared one
     */
    public static StoredComponent unversioned(DataValue data) {
        return new StoredComponent(1, data);
    }

    /**
     * @param data The stored data
     * @throws IllegalArgumentException If the data is null
     * @return A copy of this component carrying the given data at the same version
     */
    public StoredComponent withData(DataValue data) {
        return new StoredComponent(version, data);
    }
}
