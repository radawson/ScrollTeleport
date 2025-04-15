package org.clockworx.scrollteleportation.exceptions;

/**
 * Exception thrown when a scroll configuration is invalid.
 */
public class ScrollInvalidException extends ScrollException {

    /**
     * Creates a new ScrollInvalidException with the specified message.
     *
     * @param message The error message
     */
    public ScrollInvalidException(String message) {
        super(message);
    }

    /**
     * Creates a new ScrollInvalidException with the specified message and cause.
     *
     * @param message The error message
     * @param cause The cause of the exception
     */
    public ScrollInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
} 