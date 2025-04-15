package org.clockworx.scrollteleportation.exceptions;

/**
 * Exception thrown when a scroll destination is invalid.
 */
public class DestinationInvalidException extends ScrollException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2612167224780778291L;
	
	/**
	 * Creates a new DestinationInvalidException with the specified message.
	 *
	 * @param message The error message
	 */
	public DestinationInvalidException(String message) {
		super(message);
	}

	/**
	 * Creates a new DestinationInvalidException with the specified message and cause.
	 *
	 * @param message The error message
	 * @param cause The cause of the exception
	 */
	public DestinationInvalidException(String message, Throwable cause) {
		super(message, cause);
	}
}
