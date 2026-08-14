package io.github.hyscript7.ascendancy.api.data;

/**
 * A {@link DataValue} holding a whole number.
 * <p>
 * Kept distinct from {@link DataDecimal} on purpose: collapsing both into a single floating-point
 * "number" silently loses precision above 2^53, which is exactly where UUID halves and world seeds
 * live.
 *
 * @param value The wrapped number
 */
public record DataInteger(long value) implements DataValue {
    /**
     * Narrows the value to an {@code int}.
     *
     * @return The value as an {@code int}, truncated if it does not fit
     */
    public int asInt() {
        return (int) value;
    }
}
