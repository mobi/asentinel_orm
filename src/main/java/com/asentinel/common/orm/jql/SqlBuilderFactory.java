package com.asentinel.common.orm.jql;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.orm.ed.tree.EntityDescriptorTreeRepository;

/**
 * Factory interface for creating {@link SqlBuilder} instances. This is not intended
 * to be implemented in the user code.
 *  
 * @author Razvan Popian
 */
public interface SqlBuilderFactory {

	/**
	 * {@code SqlBuilder} factory method.
	 * 
	 * @param clasz the type of the root entity.
	 * @return a {@code SqlBuilder} instance that can create and execute SQL code for
	 * 			the specified class.
	 */
	public <E> SqlBuilder<E> newSqlBuilder(Class<E> clasz);

	/**
	 * @return the underlying {@code EntityDescriptorTreeRepository}.
	 */
	public EntityDescriptorTreeRepository getEntityDescriptorTreeRepository();
	
	/**
	 * @return the underlying {@code SqlQuery}.
	 */
	public SqlQuery getSqlQuery();
}
