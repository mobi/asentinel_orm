package com.asentinel.common.text;

/**
 * Exception used when a number is out of range.
 * 
 * @since 1.59.8
 * @author Mihai.Curca
 */
@SuppressWarnings("serial")
public class OutOfBoundsException extends Exception {
	

	public OutOfBoundsException() {
	}

	public OutOfBoundsException(String s) {
		super(s);
	}
	
	public OutOfBoundsException(Throwable cause) {
		super(cause);
	}
	
	public OutOfBoundsException(String s, Throwable cause) {
		super(s, cause);
	}
	
}
