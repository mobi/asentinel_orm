package com.asentinel.common.lang;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Allows wrapping calls that throw checked {@code Exception}s
 * so that they can be used inside lambdas or higher order
 * functions. 
 * 
 * @author horatiu.dan
 */
public final class ExceptionWrapper {
				
    /**
     * Effectively applies the provided {@code function}.
     * <p>
     * In case a checked {@code Exception} is thrown, it is wrapped 
     * into a {@link RuntimeException} and thrown forward.
     * 
     * @param function
     * 			the call that is wrapped
     */
	public static <T, R, E extends Exception> Function<T, R> apply(ExceptionFunction<T, R, E> function) {
		return t -> {
			try {
				return function.apply(t);
			} catch (Exception e) {				  
				throw new RuntimeException(e);
			}
		};
	}
            
    /**
     * Effectively applies the provided {@code function}.
     * <p>
     * In case a checked {@code Exception} is thrown, this is wrapped 
     * into an {@link Either} object, on the left.
     * <p>
     * In case the call is successful, the result is wrapped
     * into an {@link Either} object, on the right.
     * 
     * @see Either
     * 
     * @param function
     * 			the call that is wrapped
     */
    @SuppressWarnings("unchecked")
	public static <T, R, E extends Exception> Function<T, Either<E, R>> applyEither(ExceptionFunction<T, R, E> function) {
    	return t -> {
			try {
				return Either.right(function.apply(t));
			} catch (Exception e) {				  
				return Either.left((E) e);
			}
		};
    }
    
    /**
     * Effectively applies the provided {@code predicate}.
     * <p>
     * In case a checked {@code Exception} is thrown, it is wrapped 
     * into a {@link RuntimeException} and thrown forward.
     * 
     * @param predicate
     * 			the predicate that is wrapped
     * @return
     * 		the result of the provided {@code predicate}
     * 		in case an exception is not thrown
     */
	public static <T, E extends Exception> Predicate<T> test(ExceptionPredicate<T, E> predicate) {
		return t -> {
			try {
				return predicate.test(t);
			} catch (Exception e) {				  
				throw new RuntimeException(e);
			}
		};
	}
    
    ExceptionWrapper() {
    	throw new UnsupportedOperationException("No need to be called.");
    }
}
