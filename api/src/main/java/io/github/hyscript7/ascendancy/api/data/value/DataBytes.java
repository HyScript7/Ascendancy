package io.github.hyscript7.ascendancy.api.data.value;

import java.util.Arrays;

/**
 * A {@link DataValue} holding raw bytes, for data that already has its own serialized form —
 * Bukkit's {@code ItemStack#serializeAsBytes()} being the obvious customer.
 * <p>
 * The array is defensively copied on the way in and on the way out, so this stays as immutable as
 * every other {@link DataValue} despite wrapping a mutable array.
 *
 * @param value The wrapped bytes, never null
 */
public record DataBytes(byte[] value) implements DataValue {
    /**
     * @throws IllegalArgumentException If the value is null
     */
    public DataBytes {
        if (value == null) {
            throw new IllegalArgumentException("DataBytes value cannot be null");
        }
        value = value.clone();
    }

    /**
     * Returns a copy of the wrapped bytes. Mutating it does not affect this value.
     *
     * @return A fresh copy of the byte array
     */
    @Override
    public byte[] value() {
        return value.clone();
    }

    /**
     * Compares by array content rather than by array identity, which is what the generated record
     * implementation would have done.
     *
     * @param other The object to compare against
     * @return True if the other object is a {@link DataBytes} with equal content
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof DataBytes bytes && Arrays.equals(value, bytes.value);
    }

    /**
     * @return A content-based hash code, consistent with {@link #equals(Object)}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    /**
     * @return A description of the array's length, since dumping the content helps nobody
     */
    @Override
    public String toString() {
        return "DataBytes[" + value.length + " bytes]";
    }
}
