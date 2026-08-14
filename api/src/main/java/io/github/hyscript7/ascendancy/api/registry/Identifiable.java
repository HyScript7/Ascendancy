package io.github.hyscript7.ascendancy.api.registry;

/**
 * Something that knows its own {@link Identifier}, and can therefore be put in a {@link Registry}.
 * <p>
 * The identifier is the object's identity as far as a registry is concerned, so it must not change
 * once the object has been registered — a registry indexes by it and will not notice it moving.
 */
public interface Identifiable {
    /**
     * Must return the identifier for the object implementing this method.
     * <p>
     * Expected to be stable: the same instance should return an equal identifier every time.
     *
     * @return The identifier naming this object, never null
     */
    Identifier getIdentifier();
}
