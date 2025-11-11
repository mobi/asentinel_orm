package com.asentinel.common.orm.query;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.flavors.SqlTemplates;
import com.asentinel.common.orm.Entity;
import com.asentinel.common.orm.EntityBuilder;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.ManyToManyQueryReady;
import com.asentinel.common.orm.ParameterizedQuery;
import com.asentinel.common.orm.QueryCriteria;
import com.asentinel.common.orm.QueryReady;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.util.Assert;

/**
 * Interface for creating SQL queries using {@link EntityDescriptor} trees.
 *
 * @see DefaultSqlFactory
 * @see QueryReady
 * @see ManyToManyQueryReady
 * @see SimpleEntityDescriptor
 * 
 * @author Razvan Popian
 */
public interface SqlFactory {

	// standard query building methods
	
	/**
	 * Method that provides access to the configured {@link SqlTemplates} instance.
	 * 
	 * @return the configured {@link SqlTemplates} instance.
	 */
	SqlTemplates getSqlTemplates();
	
	
	/**
	 * @see #buildParameterizedQuery(Node, String...)
	 */
	default String buildQuery(Class<?> clazz, String tableAlias) {
		return buildParameterizedQuery(clazz , tableAlias).getSql();
	}
	
	/**
	 * Creates a sql query for a single annotated {@link Entity} class.
	 * @return the {@link ParameterizedQuery} encapsulating the SQL string and the parameters (if any).
	 */
	default ParameterizedQuery buildParameterizedQuery(Class<?> clazz, String tableAlias) {
		Assert.assertNotNull(clazz, "clazz");
		Assert.assertNotEmpty(tableAlias, "tableAlias");
		return buildParameterizedQuery(
				new SimpleNode<>(
						new SimpleEntityDescriptor.Builder(clazz).tableAlias(tableAlias).build()
						)
				);
	}

	/**
	 * @see #buildQuery(Node)
	 */
	default String buildQuery(Node<?> root) {
		return buildParameterizedQuery(root).getSql();
	}

	/**
	 * @see #buildParameterizedQuery(Node)
	 */
	default ParameterizedQuery buildParameterizedQuery(Node<?> root) {
		return buildParameterizedQuery(root, (String[]) null);
	}
	
	/**
	 * @see #buildParameterizedQuery(Node, String...)
	 */
	default String buildQuery(Node<?> root, String ... additionalColumns) {
		return buildParameterizedQuery(root, additionalColumns).getSql();
	}

	/**
	 * Creates a sql query from a tree of {@link QueryReady} represented
	 * by its root node. This query can be executed and its result can be processed
	 * using a {@link EntityBuilder} instance.
	 * @param root the root of the tree.
	 * @param additionalColumns columns that need to be added to the SELECT statement created
	 * 			by this method, other than those found in the entities. 
	 * @return the {@link ParameterizedQuery} encapsulating the SQL string and the parameters (if any).
	 */
	ParameterizedQuery buildParameterizedQuery(Node<?> root, String ... additionalColumns);

	/**
	 * @see #buildParameterizedQuery(QueryCriteria)
	 */
	default String buildQuery(QueryCriteria criteria) {
		return buildParameterizedQuery(criteria).getSql();
	}
	
	/**
	 * Creates a SQL query from a {@link QueryCriteria} object. There are 3 scenarios that this method
	 * handles:<br><br>
	 * 
	 * 1. <b>The entity descriptor hierarchy contained in the <code>QueryCriteria</code> parameter 
	 * has only associations.</b> In this case the method calls the {@link #buildQuery(Node)} method and appends 
	 * where and order by clauses to the SQL returned by this method.<br>
	 * Example:
	 * 
	 * <pre>
	 * 	// assume the following entity descriptor hierarchy, assume fields are properly annotated
	 * 	// with @Column in the Invoice and Bill class.
	 * 	Node<EntityDescriptor> node1 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(Bill.class));
	 *	Node<EntityDescriptor> node2 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(Invoice.class));
	 *	node1.addChild(node2);
	 *	QueryCriteria criteria  = new QueryCriteria(node1);
	 * </pre>
	 * 
	 * The following sql will result:
	 * 
	 * <pre>
	 *	SELECT 
	 *		t0.BillId t0_BillId, t0.ItemNumber t0_ItemNumber,
	 *		t1.InvoiceId t1_InvoiceId, t1.InvoiceNumber t1_InvoiceNumber
	 *	FROM Bill t0 LEFT JOIN Invoice t1 ON t0.InvoiceId = t1.InvoiceId
	 * </pre>
	 *   
	 * <br><br>
	 * 
	 * 2. <b>The entity descriptor hierarchy contained in the <code>QueryCriteria</code> parameter 
	 * has at least one collection.</b> The result is quite similar to the first case, but the where 
	 * and order by clauses are placed differently.
	 * <br>
	 * Example:
	 * 
	 * <pre>
	 * 	// assume the following entity descriptor hierarchy, assume fields are properly annotated
	 * 	// with @Column in the Invoice and Bill class.
	 * 		Node<EntityDescriptor> node1 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(Invoice.class));
	 * 		Node<EntityDescriptor> node2 = new SimpleNode<EntityDescriptor>(
	 * 			new SimpleEntityDescriptor.Builder(Bill.class).parentRelationType(RelationType.MANY_TO_ONE).build()
	 * 		);
	 *	node1.addChild(node2);
	 *	QueryCriteria criteria  = new QueryCriteria.Builder(node1).mainWhereClause("t0.InvoiceId = 1").build();
	 * </pre>
	 * 
	 * The following sql will result:
	 * 
	 * <pre>
	 *	SELECT 
	 *	    main.*, 
	 *	    t1.BillId t1_BillId, t1.ItemNumber t1_ItemNumber
	 *	FROM (SELECT ROWNUM - 1 RowIndex, indexed.*
	 *		   FROM (SELECT 
	 *				t0.InvoiceId t0_InvoiceId, t0.InvoiceNumber t0_InvoiceNumber
	 *				FROM Invoice t0
	 *		        WHERE t0.InvoiceId = 1) indexed) main
	 *		    LEFT JOIN Bill t1 ON t0_InvoiceId = t1.InvoiceId
	 *	ORDER BY RowIndex	 
	 * </pre>
	 * 
	 * <br><br>
	 * 
	 * 3. <b>The entity descriptor hierarchy contained in the <code>QueryCriteria</code> parameter 
	 * has at least one collection and a group by is requested in the criteria object.</b> This allows the client 
	 * of this method to place conditions on the details section of the query.
	 * <br>
	 * Example:
	 * 
	 * <pre>
	 * 	// assume the following entity descriptor hierarchy, assume fields are properly annotated
	 * 	// with @Column in the Invoice and Bill class.
	 * 		Node<EntityDescriptor> node1 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(Invoice.class));
	 * 		Node<EntityDescriptor> node2 = new SimpleNode<EntityDescriptor>(
	 * 			new SimpleEntityDescriptor.Builder(Bill.class).parentRelationType(RelationType.MANY_TO_ONE).build()
	 * 		);
	 *	node1.addChild(node2);
	 *	QueryCriteria criteria  = new QueryCriteria.Builder(node1)
	 *		.useGroupByOnMainQuery(true)
	 *		.mainWhereClause("t1.Total &gt; 10")
	 *		.mainOrderByClause("t0.InvoiceId")
	 *		.build();
	 * </pre>
	 * 
	 * The following sql will result (it returns the invoices that have at least one bill with the total greater
	 * than 10):
	 * 
	 * <pre>
	 * 	SELECT 
	 * 	main.*, 
	 * 	t1.BillId t1_BillId, t1.ItemNumber t1_ItemNumber
	 * 	FROM    (SELECT ROWNUM - 1 RowIndex, indexed.*
	 *  			FROM (  SELECT 
	 *						t0.InvoiceId t0_InvoiceId, t0.InvoiceNumber t0_InvoiceNumber
	 *						FROM    Invoice t0
	 *						LEFT JOIN
	 *							Bill t1 ON t0.InvoiceId = t1.InvoiceId
	 *						WHERE t1.Total &gt; 10
	 *                      GROUP BY t0.InvoiceId, t0.InvoiceNumber
	 *                      ORDER BY t0.InvoiceId) indexed) main
	 *	LEFT JOIN
	 *  	Bill t1 ON t0_InvoiceId = t1.InvoiceId
	 * 	ORDER BY RowIndex	 
	 * </pre>
	 * 
	 * @param criteria the criteria object.
	 * @return the {@link ParameterizedQuery} encapsulating the SQL string and the parameters (if any).
	 * 
	 * @see QueryCriteria
	 */
	ParameterizedQuery buildParameterizedQuery(QueryCriteria criteria);
	
	// FROM query methods
	
	/**
	 * @see #buildParameterizedFromQuery(Node)
	 */
	default String buildFromQuery(Node<?> root, String ... additionalColumns) {
		return buildParameterizedFromQuery(root).getSql();
	}
	
	
	/**
	 * Creates the FROM part of a SQL query from a tree of {@link QueryReady} represented
	 * by its root node. This methods allows the client code to select only specific columns, but still 
	 * benefit from the table joining features of the {@code QueryUtils class}. 
	 * @param root the root of the tree.
	 * @return the {@link ParameterizedQuery} encapsulating the SQL string and the parameters (if any).
	 */
	ParameterizedQuery buildParameterizedFromQuery(Node<?> root);

	
	// pagination query methods
	// TODO: enhance the documentation of the pagination methods
	
	/**
	 * @see #buildPaginatedParameterizedQuery(QueryCriteria)
	 */
	default String buildPaginatedQuery(QueryCriteria criteria) {
		return buildPaginatedParameterizedQuery(criteria).getSql();
	}
	
	
	/**
	 * @see #buildPaginatedQuery(QueryCriteria, boolean)
	 */
	default ParameterizedQuery buildPaginatedParameterizedQuery(QueryCriteria criteria) {
		return buildPaginatedParameterizedQuery(criteria, false);
	}
	
	/**
	 * @see #buildPaginatedParameterizedQuery(QueryCriteria, boolean)
	 */
	default String buildPaginatedQuery(QueryCriteria criteria, boolean useNamedParams) {
		return buildPaginatedParameterizedQuery(criteria, useNamedParams).getSql();
		
	}
	
	/**
	 * This method behaves in the same way as the {@link #buildQuery(QueryCriteria)}, but it also adds
	 * pagination criteria to the query (it creates a query that pulls a range of records).<br>
	 * The query can be executed using {@link SqlQuery#query(String, org.springframework.jdbc.core.RowCallbackHandler, Object...)}
	 * or {@link NamedParameterJdbcTemplate#query(String, java.util.Map, org.springframework.jdbc.core.RowCallbackHandler)} 
	 * and the results can be processed with an {@link EntityBuilder}.
	 * <br> 
	 * <b>When the user executes the query, he must
	 * REMEMBER to pass the index of the first record in the page (0 based inclusive) and the index
	 * of the last record in the page (0 based exclusive) as the last parameters in the call to
	 * <code>SqlQuery#query(String, org.springframework.jdbc.core.RowCallbackHandler, Object...)</code> 
	 * or if a named parameters query is used the user must provide values in the parameters map for the following keys:
	 * {@link SqlTemplates#getPaginationNamedParam1()} and {@link SqlTemplates#getPaginationNamedParam2()}.
	 * </b>
	 * 
	 * @param criteria the criteria object.
	 * @param useNamedParams if true a named parameters pagination query will be used. If false
	 * 			a query with question mark placeholders will be used.
	 * @return the {@link ParameterizedQuery} encapsulating the SQL pagination string and the parameters (if any).
	 * 
	 * @see #buildQuery(QueryCriteria)
	 * @see #buildCountQuery(QueryCriteria)
	 * @see QueryCriteria
	 */
	ParameterizedQuery buildPaginatedParameterizedQuery(QueryCriteria criteria, boolean useNamedParams);
	/**
	 * @see #buildCountParameterizedQuery(QueryCriteria)
	 */
	default String buildCountQuery(QueryCriteria criteria) {
		return buildCountParameterizedQuery(criteria).getSql();
	}
	
	/**
	 * Creates a counting query for the specified {@link QueryCriteria}. This should be
	 * used for pagination, to determine the total number of records.
	 * 
	 * @param criteria the criteria object.
	 * @return the {@link ParameterizedQuery} encapsulating the count SQL string for the specified {@link QueryCriteria}
	 * 			and the parameters (if any).
	 * 
	 * @see #buildPaginatedQuery(QueryCriteria)
	 * @see QueryCriteria
	 */
	ParameterizedQuery buildCountParameterizedQuery(QueryCriteria criteria);

}
