package com.asentinel.common.text;

import java.text.ParseException;

@SuppressWarnings("serial")
public class ConversionException extends Exception {
	
	/** default constructor */
	public ConversionException() {
	}

	/** constructor */
	public ConversionException(String s) {
		super(s);
	}
	
	/** constructor */
	public ConversionException(Throwable cause) {
		super(cause);
	}
	
	/** constructor */
	public ConversionException(String s, Throwable cause) {
		super(s, cause);
	}
	
	/**
	 * @return {@code true} only when the exception occurred because the number was invalid, 
	 * the Throwable cause being {@link NumberFormatException} or {@link ParseException}.  
	 * Otherwise {@code false}.
	 */
	public boolean isParseError() {
		return getCause() instanceof NumberFormatException 
			|| getCause() instanceof ParseException;
	}

	/**
	 * @return {@code true} only when the exception occurred because the number was out of bounds, 
	 * the Throwable cause being {@link OutOfBoundsException}.  
	 * Otherwise {@code false}.
	 */
	public boolean isOutOfBoundsError() {
		return getCause() instanceof OutOfBoundsException;
	}
	
}
