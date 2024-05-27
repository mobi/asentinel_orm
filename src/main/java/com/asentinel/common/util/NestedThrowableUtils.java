package com.asentinel.common.util;

import java.util.Optional;

import org.springframework.core.NestedExceptionUtils;

/**
 * Utility class for dealing with nested throwables. It is complementary to the
 * spring {@link NestedExceptionUtils} class.
 * 
 * @see NestedExceptionUtils
 * 
 * @since 1.60.16
 * @author Razvan Popian
 *
 */
public final class NestedThrowableUtils {
	
	private NestedThrowableUtils() { }

	/**
	 * Recursively looks in the causes of the {@code original} throwable for an
	 * exception <b>whose type exactly matches</b> the {@code targetType}. It returns the
	 * first one found.
	 * 
	 * @param original   the throwable to inspect
	 * @param targetType the type of {@code Throwable} to look for
	 * @return empty {@code Optional} if the specified exception could not be found,
	 *         or {@code Optional} containing the exception of the specified type.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Throwable> Optional<T> getCause(Throwable original, Class<T> targetType) {
		Throwable cause = original;
		while (cause != null) {
			if (equals(targetType, cause.getClass())) {
				return (Optional<T>) Optional.of(cause);
			}
			cause = cause.getCause();
		}
		return Optional.empty();
	}
	
	private static boolean equals(Class<?> targetType, Class<?> causeType) {
		return targetType == causeType;
	}
}
