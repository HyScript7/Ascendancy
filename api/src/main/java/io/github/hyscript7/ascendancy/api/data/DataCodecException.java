package io.github.hyscript7.ascendancy.api.data;

/**
 * Thrown when a component value cannot be converted to or from its {@link DataValue} form — a
 * missing required field, a field of the wrong type, or a value the component rejects.
 */
public class DataCodecException extends RuntimeException {
    /**
     * @param message A description of what failed to encode or decode
     */
    public DataCodecException(String message) {
        super(message);
    }

    /**
     * @param message A description of what failed to encode or decode
     * @param cause   The underlying failure
     */
    public DataCodecException(String message, Throwable cause) {
        super(message, cause);
    }
}
