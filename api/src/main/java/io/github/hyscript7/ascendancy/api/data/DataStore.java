package io.github.hyscript7.ascendancy.api.data;

import io.github.hyscript7.ascendancy.api.registry.Identifier;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Stores and retrieves entities by {@link DataKey}.
 * <p>
 * Reads and writes are synchronous and main-thread-safe. Persistence is not: changes are recorded in
 * memory and flushed to the backend asynchronously, on an interval and at shutdown.
 * <p>
 * Implementations are thread-safe.
 */
public interface DataStore {
    /**
     * Retrieves an entity, loading it if it is not resident and creating it if it does not exist.
     * <p>
     * A brand new entity is not written to storage until it has a component set on it, so calling
     * this to check on something harmless does not litter the disk with empty files.
     *
     * @param key The entity to retrieve
     * @throws IllegalArgumentException If the key is null
     * @throws DataStorageException     If the entity is not resident and loading it fails
     * @return The entity's components
     */
    ComponentHolder get(DataKey key);

    /**
     * Retrieves an entity only if it is already in memory, never touching the disk.
     *
     * @param key The entity to retrieve
     * @throws IllegalArgumentException If the key is null
     * @return The entity's components, or empty if it is not resident
     */
    Optional<ComponentHolder> getIfLoaded(DataKey key);

    /**
     * Loads an entity into memory ahead of time, so that a later {@link #get(DataKey)} on the main
     * thread is a map lookup rather than a disk read.
     *
     * @param key The entity to load
     * @throws IllegalArgumentException If the key is null
     * @return A future completing with the loaded entity, or completing exceptionally with a
     *         {@link DataStorageException}
     */
    CompletableFuture<ComponentHolder> preload(DataKey key);

    /**
     * Tests whether an entity has stored data, without loading it.
     *
     * @param key The entity to test for
     * @throws IllegalArgumentException If the key is null
     * @throws DataStorageException     If the backend cannot be queried
     * @return True if the entity is resident or present in storage
     */
    boolean exists(DataKey key);

    /**
     * Lists every entity in a scope.
     * <p>
     * For an {@link ResidencyPolicy#EAGER} scope this reads from memory, which is the whole reason
     * that policy exists. For a {@link ResidencyPolicy#LAZY} scope it queries the backend and may be
     * expensive — avoid it on the main thread for large scopes.
     *
     * @param scope The scope to enumerate
     * @throws IllegalArgumentException If the scope is null
     * @throws DataStorageException     If the backend cannot be enumerated
     * @return The keys of every entity in the scope
     */
    Collection<DataKey> keys(Identifier scope);

    /**
     * Permanently deletes an entity, evicting it from memory and removing it from storage.
     *
     * @param key The entity to delete
     * @throws IllegalArgumentException If the key is null
     * @throws DataStorageException     If the backend cannot be written to
     * @return True if the entity existed
     */
    boolean delete(DataKey key);

    /**
     * Writes an entity's pending changes to storage.
     *
     * @param key The entity to flush
     * @throws IllegalArgumentException If the key is null
     * @return A future completing once the write lands, or completing exceptionally with a
     *         {@link DataStorageException}
     */
    CompletableFuture<Void> flush(DataKey key);

    /**
     * Writes every dirty entity to storage.
     *
     * @return A future completing once all writes land, or completing exceptionally with a
     *         {@link DataStorageException}
     */
    CompletableFuture<Void> flushAll();

    /**
     * Flushes an entity and evicts it from memory, for whoever owns its lifecycle to call — on quit
     * for a player, on unload for a chunk.
     * <p>
     * Entities in an {@link ResidencyPolicy#EAGER} scope are flushed but not evicted, since eviction
     * would defeat the policy.
     *
     * @param key The entity to unload
     * @throws IllegalArgumentException If the key is null
     * @return A future completing once the write lands, or completing exceptionally with a
     *         {@link DataStorageException}
     */
    CompletableFuture<Void> unload(DataKey key);
}
