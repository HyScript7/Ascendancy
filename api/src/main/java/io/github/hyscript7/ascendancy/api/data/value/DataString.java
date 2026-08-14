package io.github.hyscript7.ascendancy.api.data.value;

/**
 * A {@link DataValue} holding a string.
 *
 * @param value The wrapped string, never null
 */
public record DataString(String value) implements DataValue {
    /**
     * @throws IllegalArgumentException If the value is null
     */
    public DataString {
        if (value == null) {
            throw new IllegalArgumentException("DataString value cannot be null");
        }
    }
}
