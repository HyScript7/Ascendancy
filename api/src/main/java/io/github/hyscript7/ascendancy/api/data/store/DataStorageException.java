package io.github.hyscript7.ascendancy.api.data.store;

import io.github.hyscript7.ascendancy.api.data.value.DataCodecException;

/**
 * Thrown when the underlying storage backend fails — unreadable file, permission denied, corrupt
 * data on disk, or whatever a future database backend decides to be unhappy about.
 * <p>
 * Distinct from {@link DataCodecException}: this one means the bytes could not be reached, not that
 * they could not be understood.
 */
public class DataStorageException extends RuntimeException {
    /**
     * @param message A description of the storage failure
     */
    public DataStorageException(String message) {
        super(message);
    }

    /**
     * @param message A description of the storage failure
     * @param cause   The underlying failure
     */
    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
