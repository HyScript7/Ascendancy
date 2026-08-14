package io.github.hyscript7.ascendancy.api.data.component;

import io.github.hyscript7.ascendancy.api.data.value.DataCodecException;
import io.github.hyscript7.ascendancy.api.data.value.DataValue;

/**
 * Upgrades a component's stored data from an older shape to the current one.
 * <p>
 * A migrator works entirely in {@link DataValue} space — it never sees the decoded type, because the
 * whole point is that the old data <em>cannot</em> be decoded by the current codec. Rename a field,
 * change a type, split one component into two: all of it happens here, before
 * {@link ComponentType#decode(DataValue)} is ever called.
 */
@FunctionalInterface
public interface ComponentMigrator {
    /**
     * Converts stored data written by an older version into the shape the current codec expects.
     * <p>
     * Implementations should handle every version they might be handed, not only the immediately
     * previous one — a player who has not logged in for a year arrives with whatever was current
     * back then.
     *
     * @param data        The stored data, in the shape written by {@code fromVersion}
     * @param fromVersion The version the data was written at, always lower than the component's
     *                    current {@link ComponentType#version()}
     * @throws DataCodecException If the data cannot be brought forward
     * @return The data in the current version's shape
     */
    DataValue migrate(DataValue data, int fromVersion) throws DataCodecException;
}
