package io.github.hyscript7.ascendancy.api.data.store;

import io.github.hyscript7.ascendancy.api.registry.Identifiable;
import io.github.hyscript7.ascendancy.api.registry.Identifier;
import java.util.UUID;

/**
 * A family of entities sharing a lifetime and a key format.
 * <p>
 * Scopes are registered so that the store knows which ones to load eagerly at startup, and so that
 * two packs cannot claim the same scope identifier.
 *
 * @param identifier Names the scope, e.g. {@code ascendancy:player}. Doubles as the on-disk
 *                   directory path, which the {@code namespace:path} shape happens to suit nicely.
 * @param residency  When entities in this scope are held in memory
 */
public record DataScope(Identifier identifier, ResidencyPolicy residency) implements Identifiable {
    /**
     * @throws IllegalArgumentException If the identifier or residency is null
     */
    public DataScope {
        if (identifier == null) {
            throw new IllegalArgumentException("Scope identifier cannot be null");
        }
        if (residency == null) {
            throw new IllegalArgumentException("Scope residency cannot be null");
        }
    }

    /**
     * Creates a scope whose entities load on demand.
     *
     * @param identifier Names the scope
     * @throws IllegalArgumentException If the identifier is null
     * @return The scope
     */
    public static DataScope lazy(Identifier identifier) {
        return new DataScope(identifier, ResidencyPolicy.LAZY);
    }

    /**
     * Creates a scope whose entities are all loaded at startup and kept resident.
     *
     * @param identifier Names the scope
     * @throws IllegalArgumentException If the identifier is null
     * @return The scope
     */
    public static DataScope eager(Identifier identifier) {
        return new DataScope(identifier, ResidencyPolicy.EAGER);
    }

    /**
     * @return The identifier naming this scope
     */
    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * Builds a key addressing an entity within this scope.
     *
     * @param id The entity id
     * @throws IllegalArgumentException If validation of the id fails
     * @return The key
     */
    public DataKey key(String id) {
        return DataKey.of(identifier, id);
    }

    /**
     * Builds a key addressing an entity within this scope.
     *
     * @param id The entity UUID
     * @throws IllegalArgumentException If the id is null
     * @return The key
     */
    public DataKey key(UUID id) {
        return DataKey.of(identifier, id);
    }
}
