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
 * <li>{@code JdbcFlavor}
 * <li>{@code JdbcOperations} built on top of a configured {@code DataSource}
 * <li>{@code OrmConversionServiceConfig} is optional for registering custom
 * converters.<br>
 * 
 * <br>
 * <br>
 * 
 * Example:
 * 
 * <pre>
 * &#64;Bean
 * public DataSource dataSource() {
 * 	return new SingleConnectionDataSource("jdbc:h2:mem:testdb", "sa", "", false);
 * }
 * 
 * &#64;Bean
 * public JdbcFlavor jdbcFlavor() {
 * 	return new H2JdbcFlavor();
 * }
 * 
 * &#64;Bean
 * public JdbcOperations jdbcOperations(DataSource dataSource, JdbcFlavor jdbcFlavor) {
 * 	return new JdbcTemplate(dataSource) {
 * 
 * 		// add support for byte[], InputStream and Enum params
 * 		&#64;Override
 * 		protected PreparedStatementSetter newArgPreparedStatementSetter(Object[] args) {
 * 			return new CustomArgumentPreparedStatementSetter(jdbcFlavor, args);
 * 		}
 * 	};
 * }
 * </pre>
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
