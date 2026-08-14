package io.github.hyscript7.ascendancy.api.registry;

/**
 * Thrown when an object by the same identifier already exists in the registry.
 * <p>
 * Loud on purpose. The alternative — letting the second registration win — means whichever plugin
 * loaded last silently owns the identifier, and the symptom shows up much later as the other one's
 * data going missing.
 */
public class RegistryIdentifierCollisionException extends RuntimeException {
    /**
     * @param message A description of which identifier collided
     */
    public RegistryIdentifierCollisionException(String message) {
        super(message);
    }
}
