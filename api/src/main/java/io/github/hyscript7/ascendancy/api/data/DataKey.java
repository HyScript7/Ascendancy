package io.github.hyscript7.ascendancy.api.data;

import io.github.hyscript7.ascendancy.api.registry.Identifier;
import java.util.UUID;

/**
 * Addresses a single persistable entity: a player, a chunk, a faction, or anything else a feature
 * decides to hang data off.
 * <p>
 * This is a <strong>natural composite key</strong> rather than a surrogate UUID behind a lookup
 * table, and that is load-bearing. Resolving "the entity for this chunk" has to be pure computation
 * on the main thread — an index lookup would make chunk data cost a query per access.
 *
 * @param scope Groups entities sharing a lifetime and key format, e.g. {@code ascendancy:player}
 * @param id    Identifies one entity within that scope
 */
public record DataKey(Identifier scope, String id) {
    /**
     * The characters an {@link #id} may contain.
     * <p>
     * Restricted because the id ends up in a file path. This is also why the built-in chunk scope
     * keys on the <em>world UUID</em> rather than the world name — server owners name worlds things
     * like {@code My World (COPY)}, and we would rather not find out what that does to a filesystem.
     */
    private static final String ALLOWED_ID_CHARACTERS = "[A-Za-z0-9_.-]+";

    /**
     * @throws IllegalArgumentException If the scope is null, the id is null or blank, the id
     *                                  contains characters outside {@code [A-Za-z0-9_.-]}, or the id
     *                                  consists only of dots (which would resolve to a directory)
     */
    public DataKey {
        if (scope == null) {
            throw new IllegalArgumentException("Scope cannot be null");
        }
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Entity id cannot be null or blank");
        }
        if (!id.matches(ALLOWED_ID_CHARACTERS)) {
            throw new IllegalArgumentException(
                    "Entity id \"" + id + "\" may only contain letters, digits, underscores, dots and hyphens");
        }
        if (id.chars().allMatch(character -> character == '.')) {
            throw new IllegalArgumentException("Entity id cannot consist only of dots");
        }
    }

    /**
     * @param scope The owning scope
     * @param id    The entity id
     * @throws IllegalArgumentException If validation of the scope or id fails
     * @return The key
     */
    public static DataKey of(Identifier scope, String id) {
        return new DataKey(scope, id);
    }

    /**
     * Shorthand for the common case of an entity identified by a UUID.
     *
     * @param scope The owning scope
     * @param id    The entity UUID
     * @throws IllegalArgumentException If the scope or id is null
     * @return The key
     */
    public static DataKey of(Identifier scope, UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Entity id cannot be null");
        }
        return new DataKey(scope, id.toString());
    }

    /**
     * @return A readable form, {@code namespace:path/id}
     */
    @Override
    public String toString() {
        return scope + "/" + id;
    }
}
