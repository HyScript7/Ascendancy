package io.github.hyscript7.ascendancy.api.data.component;

import io.github.hyscript7.ascendancy.api.data.Persistence;
import io.github.hyscript7.ascendancy.api.data.store.DataKey;
import io.github.hyscript7.ascendancy.api.data.store.DataStore;
import io.github.hyscript7.ascendancy.api.data.value.DataCodecException;
import io.github.hyscript7.ascendancy.api.registry.Identifier;
import java.util.Optional;
import java.util.Set;

/**
 * One persistable entity's components — the closest thing to an "entity object" this design has, and
 * it is deliberately nothing more than a bag of typed data.
 * <p>
 * Accessors are synchronous and safe to call from the main thread. Nothing here touches the disk;
 * writes are recorded and flushed later by {@link DataStore}.
 * <p>
 * Implementations are thread-safe.
 */
public interface ComponentHolder {
    /**
     * @return The key addressing this entity
     */
    DataKey getKey();

    /**
     * Reads a component, falling back to {@link ComponentType#defaultValue()} if it was never set.
     *
     * @param <T>  The component's value type
     * @param type The component to read
     * @throws IllegalArgumentException If the type is null
     * @throws DataCodecException       If stored data for this component cannot be decoded
     * @return The stored value, or the component's default
     */
    <T> T get(ComponentType<T> type) throws DataCodecException;

    /**
     * Reads a component without falling back to its default, for the rare caller that needs to tell
     * "never set" apart from "set to the default".
     *
     * @param <T>  The component's value type
     * @param type The component to read
     * @throws IllegalArgumentException If the type is null
     * @throws DataCodecException       If stored data for this component cannot be decoded
     * @return The stored value, or empty if the component was never set
     */
    <T> Optional<T> find(ComponentType<T> type) throws DataCodecException;

    /**
     * Attaches or replaces a component and marks the entity dirty.
     * <p>
     * The value is encoded immediately rather than at flush time, which keeps the stored form
     * authoritative and means a later flush never has to run pack code on a background thread.
     *
     * @param <T>   The component's value type
     * @param type  The component to write
     * @param value The value to store, must not be null
     * @throws IllegalArgumentException             If the type or value is null
     * @throws ComponentTypeNotRegisteredException  If the type was never registered in
     *                                              {@link Persistence#componentTypes()}
     * @throws DataCodecException                   If the value cannot be encoded
     */
    <T> void set(ComponentType<T> type, T value) throws DataCodecException, ComponentTypeNotRegisteredException;

    /**
     * @param type The component to test for
     * @throws IllegalArgumentException If the type is null
     * @return True if this entity has data stored for the component
     */
    boolean has(ComponentType<?> type);

    /**
     * Detaches a component and marks the entity dirty.
     *
     * @param type The component to remove
     * @throws IllegalArgumentException If the type is null
     * @return True if the component was present
     */
    boolean remove(ComponentType<?> type);

    /**
     * Lists every component identifier stored on this entity, <strong>including those belonging to
     * packs that are not currently loaded</strong>.
     * <p>
     * Data for unrecognised identifiers is retained verbatim and written back untouched, so removing
     * a pack for one restart does not destroy its data.
     *
     * @return An immutable snapshot of the stored component identifiers
     */
    Set<Identifier> componentIds();

    /**
     * @return True if this entity has unsaved changes
     */
    boolean isDirty();
}
