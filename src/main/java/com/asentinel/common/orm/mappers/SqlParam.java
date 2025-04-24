package com.asentinel.common.orm.mappers;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.SqlParameter;

/**
 * Annotation holding information about the type of the mapped database column
 * if the column is not a standard SQL type. It should be used together with a
 * {@code ConversionService} when custom conversion from the java type to the
 * SQL type is needed. The value of the annotation will be used to construct a
 * {@link SqlParameter} instance that will be used by a converter registered
 * with the {@link ConversionService}.
 * 
 * @see Column
 * @see SqlParameter
 * 
 * @since 1.71.0
 * @author Razvan Popian
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SqlParam {

	// TODO: add the other attributes of the SqlParameter class
	
	/**
	 * @return information to be used by the {@code ConversionService} to perform
	 *         the conversion from the java type to the database type. The default
	 *         is empty indicating that the default conversion should be performed.
	 *         The value will be used to create an {@link SqlParameter} instance
	 *         with the type name equal to the value.
	 * 
	 * @see SqlParameter#getTypeName()
	 */
	String value() default "";

}
