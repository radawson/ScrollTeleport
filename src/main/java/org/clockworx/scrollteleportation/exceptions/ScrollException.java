package org.clockworx.scrollteleportation.exceptions;

/**
 * Base exception class for all scroll-related exceptions.
 */
public class ScrollException extends Exception {

    /**
     * Creates a new ScrollException with the specified message.
     *
     * @param message The error message
     */
    public ScrollException(String message) {
        super(message);
    }

    /**
     * Creates a new ScrollException with the specified message and cause.
     *
     * @param message The error message
     * @param cause The cause of the exception
     */
    public ScrollException(String message, Throwable cause) {
        super(message, cause);
    }
} 