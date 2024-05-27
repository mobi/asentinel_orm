package com.asentinel.common.orm;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.jdbc.flavors.JdbcFlavorConfig;
import com.asentinel.common.jdbc.flavors.SqlTemplates;
import com.asentinel.common.orm.query.DefaultSqlFactory;
import com.asentinel.common.orm.query.SqlFactory;

/**
 * Utility class for creating SQL queries using {@link EntityDescriptor} trees.
 * 
 * @see QueryReady
 * @see ManyToManyQueryReady
 * @see SimpleEntityDescriptor
 * 
 * @author Razvan Popian
 */
public final class QueryUtils {
	
	/**
	 * @deprecated in favor of {@link SqlTemplates#ROW_INDEX_COL}.
	 */
	@Deprecated
	public final static String ROW_INDEX_COL = SqlTemplates.ROW_INDEX_COL;	
	
	@Deprecated
	private final static SqlFactory sqlFactory = new DefaultSqlFactory(JdbcFlavorConfig.getJdbcFlavor());
	
	/**
	 * @return the static {@code sqlFactory} that this class encapsulates. This method
	 * 		allows the {@code SqlFactory} instance to be used inside an application context.
 	 * @deprecated in favor of constructing the {@link SqlFactory} with its constructor.
	 */
	@Deprecated
	public static SqlFactory getSqlFactory() {
		return sqlFactory;
	}

	/** 
	 * The maximum descriptor id. When this value is reached the thread local 
	 * descriptor id is reset to 0. This means that the last possible table alias
	 * returned by the method {@link #nextTableAlias()} is "tfff".  
	 */
	final static int MAX_DESCRIPTOR_ID = 0x1000;
	
	private final static ThreadLocal<Integer> descriptorId = new ThreadLocal<Integer>();

	private QueryUtils() {}

	
	// standard query building methods
	
	/**
	 * @see SqlFactory#buildQuery(Class, String)
	 * 
	 * @deprecated in favor of the corresponding method is {@link SqlFactory}.
	 */
	@Deprecated
	public static String buildQuery(Class<?> clazz, String tableAlias) {
		return sqlFactory.buildQuery(clazz, tableAlias);
	}
	
	/**
	 * @see SqlFactory#buildParameterizedQuery(Class, String)
	 * 
	 * @deprecated in favor of the corresponding method is {@link SqlFactory}.
	 */
	@Deprecated
	public static ParameterizedQuery buildParameterizedQuery(Class<?> clazz, String tableAlias) {
		return sqlFactory.buildParameterizedQuery(clazz, tableAlias);
	}

	/**
	 * @see SqlFactory#buildQuery(Node)
	 * 
	 * @deprecated in favor of the corresponding method is {@link SqlFactory}.
	 */
	@Deprecated
	public static String buildQuery(Node<?> root) {
		return sqlFactory.buildQuery(root);
	}

	/**
	 * @see #buildParameterizedQuery(Node, String...)
	 * 
	 * @deprecated in favor of the corresponding method is {@link SqlFactory}.
	 */
	@Deprecated
	public static ParameterizedQuery buildParameterizedQuery(Node<?> root) {
		return sqlFactory.buildParameterizedQuery(root);
	}
	
	/**
	 * @see SqlFactory#buildQuery(Node, String...)
	 * 
	 * @deprecated in favor of the corresponding method is {@link SqlFactory}.
	 */
	@Deprecated
	public static String buildQuery(Node<?> root, String ... additionalColumns) {
		return sqlFactory.buildQuery(root, additionalColumns);
	}

	/**
	 * @see SqlFactory#buildParameterizedQuery(Node, String...)
	 * 
	 * @deprecated in favor of the corresponding method is {@link SqlFactory}.
	 */
	@Deprecated
	public static ParameterizedQuery buildParameterizedQuery(Node<?> root, String ... additionalColumns) {
		return sqlFactory.buildParameterizedQuery(root, additionalColumns);
	}
	
	
	/**
	 * @see SqlFactory#buildQuery(QueryCriteria)
	 * 
	 * @deprecated in favor of the corresponding method is {@link SqlFactory}.
	 */
	@Deprecated
	public static String buildQuery(QueryCriteria criteria) {
		return sqlFactory.buildQuery(criteria);
	}
	

	/**
	 * @see SqlFactory#buildParameterizedQuery(QueryCriteria)
	 * 
	 * @deprecated in favor of the corresponding method is {@link SqlFactory}.
	 */
	@Deprecated
	public static ParameterizedQuery buildParameterizedQuery(QueryCriteria criteria) {
		return sqlFactory.buildParameterizedQuery(criteria);
	}
	
	// FROM query methods
	
	/**
	 * @see SqlFactory#buildFromQuery(Node, String...)
	 * 
	 * @deprecated in favor of the corresponding method is {@link SqlFactory}.
	 */
	@Deprecated
	public static String buildFromQuery(Node<?> root, String ... additionalColumns) {
		return sqlFactory.buildFromQuery(root, additionalColumns);
	}
	

	/**
	 * @see SqlFactory#buildParameterizedFromQuery(Node)
	 * 
	 * @deprecated in favor of the corresponding method is {@link SqlFactory}.
	 */
	@Deprecated
	public static ParameterizedQuery buildParameterizedFromQuery(Node<?> root) {
		return sqlFactory.buildParameterizedFromQuery(root);
	}
	
	
	
	// pagination query methods
	
	/**
	 * @see SqlFactory#buildPaginatedQuery(QueryCriteria)
	 * 
	 * @deprecated in favor of the corresponding method is {@link SqlFactory}.
	 */
	@Deprecated
	public static String buildPaginatedQuery(QueryCriteria criteria) {
		return sqlFactory.buildPaginatedQuery(criteria);
	}
	
	
	/**
	 * @see SqlFactory#buildPaginatedParameterizedQuery(QueryCriteria)
	 * 
	 * @deprecated in favor of the corresponding method is {@link SqlFactory}.
	 */
	@Deprecated
	public static ParameterizedQuery buildPaginatedParameterizedQuery(QueryCriteria criteria) {
		return sqlFactory.buildPaginatedParameterizedQuery(criteria);
	}
	
	/**
	 * @see #buildPaginatedParameterizedQuery(QueryCriteria, boolean)
	 * 
	 * @deprecated in favor of the corresponding method is {@link SqlFactory}.
	 */
	@Deprecated
	public static String buildPaginatedQuery(QueryCriteria criteria, boolean useNamedParams) {
		return sqlFactory.buildPaginatedQuery(criteria, useNamedParams);
		
	}


	/**
	 * @see SqlFactory#buildPaginatedParameterizedQuery(QueryCriteria, boolean) 
	 * 
	 * @deprecated in favor of the corresponding method is {@link SqlFactory}.
	 */
	@Deprecated
	public static ParameterizedQuery buildPaginatedParameterizedQuery(QueryCriteria criteria, boolean useNamedParams) {
		return sqlFactory.buildPaginatedParameterizedQuery(criteria, useNamedParams);
	}

	/**
	 * @see SqlFactory#buildCountQuery(QueryCriteria)
	 * 
	 * @deprecated in favor of the corresponding method is {@link SqlFactory}.
	 */
	@Deprecated
	public static String buildCountQuery(QueryCriteria criteria) {
		return sqlFactory.buildCountQuery(criteria);
	}

	/**
	 * @see SqlFactory#buildCountParameterizedQuery(QueryCriteria)
	 * 
	 * @deprecated in favor of the corresponding method is {@link SqlFactory}.
	 */
	@Deprecated
	public static ParameterizedQuery buildCountParameterizedQuery(QueryCriteria criteria) {
		return sqlFactory.buildCountParameterizedQuery(criteria);
	}
	
	
	// descriptor id methods
	
	/**
	 * @return the next descriptor id. The id is used to 
	 * 			create unique table aliases for each descriptor.
	 */
	private static Integer nextDescriptorId() {
		Integer id = descriptorId.get();
		if (id == null 
				|| id >= MAX_DESCRIPTOR_ID
				|| id < 0
				) {
			id = 0;
		}
		descriptorId.set(id + 1);
		return id;
	}
	
	/**
	 * @return the next table alias based on the next
	 * 		descriptor id.
	 * 		The first table alias is "t0" which corresponds to the descriptor id 0x00.  
	 * 		The last table alias is "t[{@link #MAX_DESCRIPTOR_ID} - 1]"
	 * 		or to be more specific "tfff". This corresponds to the descriptor id value 0xFFF.
	 * 		Once this last descriptor is used the descriptor id counter will reset to 0.
	 * 		These mean that the maximum size of the table alias is 4 characters. This allows for
	 * 		up to {@link #MAX_DESCRIPTOR_ID} tables to be used in the same query. This is more than
	 * 		enough.
	 * @see #resetDescriptorId()
	 */
	public static String nextTableAlias() {
		return "a" + Integer.toHexString(nextDescriptorId());
	}
	
	/**
	 * Resets the descriptor id sequence generator.
	 */
	public static void resetDescriptorId() {
		descriptorId.set(0);
	}
	
	/**
	 * Sets the descriptor id thread local variable. 
	 * @param id
	 */
	public static void setDescriptorId(int id) {
		descriptorId.set(id);
	}
	
}
