package io.github.hyscript7.ascendancy.api.data.component;

import io.github.hyscript7.ascendancy.api.data.Persistence;

/**
 * Thrown when data is written using a {@link ComponentType} that was never registered in
 * {@link Persistence#componentTypes()}.
 * <p>
 * Registration is what makes identifier collisions between packs detectable. Allowing an
 * unregistered type to write anyway would mean two packs could quietly share one identifier and
 * overwrite each other's data — the exact failure the registry exists to prevent — so the write is
 * refused instead.
 */
public class ComponentTypeNotRegisteredException extends RuntimeException {
    /**
     * @param message A description of which component was not registered
     */
    public ComponentTypeNotRegisteredException(String message) {
        super(message);
    }
}
