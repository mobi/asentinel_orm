package com.asentinel.common.orm.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.asentinel.common.orm.OrmOperations;

/**
 * Enables Asentinel ORM support. Creates the central ORM interface
 * {@link OrmOperations} bean that can be then injected into other beans. Client
 * code is responsible for providing the following beans:
 * <li>{@code DataSource}
 * <li>{@code OrmConversionServiceConfig} - optional for registering custom
 * converters.
 * 
 * @since 1.72.0
 * @author Razvan Popian
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(OrmConfig.class)
public @interface EnableAsentinelOrm {

}
