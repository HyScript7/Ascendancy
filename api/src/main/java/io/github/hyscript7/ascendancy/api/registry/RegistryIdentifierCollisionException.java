package io.github.hyscript7.ascendancy.api.registry;

/**
 * Thrown when an object by the same identifier already exists in the registry
 */
public class RegistryIdentifierCollisionException extends RuntimeException {
    public RegistryIdentifierCollisionException(String message) {
        super(message);
    }
}