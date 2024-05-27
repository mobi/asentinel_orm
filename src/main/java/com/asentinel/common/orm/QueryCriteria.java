package com.asentinel.common.orm;

import org.springframework.util.StringUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.orm.query.SqlFactory;
import com.asentinel.common.util.Assert;

/**
 * This class encapsulates the data necessary for the <code>QueryUtils#buildXxx</code> methods
 * to create SQL queries. At a minimum it needs a tree of {@link QueryReady} objects 
 * (usually <code>EntityDescriptors</code>). There is a builder to assist in constructing
 * instances of this class.
 *
 * @see SqlFactory#buildQuery(QueryCriteria)
 * @see SqlFactory#buildPaginatedQuery(QueryCriteria)
 * @see SqlFactory#buildCountQuery(QueryCriteria)
 * 
 * @author Razvan Popian
 */
public class QueryCriteria {
	
	private final Node<EntityDescriptor> queryReadyNode;
	private final String mainWhereClause;
	private final String mainHavingClause;
	private final String secondaryWhereClause;
	private final String mainOrderByClause;
	private final String[] mainAdditionalColumns;
	private final String[] mainGroupByAdditionalColumns;
	private final boolean useGroupByOnMainQuery;
	
	// TODO: add secondary order by clause

	/**
	 * Constructor that takes a tree of {@link QueryReady}
	 * as argument.
	 */
	public QueryCriteria(Node<EntityDescriptor> queryReadyNode){
		Assert.assertNotNull(queryReadyNode, "queryReadyNode");
		Assert.assertNotNull(queryReadyNode.getValue(), "queryReadyNode.getValue()");
		this.queryReadyNode = queryReadyNode;
		this.mainWhereClause = null;
		this.mainHavingClause = null;
		this.secondaryWhereClause = null;
		this.mainOrderByClause = null;
		this.useGroupByOnMainQuery = false;
		this.mainAdditionalColumns = null;
		this.mainGroupByAdditionalColumns = null;
	}
	
	private QueryCriteria(Builder builder) {
		this.queryReadyNode = builder.entityDescriptorNode;
		this.mainWhereClause = builder.mainWhereClause;
		this.mainHavingClause = builder.mainHavingClause;
		this.secondaryWhereClause = builder.secondaryWhereClause;
		this.mainOrderByClause = builder.mainOrderByClause;
		this.useGroupByOnMainQuery = builder.useGroupByOnMainQuery;
		this.mainAdditionalColumns = builder.mainAdditionalColumns;
		this.mainGroupByAdditionalColumns = builder.mainGroupByAdditionalColumns;
	}


	public Node<?> getQueryReadyNode() {
		return queryReadyNode;
	}


	public String getMainWhereClause() {
		return mainWhereClause;
	}
	
	public String getMainHavingClause() {
		return mainHavingClause;
	}

	public String getSecondaryWhereClause() {
		return secondaryWhereClause;
	}

	/**
	 * @return the main order by clause as defined by the client of this
	 * 		class.
	 * @see #getMainOrderByClauseSafe()
	 */
	public String getMainOrderByClause() {
		return mainOrderByClause;
	}
	
	/**
	 * @return the main order by clause set by the client with the primary key of the root entity descriptor
	 * 		appended to it. This ensures that the order of the elements will be deterministic even for columns that have 
	 * 		the same values in the client set order by columns.
	 * 
	 * @see #getMainOrderByClauseSafe()
	 */
	public String getMainOrderByClauseSafe() {
		EntityDescriptor ed = queryReadyNode.getValue();
		if (!(ed instanceof QueryReady)) {
			throw new IllegalStateException("The root entity descriptor " + ed + " is not a QueryReady implementation.");
		}
		QueryReady qr = (QueryReady) ed;
		
		String tableAlias = qr.getTableAlias();
		Class<?> rootType = ed.getEntityClass();
		String pkName = TargetMembersHolder.getInstance().getTargetMembers(rootType)
				.getPkColumnMember().getPkColumnAnnotation().value();
		
		String colName = tableAlias + "." + pkName;
		if (StringUtils.hasText(mainOrderByClause)) {
			if (mainOrderByClause.toLowerCase().contains(colName.toLowerCase())) {
				return mainOrderByClause;
			}
			return mainOrderByClause + ", " + colName;
		} else {
			return colName;
		}
	}
	

	public String[] getMainAdditionalColumns() {
		if (mainAdditionalColumns == null) {
			return new String[0];
		}
		return mainAdditionalColumns;
	}

	public String[] getMainGroupByAdditionalColumns() {
		if (mainGroupByAdditionalColumns == null) {
			return new String[0];
		}
		return mainGroupByAdditionalColumns;
	}
	

	public boolean isUseGroupByOnMainQuery() {
		return useGroupByOnMainQuery;
	}

	public static class Builder {
		protected final Node<EntityDescriptor> entityDescriptorNode;
		protected String mainWhereClause;
		protected String mainHavingClause;
		protected String secondaryWhereClause;
		protected String mainOrderByClause;
		protected String[] mainAdditionalColumns;
		protected String[] mainGroupByAdditionalColumns;
		protected boolean useGroupByOnMainQuery;

		/**
		 * Constructor.
		 * @param queryReadyNode
		 */
		public Builder(Node<EntityDescriptor> queryReadyNode) {
			Assert.assertNotNull(queryReadyNode, "queryReadyNode");
			Assert.assertNotNull(queryReadyNode.getValue(), "queryReadyNode.getValue()");
			this.entityDescriptorNode = queryReadyNode;
		}
		
		/**
		 * Sets the main where clause without the <code>where</code> keyword. The string provided here
		 * should only apply to entities/tables that are not part of a collection (the main query). Conditions 
		 * for the details part of the query can be specified only if group by is used.
		 * 
		 * @see #useGroupByOnMainQuery(boolean)
		 */
		public Builder mainWhereClause(String mainWhereClause) {
			this.mainWhereClause = mainWhereClause;
			return this;
		}

		/**
		 * Sets the main having clause without the <code>having</code> keyword. The string provided here
		 * should only apply to entities/tables that are part of a collection (details). A having clause will
		 * only be used if {@link #useGroupByOnMainQuery(boolean)} method is called with <code>true</code>.
		 * 
		 * @see #useGroupByOnMainQuery(boolean)
		 */
		public Builder mainHavingClause(String mainHavingClause) {
			this.mainHavingClause = mainHavingClause;
			return this;
		}

		/**
		 * Sets the secondary where clause without the <code>where</code> keyword. The string provided here
		 * should only apply to entities/tables that are part of the secondary queries (the details).
		 */
		public Builder secondaryWhereClause(String secondaryWhereClause) {
			this.secondaryWhereClause = secondaryWhereClause;
			return this;
		}


		/**
		 * Sets the order by clause without the <code>order by</code> keyword. The string provided here
		 * should only apply to entities/tables that are not part of a collection. Columns 
		 * for the details part of the query can be specified only if group by is used.
		 */
		public Builder mainOrderByClause(String mainOrderByClause) {
			this.mainOrderByClause = mainOrderByClause;
			return this;
		}
		
		/**
		 * Sets the columns that need to be added to the main SELECT statement, other than those found 
		 * in the entities. If group by is used this columns must be also set with the 
		 * {@link #mainGroupByAdditionalColumns}.
		 * 
		 * @see #mainGroupByAdditionalColumns
		 */
		public Builder mainAdditionalColumns(String ... mainAdditionalColumns) {
			this.mainAdditionalColumns = mainAdditionalColumns;
			return this;
		}

		/**
		 * Sets the columns that need to be added to the main SELECT statement GROUP by clause. 
		 * This columns must also be set using the {@link #mainAdditionalColumns(String...)}
		 * 
		 * @see #mainAdditionalColumns
		 */
		public Builder mainGroupByAdditionalColumns(String ... mainGroupByAdditionalColumns) {
			this.mainGroupByAdditionalColumns = mainGroupByAdditionalColumns;
			return this;
		}
		
		/**
		 * Sets whether to use group by in the main query or not.
		 */
		public Builder useGroupByOnMainQuery(boolean useGroupByOnMainQuery) {
			this.useGroupByOnMainQuery = useGroupByOnMainQuery;
			return this;
		}
		
		/**
		 * Build method.
		 * @throws IllegalArgumentException if the number of additional columns in the main additional columns array
		 * 			is different from the number of additional columns for the GROUP BY clause. 
		 */
		public QueryCriteria build() throws IllegalArgumentException {
			return new QueryCriteria(this);
		}
	}

}
