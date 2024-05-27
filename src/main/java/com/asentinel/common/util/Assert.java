package com.asentinel.common.util;

/**
 * This class contains some static utility methods
 * for making assertions.
 * 
 * @author Razvan Popian
 */
public final class Assert {
	
	private Assert(){
		throw new UnsupportedOperationException();
	}

	/**
	 * Tests the object parameter and throws NullPointerException if it is null. This is intended 
	 * to be used for testing methods arguments.
	 * @throws NullPointerException if the first parameter is null.
	 */
	public final static void assertNotNull(Object o, String name) throws NullPointerException {
		if (o == null){
			throw new NullPointerException("The variable or expression " + name + " can not be null.");
		}
	}
	
	/**
	 * Tests the first CharSequence parameter and throws IllegalArgumentException if
	 * its value is null or if its length is 0. This is intended to be used for testing methods
	 * arguments.
	 * @throws IllegalArgumentException if the first String parameter is null or 0-length.
	 */
	public final static void assertNotEmpty(CharSequence s, String name) throws IllegalArgumentException {
		if (s == null || s.length() == 0){
			throw new IllegalArgumentException("The CharSequence " + name + " can not be empty.");
		}
	}

	/**
	 * Tests the object parameter and throws IllegalStateException if it is null. This method is intended
	 * to be used for member variables.
	 * @throws IllegalStateException if the first parameter is null.
	 */
	public final static void assertMemberNotNull(Object o, String name) throws IllegalStateException {
		if (o == null){
			throw new IllegalStateException("The member " + name + " can not be null.");
		}
	}

	/**
	 * Tests if the expected value is equal to the value. Throws IllegalStateException if they
	 * are not equal.
	 * @throws IllegalStateException if expected != value
	 */
	public final static void assertMemberEquals(long expected, long value, String message) throws IllegalStateException {
		if (expected != value) {
			throw new IllegalStateException(message);
		}
	}

	/**
	 * Throws IllegalArgumentException if the provided <code>condition</code> is false. 
	 * @throws IllegalArgumentException
	 */
	public final static void assertTrue(boolean condition, String message) throws IllegalArgumentException {
		if (!condition) {
			throw new IllegalArgumentException(message);
		}
	}
	
	/**
	 * Throws IllegalArgumentException if the provided <code>condition</code> is true. 
	 * @throws IllegalArgumentException
	 */
	public final static void assertFalse(boolean condition, String message) throws IllegalArgumentException {
		if (condition) {
			throw new IllegalArgumentException(message);
		}
	}
	
	/**
	 * Tests if the expected value is equal to the value. Throws IllegalStateException if they
	 * are not equal.
	 * @throws IllegalStateException if expected != value
	 */
	public final static void assertMemberEquals(boolean expected, boolean value, String message) throws IllegalStateException {
		if (expected != value) {
			throw new IllegalStateException(message);
		}
	}
	
	/**
	 * Tests if the expected value is greater than 0. Throws IllegalArgumentException if it is not.
	 * @throws IllegalArgumentException if value &lte; 0.
	 */
	public final static void assertStrictPositive(long value, String name) throws IllegalArgumentException {
		if (value <= 0) {
			throw new IllegalArgumentException("The parameter " + name + " must be greater than 0.");
		}
	}

	/**
	 * Tests if the expected value is greater or equal to 0. Throws IllegalArgumentException if it is not.
	 * @throws IllegalArgumentException if value &lt; 0.
	 */
	public final static void assertPositive(long value, String name) throws IllegalArgumentException {
		if (value < 0) {
			throw new IllegalArgumentException("The parameter " + name + " must be greater or equal to 0.");
		}
	}

	/**
	 * Tests if the expected value is equal to the value. Throws IllegalArgumentException if they
	 * are not equal.
	 * @throws IllegalArgumentException if expected != value
	 */
	public final static void assertEquals(long expected, long value, String message) throws IllegalArgumentException {
		if (expected != value) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Tests if the value is in the range [v0, v1).
	 * @throws IllegalArgumentException if the value parameter is outside the range [v0, v1). If the value is equal to v0
	 * 										no exception is thrown, but if the value is equal to v1 an exception is thrown.
	 */
	public final static void assertInRange(long v0, long v1, long value, String name) {
		if (value < v0 || value >= v1) {
			throw new IllegalArgumentException("The parameter " + name + " is not in the range [" + v0 + ","  + v1 + ").");
		}
	}
}
