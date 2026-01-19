package com.asentinel.common.orm.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.jdbc.core.JdbcOperations;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.SqlQueryTemplate;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
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
public class OrmConfig {
	
	static final String ORM_CS_BEAN_NAME = "ormConversionService";

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
    		SqlBuilderFactory sqlBuilderFactory,
    		@Qualifier(ORM_CS_BEAN_NAME) ConversionService conversionService) {
        DefaultEntityDescriptorTreeRepository treeRepository = new DefaultEntityDescriptorTreeRepository();
        treeRepository.setSqlBuilderFactory(sqlBuilderFactory);
        treeRepository.setConversionService(conversionService);
        return treeRepository;
    }

    @Bean
    public DefaultSqlBuilderFactory sqlBuilderFactory(@Lazy EntityDescriptorTreeRepository entityDescriptorTreeRepository,
                                                      SqlFactory sqlFactory, SqlQuery sqlQuery) {
        DefaultSqlBuilderFactory sqlBuilderFactory = new DefaultSqlBuilderFactory(sqlFactory, sqlQuery);
        sqlBuilderFactory.setEntityDescriptorTreeRepository(entityDescriptorTreeRepository);
        return sqlBuilderFactory;
    }
    
    @Bean(name = OrmConfig.ORM_CS_BEAN_NAME)
    public ConfigurableConversionService ormConversionService() {
    	GenericConversionService conversionService = new GenericConversionService();
    	return conversionService;
    }
    
    @Bean
    public OrmOperations orm(JdbcFlavor jdbcFlavor, SqlQuery sqlQuery,
                             SqlBuilderFactory sqlBuilderFactory,
                             @Qualifier(ORM_CS_BEAN_NAME) ConversionService conversionService) {
       	SimpleUpdater updater = new SimpleUpdater(jdbcFlavor, sqlQuery);
    	updater.setConversionService(conversionService);
        return new OrmTemplate(sqlBuilderFactory, updater);
    }
}
