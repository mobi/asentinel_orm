package com.asentinel.common.jdbc.exceptions.resolve;

import static com.asentinel.common.util.Assert.*;

import java.util.Arrays;
import java.util.Objects;

/**
 * Wrapper class for a message code and its parameters, similar to Spring's
 * {@code MessageSourceResolvable}. We don't use this just to save a dependency.
 * 
 * @since 1.60.12
 * @author Razvan Popian
 */
public class MessageCodeWrapper {

	private final String code;
	private final Object[] arguments;
	private final String defaultMessage;

	public MessageCodeWrapper(String code, Object ... arguments) {
		this(code, arguments, "");
	}

	public MessageCodeWrapper(String code, Object[] arguments, String defaultMessage) {
		assertNotEmpty(code, "code");
		this.code = code;
		this.arguments = arguments;
		this.defaultMessage = defaultMessage;
	}

	
	public String getCode() {
		return code;
	}

	public Object[] getArguments() {
		return arguments;
	}


	public String getDefaultMessage() {
		return defaultMessage;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(arguments);
		result = prime * result + Objects.hash(code, defaultMessage);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageCodeWrapper other = (MessageCodeWrapper) obj;
		return Arrays.deepEquals(arguments, other.arguments) && Objects.equals(code, other.code)
				&& Objects.equals(defaultMessage, other.defaultMessage);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [code=" + code + ", arguments=" + Arrays.toString(arguments) + "]";
	}
}
