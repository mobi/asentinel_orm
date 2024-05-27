package com.asentinel.common.lang;

/**
 * Represents a predicate (boolean-valued function) of one argument that might
 * throw an exception.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #test(Object)}.
 * 
 * @author horatiu.dan
 *
 * @param <T> the type of the input to the predicate
 * @param <E> the type of {@link Exception} thrown
 */
@FunctionalInterface
public interface ExceptionPredicate<T, E extends Exception> {

	/**
     * Evaluates this predicate on the given argument.
     *
     * @param t 
     * 		the input argument
     * @return {@code true} if the input argument matches the predicate,
     * 		   otherwise {@code false}
     * 
     * @throws Exception
     */	
    boolean test(T t) throws E; 
}
