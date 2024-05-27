package com.asentinel.common.jdbc.exceptions.resolve;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation processed by the {@link ResolvableExceptionAspect}. Assuming
 * correct configuration, annotated methods would trigger the advice and using
 * any registered {@link ExceptionResolver}s they would turn supported
 * exceptions into a {@link ResolvedException} that encapulates the information
 * needed to render a friendly message to the end user.
 *
 * @see ResolvableExceptionAspect
 * 
 * @since 1.60.12
 * @author Razvan Popian
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ThrowsResolvableException {

}
