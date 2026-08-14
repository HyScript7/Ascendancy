package io.github.hyscript7.ascendancy.api;

/**
 * Thrown when something tries to register a second {@link AscendancyAPI} implementation while one is
 * already registered with the services manager.
 * <p>
 * In practice this means two Cores on one server, which is not a situation worth guessing about — the
 * second one is refused rather than silently replacing the first and stranding everything already
 * holding a reference to it.
 */
public class AscendancyAPIAlreadyInitializedException extends RuntimeException {
    /**
     * @param message A description of what was already registered
     */
    public AscendancyAPIAlreadyInitializedException(String message) {
        super(message);
    }
}
