package com.asentinel.common.jdbc.exceptions.resolve;

import java.util.Optional;

/**
 * Validator like class that can turn exceptions into a
 * {@code MessageCodeWrapper} which in turn can be used to display a friendly
 * localized (translated) message to the end user.
 * 
 * @since 1.60.12
 * @author Razvan Popian
 */
public interface ExceptionResolver {

	/**
	 * Tests if this resolver supports a certain exception and type.
	 * 
	 * @param ex   the exception to be checked.
	 * @param type the type of {@code Object} that this resolver should support, it
	 *             is passed only if available.
	 * @return {@code true} if this resolver supports a certain exception and type.
	 */
	boolean supports(Exception ex, Optional<Class<?>> type);

	/**
	 * Turns the exception into a {@code MessageCodeWrapper}.
	 * 
	 * @param ex     the exception to be checked.
	 * @param object the actual object for which the exception occurred if available.
	 * @return {@code MessageCodeWrapper} that can be used to render a friendly
	 *         localized (translated) error message to the end user. It should never
	 *         return {@code null}.
	 */
	MessageCodeWrapper resolve(Exception ex, Optional<?> object);
}
