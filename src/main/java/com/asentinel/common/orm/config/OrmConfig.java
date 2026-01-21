package com.asentinel.common.orm.config;

import java.sql.DatabaseMetaData;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.SqlQueryTemplate;
import com.asentinel.common.jdbc.flavors.CustomArgumentPreparedStatementSetter;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.h2.H2JdbcFlavor;
import com.asentinel.common.jdbc.flavors.oracle.OracleJdbcFlavor;
import com.asentinel.common.jdbc.flavors.postgres.PgEchoingJdbcTemplate;
import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import com.asentinel.common.orm.OrmOperations;
import com.asentinel.common.orm.OrmTemplate;
import com.asentinel.common.orm.ed.tree.DefaultEntityDescriptorTreeRepository;
import com.asentinel.common.orm.ed.tree.EntityDescriptorTreeRepository;
import com.asentinel.common.orm.jql.DefaultSqlBuilderFactory;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.persist.SimpleUpdater;
import com.asentinel.common.orm.query.DefaultSqlFactory;
import com.asentinel.common.orm.query.SqlFactory;

/**
 * @since 1.72.0
 * @author Razvan Popian 
 */
@Configuration
public class OrmConfig {
	private final static Logger log = LoggerFactory.getLogger(OrmConfig.class);
	
	private static final String PG_NAME = "PostgreSQL";

	private static final String ORM_SQL_BUILDER_FACTORY_BEAN_NAME = "ormSqlBuilderFactory";
	static final String ORM_CS_BEAN_NAME = "ormConversionService";

	/**
	 * Expensive call as it opens a connection to the database to retrieve metadata.
	 */
	@Bean(autowireCandidate = false)
	public String getDatabaseName(DataSource dataSource) throws MetaDataAccessException {
		log.debug("getDatabaseName - Determining database product name...");
		String name = JdbcUtils.extractDatabaseMetaData(dataSource,
				DatabaseMetaData::getDatabaseProductName);
		log.info("getDatabaseName - Found: {}", name);
		return name;
	}
	
    @Bean
    public JdbcFlavor jdbcFlavor(DataSource dataSource) throws MetaDataAccessException {
    	String name = getDatabaseName(dataSource);
    	if (PG_NAME.equalsIgnoreCase(name)) {
    		return new PostgresJdbcFlavor();
    	} else if ("H2".equalsIgnoreCase(name)) {
			return new H2JdbcFlavor();
		} else if ("Oracle".equalsIgnoreCase(name)) {
			return new OracleJdbcFlavor();
		} else {
			throw new IllegalStateException("Database '" + name + "' is not yet supported. "
					+ "A new kind of " + JdbcFlavor.class.getSimpleName() + " is needed. We welcome contributions!");
		}
    }
    
	@Bean
	public JdbcOperations jdbcOperations(DataSource dataSource, JdbcFlavor jdbcFlavor) throws MetaDataAccessException {
		String name = getDatabaseName(dataSource);
		if (PG_NAME.equalsIgnoreCase(name)) {
			PgEchoingJdbcTemplate pgt =  new PgEchoingJdbcTemplate(dataSource);
			pgt.setJdbcFlavor(jdbcFlavor);
			return pgt;
		} else {
			return new JdbcTemplate(dataSource) {
				/*
				 * add support for byte[], InputStream and Enum params
				 */
				@Override
				protected PreparedStatementSetter newArgPreparedStatementSetter(Object[] args) {
					return new CustomArgumentPreparedStatementSetter(jdbcFlavor, args);
				}
			};
		}
	}

    @Bean
    public SqlQuery sqlQuery(JdbcFlavor jdbcFlavor, JdbcOperations jdbcOps) {
        return new SqlQueryTemplate(jdbcFlavor, jdbcOps);
    }

    @Bean
    public SqlFactory sqlFactory(JdbcFlavor jdbcFlavor) {
        return new DefaultSqlFactory(jdbcFlavor);
    }

    @Bean
    public DefaultEntityDescriptorTreeRepository entityDescriptorTreeRepository(
    		@Qualifier(ORM_SQL_BUILDER_FACTORY_BEAN_NAME) SqlBuilderFactory sqlBuilderFactory) {
    	ConversionService conversionService = ormConversionService();
        DefaultEntityDescriptorTreeRepository treeRepository = new DefaultEntityDescriptorTreeRepository();
        treeRepository.setSqlBuilderFactory(sqlBuilderFactory);
        treeRepository.setConversionService(conversionService);
        return treeRepository;
    }

    @Bean(name = ORM_SQL_BUILDER_FACTORY_BEAN_NAME)
    public DefaultSqlBuilderFactory sqlBuilderFactory(@Lazy EntityDescriptorTreeRepository entityDescriptorTreeRepository,
                                                      SqlFactory sqlFactory, SqlQuery sqlQuery) {
        DefaultSqlBuilderFactory sqlBuilderFactory = new DefaultSqlBuilderFactory(sqlFactory, sqlQuery);
        sqlBuilderFactory.setEntityDescriptorTreeRepository(entityDescriptorTreeRepository);
        return sqlBuilderFactory;
    }
    
	/**
	 * Not an autowire candidate, to avoid conflicts with other conversion service
	 * beans that might be present in the container.
	 */
    @Bean(name = OrmConfig.ORM_CS_BEAN_NAME, autowireCandidate = false)
    public ConfigurableConversionService ormConversionService() {
    	return new GenericConversionService();
    }
    
    @Bean
    public OrmOperations orm(JdbcFlavor jdbcFlavor, SqlQuery sqlQuery,
							 @Qualifier(ORM_SQL_BUILDER_FACTORY_BEAN_NAME) SqlBuilderFactory sqlBuilderFactory) {
    	ConversionService conversionService = ormConversionService();
       	SimpleUpdater updater = new SimpleUpdater(jdbcFlavor, sqlQuery);
    	updater.setConversionService(conversionService);
        return new OrmTemplate(sqlBuilderFactory, updater);
    }
}
