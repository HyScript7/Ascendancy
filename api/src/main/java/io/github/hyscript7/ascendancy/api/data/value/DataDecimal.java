package io.github.hyscript7.ascendancy.api.data.value;

/**
 * A {@link DataValue} holding a fractional number. See {@link DataInteger} for why the two are
 * separate types.
 *
 * @param value The wrapped number
 */
public record DataDecimal(double value) implements DataValue {}
