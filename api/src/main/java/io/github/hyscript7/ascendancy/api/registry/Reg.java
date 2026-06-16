package io.github.hyscript7.ascendancy.api.registry;

import java.util.List;
import java.util.Optional;

/**
*The Registry which binds a unified key to a generic object
*/
public interface Reg<E extends RegIdentifiable> {
    /**
    *Adds an object to the register
    */
    void register(E value) throws RegIdentifierCollision;
    /**
    *Returns all registered elements
    */
    List<E> getAll();
    /**
    *Returns the element associated with the provided id
    *Implementation detail: Must log a warning when a lookup fails
    */
    Optional<E> get(RegIdentifier id);
}