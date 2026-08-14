package io.github.hyscript7.ascendancy.data;

import io.github.hyscript7.ascendancy.api.data.DataKey;
import io.github.hyscript7.ascendancy.api.data.DataStorageException;
import io.github.hyscript7.ascendancy.api.data.DataValue;
import io.github.hyscript7.ascendancy.api.registry.Identifier;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Where entity data physically lives.
 * <p>
 * Lives in {@code :core}, not {@code :api}: content packs have no business choosing a storage
 * engine, and keeping the SPI internal means a future SQLite or MySQL backend can be added without
 * touching anything packs compile against.
 * <p>
 * Backends deal exclusively in raw {@link DataValue} keyed by component {@link Identifier}. They
 * never decode component values, which is what allows data from an uninstalled pack to survive a
 * round-trip.
 * <p>
 * Implementations must be safe to call from a background thread.
 */
public interface StorageBackend {
    /**
     * Reads an entity's stored components.
     *
     * @param key The entity to read
     * @throws DataStorageException If the entity exists but cannot be read or parsed
     * @return The stored components, or empty if the entity has never been stored
     */
    Optional<Map<Identifier, DataValue>> read(DataKey key);

    /**
     * Writes an entity's components, replacing whatever was there.
     *
     * @param key        The entity to write
     * @param components The complete component set to store
     * @throws DataStorageException If the write fails
     */
    void write(DataKey key, Map<Identifier, DataValue> components);

    /**
     * Removes an entity from storage.
     *
     * @param key The entity to delete
     * @throws DataStorageException If the deletion fails
     * @return True if the entity existed
     */
    boolean delete(DataKey key);

    /**
     * Tests whether an entity has stored data.
     *
     * @param key The entity to test for
     * @throws DataStorageException If storage cannot be queried
     * @return True if the entity is present in storage
     */
    boolean exists(DataKey key);

    /**
     * Lists every stored entity in a scope.
     *
     * @param scope The scope to enumerate
     * @throws DataStorageException If storage cannot be enumerated
     * @return The keys of every stored entity in the scope
     */
    Collection<DataKey> keys(Identifier scope);

    /**
     * Releases any resources the backend holds. Called once during shutdown, after the final flush.
     *
     * @throws DataStorageException If shutting down fails
     */
    void close();
}
