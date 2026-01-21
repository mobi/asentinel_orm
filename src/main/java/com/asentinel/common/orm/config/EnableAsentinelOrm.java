package com.asentinel.common.orm.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.asentinel.common.orm.OrmTemplate;
import org.springframework.context.annotation.Import;

import com.asentinel.common.orm.OrmOperations;

/**
 * Enables Asentinel ORM support. It creates an {@link OrmTemplate} bean instance
 * implementing the ORM centarl interface {@link OrmOperations} - that can be
 * further injected as needed.
 * <br> Client code can provide either:
 * <li> the {@code DataSource} instance, or
 * <li> a dependency that ensures the automatic {@code DataSource} configuration
 * together with the appropriate properties (e.g., {@code spring-boot-starter-jdbc} and
 * {@code spring.datasource.url}, {@code spring.datasource.username}, {@code spring.datasource.password}, etc.).
 * <br>
 * <br> Optionally, it can provide a {@code OrmConversionServiceConfig} bean, for registering custom converters.
 * 
 * @since 1.72.0
 * @author Razvan Popian
 * @author horatiu.dan
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(OrmConfig.class)
public @interface EnableAsentinelOrm {

}
