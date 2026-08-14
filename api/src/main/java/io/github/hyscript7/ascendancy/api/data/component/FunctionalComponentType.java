package io.github.hyscript7.ascendancy.api.data.component;

import io.github.hyscript7.ascendancy.api.data.value.DataCodecException;
import io.github.hyscript7.ascendancy.api.data.value.DataValue;
import io.github.hyscript7.ascendancy.api.registry.Identifier;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A {@link ComponentType} assembled from functions, backing {@link ComponentType.Builder}.
 * <p>
 * Package-private on purpose: packs depend on the {@link ComponentType} contract, not on this shape.
 * <p>
 * The supplier component is named {@code defaultValueSupplier} rather than {@code defaultValue}
 * because a record component's accessor must return the component's own type, and the interface
 * needs {@code defaultValue()} to hand back a {@code T}.
 *
 * @param <T>                  The value type
 * @param identifier           Names the component
 * @param valueType            The class of value carried
 * @param defaultValueSupplier Supplies the value used when the component is absent
 * @param encoder              Converts a value into its persistable form
 * @param decoder              Reconstructs a value from its persistable form
 * @param storedVersion        The current stored shape
 * @param componentMigrator    Brings older data forward, or null if there is none
 */
record FunctionalComponentType<T>(
        Identifier identifier,
        Class<T> valueType,
        Supplier<T> defaultValueSupplier,
        Function<T, DataValue> encoder,
        Function<DataValue, T> decoder,
        int storedVersion,
        ComponentMigrator componentMigrator)
        implements ComponentType<T> {

    /**
     * @throws IllegalArgumentException If any required argument is null
     */
    FunctionalComponentType {
        if (identifier == null) {
            throw new IllegalArgumentException("Component identifier cannot be null");
        }
        if (valueType == null) {
            throw new IllegalArgumentException("Component value type cannot be null");
        }
        if (defaultValueSupplier == null) {
            throw new IllegalArgumentException("Component default value supplier cannot be null");
        }
        if (encoder == null || decoder == null) {
            throw new IllegalArgumentException("Component encoder and decoder cannot be null");
        }
        if (storedVersion < 1) {
            throw new IllegalArgumentException("Component version must be at least 1, got " + storedVersion);
        }
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public int version() {
        return storedVersion;
    }

    @Override
    public ComponentMigrator migrator() {
        return componentMigrator;
    }

    @Override
    public T defaultValue() throws DataCodecException {
        T value = defaultValueSupplier.get();
        if (value == null) {
            throw new DataCodecException("Default value supplier for component " + identifier + " returned null");
        }
        return value;
    }

    @Override
    public DataValue encode(T value) throws DataCodecException {
        if (value == null) {
            throw new DataCodecException("Cannot encode a null value for component " + identifier);
        }
        DataValue encoded = encoder.apply(value);
        if (encoded == null) {
            throw new DataCodecException("Encoder for component " + identifier + " returned null");
        }
        return encoded;
    }

    @Override
    public T decode(DataValue data) throws DataCodecException {
        if (data == null) {
            throw new DataCodecException("Cannot decode a null value for component " + identifier);
        }
        T decoded = decoder.apply(data);
        if (decoded == null) {
            throw new DataCodecException("Decoder for component " + identifier + " returned null");
        }
        return decoded;
    }

    /**
     * @return The component's identifier and version, since that is all worth reading in a log line
     */
    @Override
    public String toString() {
        return "ComponentType[" + identifier + " v" + storedVersion + "]";
    }
}
