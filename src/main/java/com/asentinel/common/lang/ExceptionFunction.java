package com.asentinel.common.lang;

/**
 * Represents a function that accepts one argument and produces a result.
 * <p>
 * During the function is applied, an exception might be thrown.
 * <p>
 * This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object)}.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @param <E> the type of {@link Exception} thrown
 * 
 * @author horatiu.dan
 */
@FunctionalInterface
public interface ExceptionFunction<T, R, E extends Exception> {
	
	 /**
     * Applies this function to the given argument.
     *
     * @param t 
     * 		the function argument
     * @return 
     * 		the function result
     * 
     * @throws Exception
     */	
    R apply(T t) throws E; 
}
