package com.asentinel.common.orm.jql;

import org.springframework.beans.factory.InitializingBean;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.orm.ed.tree.EntityDescriptorTreeRepository;
import com.asentinel.common.orm.query.SqlFactory;
import com.asentinel.common.util.Assert;

/**
 * @see SqlBuilderFactory
 * 
 * @author Razvan Popian
 */
public class DefaultSqlBuilderFactory implements SqlBuilderFactory, InitializingBean {
	
	// not final to allow the setter to work. We need to support
	// circular references for EntityDescriptorTreeRepository and SqlBuilderFactory 
	private EntityDescriptorTreeRepository entityDescriptorTreeRepository;
	
	private final SqlFactory sqlFactory;
	private final SqlQuery queryEx;
	
	public DefaultSqlBuilderFactory(
			EntityDescriptorTreeRepository entityDescriptorTreeRepository,
			SqlFactory sqlFactory,
			SqlQuery queryEx) {
		Assert.assertNotNull(entityDescriptorTreeRepository, "entityDescriptorTreeRepository");
		Assert.assertNotNull(sqlFactory, "sqlFactory");
		Assert.assertNotNull(queryEx, "queryEx");
		this.entityDescriptorTreeRepository = entityDescriptorTreeRepository;
		this.sqlFactory = sqlFactory;
		this.queryEx = queryEx;
	}
	
	public DefaultSqlBuilderFactory(
			SqlFactory sqlFactory,
			SqlQuery queryEx) {
		Assert.assertNotNull(sqlFactory, "sqlFactory");
		Assert.assertNotNull(queryEx, "queryEx");
		this.sqlFactory = sqlFactory;
		this.queryEx = queryEx;
	}
	

	@Override
	public <E> SqlBuilder<E> newSqlBuilder(Class<E> clasz) {
		return new SqlBuilder<>(clasz,
				entityDescriptorTreeRepository,
				sqlFactory,
				queryEx);
	}


	@Override
	public EntityDescriptorTreeRepository getEntityDescriptorTreeRepository() {
		return entityDescriptorTreeRepository;
	}
	
	public void setEntityDescriptorTreeRepository(EntityDescriptorTreeRepository entityDescriptorTreeRepository) {
		this.entityDescriptorTreeRepository = entityDescriptorTreeRepository;
	}

	public SqlFactory getSqlFactory() {
		return sqlFactory;
	}

	@Override
	public SqlQuery getSqlQuery() {
		return queryEx;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// we always need an EntityDescriptorTreeRepository
		Assert.assertMemberNotNull(entityDescriptorTreeRepository, "entityDescriptorTreeRepository");
	}
}
