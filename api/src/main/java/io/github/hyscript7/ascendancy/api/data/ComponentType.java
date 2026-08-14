package io.github.hyscript7.ascendancy.api.data;

import io.github.hyscript7.ascendancy.api.registry.Identifiable;
import io.github.hyscript7.ascendancy.api.registry.Identifier;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Describes one kind of data that can be attached to an entity: its identifier, its value class, its
 * default, and how it converts to and from {@link DataValue}.
 * <p>
 * <strong>Component values must be immutable</strong>, ideally records. The store tracks changes at
 * {@link ComponentHolder#set(ComponentType, Object)}, so a value mutated in place behind its back
 * will not be saved.
 * <p>
 * Types are registered in {@link Persistence#componentTypes()}. The registry's job here is
 * <em>collision detection</em> — stopping two packs silently sharing one identifier — and not codec
 * lookup. Decoding never consults it, which is exactly what lets unknown components survive a
 * round-trip untouched.
 *
 * @param <T> The value type carried by this component
 */
public interface ComponentType<T> extends Identifiable {
    /**
     * @return The identifier naming this component
     */
    @Override
    Identifier getIdentifier();

    /**
     * @return The class of value this component carries
     */
    Class<T> valueType();

    /**
     * The value returned for an entity that has never had this component set.
     * <p>
     * Every component has one, so consumers never handle {@link java.util.Optional} for a component
     * they own — which matches how the design documents specify stats, each with a stated default.
     *
     * @return A fresh default value, never null
     */
    T defaultValue();

    /**
     * Converts a value into its persistable form.
     *
     * @param value The value to encode
     * @throws DataCodecException If the value cannot be represented
     * @return The encoded form
     */
    DataValue encode(T value);

    /**
     * Reconstructs a value from its persistable form.
     *
     * @param data The encoded form, as previously produced by {@link #encode(Object)}
     * @throws DataCodecException If the data is malformed or of the wrong shape
     * @return The decoded value
     */
    T decode(DataValue data);

    /**
     * Creates a component type from functions, which spares packs writing a class per component.
     *
     * @param <T>          The value type
     * @param identifier   Names the component
     * @param valueType    The class of value carried
     * @param defaultValue Supplies the value used when the component is absent
     * @param encoder      Converts a value into its persistable form
     * @param decoder      Reconstructs a value from its persistable form
     * @throws IllegalArgumentException If any argument is null
     * @return The component type
     */
    static <T> ComponentType<T> of(
            Identifier identifier,
            Class<T> valueType,
            Supplier<T> defaultValue,
            Function<T, DataValue> encoder,
            Function<DataValue, T> decoder) {
        return new FunctionalComponentType<>(identifier, valueType, defaultValue, encoder, decoder);
    }

    /**
     * Creates a component type whose encoded form is always a {@link DataMap}, which covers nearly
     * every component and spares the decoder a cast.
     *
     * @param <T>          The value type
     * @param identifier   Names the component
     * @param valueType    The class of value carried
     * @param defaultValue Supplies the value used when the component is absent
     * @param encoder      Converts a value into a map
     * @param decoder      Reconstructs a value from a map
     * @throws IllegalArgumentException If any argument is null
     * @return The component type
     */
    static <T> ComponentType<T> ofMap(
            Identifier identifier,
            Class<T> valueType,
            Supplier<T> defaultValue,
            Function<T, DataMap> encoder,
            Function<DataMap, T> decoder) {
        if (encoder == null || decoder == null) {
            throw new IllegalArgumentException("Encoder and decoder cannot be null");
        }
        return new FunctionalComponentType<>(identifier, valueType, defaultValue, encoder::apply, data -> {
            if (!(data instanceof DataMap map)) {
                throw new DataCodecException("Expected a DataMap for component " + identifier + ", but found "
                        + data.getClass().getSimpleName());
            }
            return decoder.apply(map);
        });
    }
}
