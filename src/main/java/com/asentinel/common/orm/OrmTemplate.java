package com.asentinel.common.orm;

import java.util.Collection;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.orm.ed.tree.EntityDescriptorTreeRepository;
import com.asentinel.common.orm.jql.SqlBuilder;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumn;
import com.asentinel.common.orm.persist.UpdateSettings;
import com.asentinel.common.orm.persist.Updater;
import com.asentinel.common.util.Assert;

/**
 * @see OrmOperations
 * @see SqlBuilderFactory
 * @see Updater
 *  
 * @author Razvan Popian
 */
public class OrmTemplate implements OrmOperations {
	private final SqlBuilderFactory sqlBuilderFactory;
	private final Updater updater;

	public OrmTemplate(SqlBuilderFactory sqlBuilderFactory, Updater updater) {
		Assert.assertNotNull(sqlBuilderFactory, "sqlBuilderFactory");
		Assert.assertNotNull(updater, "updater");
		this.sqlBuilderFactory = sqlBuilderFactory;
		this.updater = updater;
	}

	@Override
	public <E> SqlBuilder<E> newSqlBuilder(Class<E> clasz) {
		return sqlBuilderFactory.newSqlBuilder(clasz);
	}

	
	// Updater interface methods

	@Override
	public int update(Object entity, UpdateSettings<? extends DynamicColumn> settings) {
		return updater.update(entity, settings);
	}
	
	@Override
	public int upsert(Object entity, UpdateSettings<? extends DynamicColumn> settings, Object... hints) {
		return updater.upsert(entity, settings, hints);
	}
	

	@Override
	public <E> void update(Collection<E> entities, UpdateSettings<? extends DynamicColumn> settings) {
		updater.update(entities, settings);		
	}
	
	@Override
	public <E> void upsert(Collection<E> entities, UpdateSettings<? extends DynamicColumn> settings, Object... hints) {
		updater.upsert(entities, settings, hints);
	}
	
	@Override
	public int delete(Class<?> entityType, Object... ids) {
		return updater.delete(entityType, ids);
	}
	
	
	// Convenience methods

	public SqlBuilderFactory getSqlBuilderFactory() {
		return sqlBuilderFactory;
	}
	
	public Updater getUpdater() {
		return updater;
	}

	@Override
	public EntityDescriptorTreeRepository getEntityDescriptorTreeRepository() {
		return sqlBuilderFactory.getEntityDescriptorTreeRepository();
	}

	@Override
	public SqlQuery getSqlQuery() {
		return sqlBuilderFactory.getSqlQuery();
	}

}
