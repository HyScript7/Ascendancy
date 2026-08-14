package io.github.hyscript7.ascendancy.api.data;

/**
 * An immutable, backend-agnostic value that can be persisted.
 * <p>
 * This is deliberately <em>not</em> Gson's {@code JsonElement}. Content packs compile against
 * {@code :api} alone, so anything exposed here is frozen the moment a third party depends on it —
 * and binding the public contract to JSON would make a future SQLite or NBT backend impossible
 * without breaking every pack in existence.
 * <p>
 * Mojang's {@code Codec} would have been the obvious alternative. It is not available:
 * {@code com.mojang:datafixerupper} is not on the {@code paper-api} compile classpath.
 * <p>
 * Every implementation is immutable, which is what makes it safe to snapshot an entity on the main
 * thread and serialize it on another one.
 */
public sealed interface DataValue
        permits DataMap, DataList, DataString, DataInteger, DataDecimal, DataBoolean, DataBytes {

    /**
     * Wraps a string.
     *
     * @param value The string to wrap, must not be null
     * @throws IllegalArgumentException If the value is null
     * @return A {@link DataString} holding the value
     */
    static DataString of(String value) {
        return new DataString(value);
    }

    /**
     * Wraps a whole number. Note that {@code of(1)} resolves here rather than to
     * {@link #of(double)}, since widening {@code int} to {@code long} is preferred.
     *
     * @param value The number to wrap
     * @return A {@link DataInteger} holding the value
     */
    static DataInteger of(long value) {
        return new DataInteger(value);
    }

    /**
     * Wraps a fractional number.
     *
     * @param value The number to wrap
     * @return A {@link DataDecimal} holding the value
     */
    static DataDecimal of(double value) {
        return new DataDecimal(value);
    }

    /**
     * Wraps a boolean.
     *
     * @param value The boolean to wrap
     * @return A cached {@link DataBoolean} holding the value
     */
    static DataBoolean of(boolean value) {
        return DataBoolean.of(value);
    }

    /**
     * Wraps a byte array. The array is defensively copied.
     *
     * @param value The bytes to wrap, must not be null
     * @throws IllegalArgumentException If the value is null
     * @return A {@link DataBytes} holding a copy of the value
     */
    static DataBytes of(byte[] value) {
        return new DataBytes(value);
    }
}
