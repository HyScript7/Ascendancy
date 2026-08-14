package io.github.hyscript7.ascendancy;

import io.github.hyscript7.ascendancy.api.registry.Identifiable;
import io.github.hyscript7.ascendancy.api.registry.Identifier;
import io.github.hyscript7.ascendancy.api.registry.Registry;
import io.github.hyscript7.ascendancy.api.registry.RegistryIdentifierCollisionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

/**
 * A base registry class for a generic E type. All E types must implement
 * Identifiable from the Ascendancy API.
 */
@Slf4j
public class BaseRegistry<E extends Identifiable> implements Registry<E> {
    private final ConcurrentMap<Identifier, E> entries;

    public BaseRegistry() {
        this.entries = new ConcurrentHashMap<>();
    }

    /**
     * Registers a new identifiable value with this registry.
     * Fails if the identifier returned by the value is already registered.
     *
     * @throws RegistryIdentifierCollisionException If the identifier is already
     *                                              associated with another value in
     *                                              this registry
     */
    @Override
    public void register(E value) throws RegistryIdentifierCollisionException {
        if (entries.get(value.getIdentifier()) == null) {
            entries.put(value.getIdentifier(), value);
        } else {
            log.warn(
                    "Duplicate identifier registration has been attempted with the identifier {}",
                    value.getIdentifier());
            throw new RegistryIdentifierCollisionException(
                    "An entry using this identifier already exists: " + value.getIdentifier());
        }
    }

    /**
     * Returns a copy of all values registered in this registry.
     *
     * @return A shallow copy list of all values in the registry
     */
    @Override
    public List<E> getAll() {
        return new ArrayList<>(entries.values());
    }

    /**
     * Attempts to retrieve a value by its identifier from the registry. Returns an
     * empty optional if not found.
     *
     * @return An optional containing the value if present, otherwise an empty
     *         optional.
     */
    @Override
    public Optional<E> get(Identifier id) {
        Optional<E> entry = Optional.ofNullable(entries.get(id));
        if (entry.isEmpty()) {
            log.warn("Entity of an id \"{}\" does not exist.", id);
        }
        return (entry);
    }
}
