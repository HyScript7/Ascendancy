package io.github.hyscript7.ascendancy.api.registry;

import java.util.List;
import java.util.Optional;

/**
 * The Registry which binds a unified key to a generic object.
 * <p>
 * Registration is the only way in, and it refuses duplicates — that refusal is the point. Two
 * content packs claiming one {@link Identifier} would otherwise quietly overwrite each other, and
 * the resulting bug looks like data loss rather than a naming collision.
 * <p>
 * Implementations are thread-safe.
 *
 * @param <E> The type held by this registry, which must be able to name itself
 */
public interface Registry<E extends Identifiable> {
    /**
     * Adds an object to the register, keyed by the identifier it reports.
     *
     * @param value The object to register
     * @throws RegistryIdentifierCollisionException If the identifier is already registered
     */
    void register(E value) throws RegistryIdentifierCollisionException;

    /**
     * Returns all registered elements.
     *
     * @return A snapshot of every registered value; mutating it does not affect the registry
     */
    List<E> getAll();

    /**
     * Returns the element associated with the provided id.
     * <p>
     * Implementation detail: Must log a warning when a lookup fails. That makes this unsuitable for
     * "is this present?" checks on a hot path — a miss is treated as a mistake worth reporting, not
     * as an ordinary outcome.
     *
     * @param id The identifier to look up
     * @return The registered value, or empty if nothing is registered under that identifier
     */
    Optional<E> get(Identifier id);
}
