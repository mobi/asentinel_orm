package com.asentinel.common.orm.mappers;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation holding information about the type of the mapped database column
 * if the column is not a standard SQL type. It should be used together with a
 * {@code ConversionService} when custom conversion from the java type to the
 * SQL type is needed.
 * 
 * @see Column
 * 
 * @since 1.71.0
 * @author Razvan Popian
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DbType {
	
	/**
	 * @return information to be used by the {@code ConversionService} to perform
	 *         the conversion from the java type to the database type. The default
	 *         is empty indicating that the default conversion should be performed.
	 */
	String value() default "";

}
