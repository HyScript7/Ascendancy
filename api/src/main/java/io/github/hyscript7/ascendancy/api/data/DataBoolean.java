package io.github.hyscript7.ascendancy.api.data;

/**
 * A {@link DataValue} holding a boolean.
 *
 * @param value The wrapped boolean
 */
public record DataBoolean(boolean value) implements DataValue {
    /** The {@code true} instance. */
    public static final DataBoolean TRUE = new DataBoolean(true);

    /** The {@code false} instance. */
    public static final DataBoolean FALSE = new DataBoolean(false);

    /**
     * Returns a cached instance rather than allocating a new one.
     *
     * @param value The boolean to wrap
     * @return {@link #TRUE} or {@link #FALSE}
     */
    public static DataBoolean of(boolean value) {
        return value ? TRUE : FALSE;
    }
}
