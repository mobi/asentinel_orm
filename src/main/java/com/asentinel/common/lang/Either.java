package com.asentinel.common.lang;

import java.util.Optional;
import java.util.function.Function;

/**
 * Data structure that allows having either a {@link #left} or 
 * a {@link #right} value, represented as an {@link Optional}.
 * 
 * @author horatiu.dan
 *
 * @param <L> the type of the left element
 * @param <R> the type of the right element
 */
public class Either<L,R> {

	private final L left;
	private final R right;
	
	private Either(L left, R right) {
		this.left = left;
		this.right = right;
	}
	
	/**
	 * Factory method that constructs an object that contains 
	 * the provided {@code value} as the left.
	 */
	public static <L,R> Either<L,R> left(L value) {
		return new Either<L, R>(value, null);
	}
	
	/**
	 * Factory method that constructs an object that contains 
	 * the provided {@code value} as the right.
	 */
	public static <L,R> Either<L,R> right(R value) {
		return new Either<L, R>(null, value);
	}
	
	/**
	 * Retrieves the {@link #left}.
	 */
	public Optional<L> getLeft() {
		return Optional.ofNullable(left);
	}
	
	/**
	 * Retrieves the {@link #right}.
	 */
	public Optional<R> getRight() {
		return Optional.ofNullable(right);
	}
	
	/**
	 * Check left is present.
	 */
	public boolean isLeft() {
		return left != null;
	}

	/**
	 * Checks right is present.
	 */
	public boolean isRight() {
		return right != null;
	}
	
	/**
	 * Transforms the {@link #left} using the provided {@code mapper}.
	 * 
	 * @param <T> the type after the transformation
	 */
	public <T> Optional<T> mapLeft(Function<? super L,T> mapper) {
		return map(left, mapper);
	}
	
	/**
	 * Transforms the {@link #right} using the provided {@code mapper}.
	 * 
	 * @param <T> the type after the transformation
	 */
	public <T> Optional<T> mapRight(Function<? super R,T> mapper) {
		return map(right, mapper);
	}
	
	private static <I,O> Optional<O> map(I input, Function<I,O> mapper) {
		O result = null;
		if (input != null) {
			result = mapper.apply(input);
		}
		return Optional.ofNullable(result);
	}
	
	@Override
	public String toString() {
		if (isLeft()) {
			return "Left[" + left + "]";
		}
		return "Right[" + right + "]";
	}		
}
