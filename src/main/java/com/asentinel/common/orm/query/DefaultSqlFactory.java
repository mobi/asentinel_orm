package com.asentinel.common.orm.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.NodeHandler;
import com.asentinel.common.collections.tree.TreeUtils;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.SqlTemplates;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.JoinType;
import com.asentinel.common.orm.ManyToManyQueryReady;
import com.asentinel.common.orm.ParameterizedQuery;
import com.asentinel.common.orm.QueryCriteria;
import com.asentinel.common.orm.QueryReady;
import com.asentinel.common.orm.RelationType;
import com.asentinel.common.orm.TargetMembersHolder;
import com.asentinel.common.util.Assert;
import com.asentinel.common.util.Utils;

/**
 * @see SqlFactory
 *  
 * @author Razvan Popian
 */
public class DefaultSqlFactory implements SqlFactory {
	private final static Logger log = LoggerFactory.getLogger(DefaultSqlFactory.class);
	
	/**
	 * @see #buildPaginatedQuery(QueryCriteria, boolean)
	 * @seee {@link #buildQuery(QueryCriteria)}
	 */
	public final static String ROW_INDEX_COL = SqlTemplates.ROW_INDEX_COL;
	
	final static String FIELD_SEPARATOR = ", ";
	
	/** 
	 * The maximum descriptor id. When this value is reached the thread local 
	 * descriptor id is reset to 0. This means that the last possible table alias
	 * returned by the method {@link #nextTableAlias()} is "tfff".  
	 */
	final static int MAX_DESCRIPTOR_ID = 0x1000;
	
	private final static String COUNT_SQL = "select count(*) from ( %s ) main";
	
	
	private final static ThreadLocal<Integer> descriptorId = new ThreadLocal<Integer>();
	
	private final SqlTemplates sqlTemplates;
	
	public DefaultSqlFactory(SqlTemplates sqlTemplates) {
		Assert.assertNotNull(sqlTemplates, "sqlTemplates");
		this.sqlTemplates = sqlTemplates;
	}
	
	public DefaultSqlFactory(JdbcFlavor jdbcFlavor) {
		this(jdbcFlavor.getSqlTemplates());
	}
	

	public SqlTemplates getSqlTemplates() {
		return sqlTemplates;
	}


	@Override
	public ParameterizedQuery buildParameterizedQuery(Node<?> root, String ... additionalColumns) {
		return buildParameterizedQuery(root, true, additionalColumns);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static ParameterizedQuery buildParameterizedQuery(Node<?> root, boolean standardQuery, String ... additionalColumns) {
		Assert.assertNotNull(root, "root");
		Assert.assertTrue(root.isRoot(), "The Node parameter must be root.");
		Assert.assertNotNull(root.getValue(), "root.getValue()");
		
		long t0 = 0;
		if (log.isTraceEnabled()) {
			t0 = System.nanoTime(); 
		}

		final StringBuilder query = new StringBuilder();
		final StringBuilder fields = new StringBuilder();
		final StringBuilder tables = new StringBuilder();

		if (standardQuery &&  additionalColumns != null) {
			// append additional fields
			for (String c: additionalColumns) {
				if (StringUtils.hasText(c)) {
					fields.append(c).append(FIELD_SEPARATOR);
				}
			}
		}
		
		ParameterizedQuery sq = new ParameterizedQuery();
		root.traverse(new NodeHandler() {
			@Override
			public void handleNode(Node childNode) {
				// this if is to accomodate the CacheEntityDescriptor
				// that does not participate in queries
				if (!isQueryReady(childNode)) {
					return;
				}
				QueryReady childDescriptor = (QueryReady) childNode.getValue();

				// tables
				if (childNode.isRoot()) {
					tables.append(childDescriptor.getTableName()).append(" ").append(childDescriptor.getTableAlias());
				} else {
					List<Object> params = appendJoinConditions(tables, childNode.getParent(), childNode, null);
					sq.addMainParameters(params);
				}
				
				if (standardQuery) {
					// fields & methods
					Collection<String> colNames = childDescriptor.getColumnNames();
					for (String colName: colNames) {
						addColumnName(fields, childDescriptor, colName, true);
					}
				}
			}
		});
		if (!standardQuery) {
			// this is a count query, we add the root PK col name to the fields section
			EntityDescriptor rootEd = (EntityDescriptor) root.getValue();
			String rootPkColumn = TargetMembersHolder.getInstance().getTargetMembers(rootEd.getEntityClass())
					.getPkColumnMember().getPkColumnAnnotation().value();
			addColumnName(fields, (QueryReady) rootEd, rootPkColumn, false);			
		}
		if (StringUtils.hasLength(fields)) {
			fields.delete(fields.length() - FIELD_SEPARATOR.length(), fields.length());
		}
		query.append("select ").append(fields).append(" from ").append(tables).append(" ");
		
		if (log.isTraceEnabled()) {
			long t1 = System.nanoTime();
			log.trace("buildParameterizedQuery - Query built in " + Utils.nanosToMillis(t1 - t0) + " ms. Query string length: " + query.length());
		}
		sq.setSql(query.toString());
		return sq;
	}
	

	@Override
	public ParameterizedQuery buildParameterizedQuery(QueryCriteria criteria) {
		Assert.assertNotNull(criteria, "criteria");
		
		long t0 = 0;
		if (log.isTraceEnabled()) {
			t0 = System.nanoTime(); 
		}
		
		ParameterizedQuery sq;
		Node<?> root = criteria.getQueryReadyNode();
		Assert.assertNotNull(root, "root");
		TreeBreakdown treeBreakdown = breakTree(root);
		if (!treeBreakdown.hasSubTrees()) {
			// the entity descriptor tree has only associations
			sq = buildParameterizedQuery(root, combineAdditionalColumns(sqlTemplates, criteria));
			StringBuilder sql = new StringBuilder(sq.getSql());
			appendWhereClause(sql, criteria);
			appendOrderByClause(sql, criteria);
			String q = String.format(sqlTemplates.getSqlOnlyAssociations(), sql.toString());
			if (log.isTraceEnabled()) {
				long t1 = System.nanoTime();
				log.trace("buildQuery - Query built in " + Utils.nanosToMillis(t1 - t0) + " ms. Query string length: " + q.length());
			}
			sq.setSql(q);
			return sq;
		} else {
			// the entity descriptor tree has at least one entity that contains a collection
			Node<?> rootCopy = treeBreakdown.getMainTree();
			List<SubTree> subTrees = treeBreakdown.getSubTrees();
			logTreeBreakdown(treeBreakdown);
			sq = buildParameterizedQuery(rootCopy, combineAdditionalColumns(sqlTemplates, criteria));
			StringBuilder sql = new StringBuilder(sq.getSql());
			List<Object> params = appendOutsideJoins(sql, criteria, treeBreakdown);
			sq.addMainParameters(params);
			appendWhereClause(sql, criteria);
			appendGroupByClause(sql, criteria, treeBreakdown);
			appendHavingClause(sql, criteria);			
			appendOrderByClause(sql, criteria);
			String secondaryWhereClause = criteria.getSecondaryWhereClause();
			ParameterizedQuery sqOutsideJoins = getOutsideJoins(subTrees, false);
			sq.addSecondaryParameters(sqOutsideJoins.getMainParameters());
			String q = String.format(sqlTemplates.getSqlAtLeastOneCollection(), 
						FIELD_SEPARATOR + getColumnNamesCsv(subTrees, true), 
						sql, 
						sqOutsideJoins.getSql(),
						StringUtils.hasText(secondaryWhereClause) ? 
								(sqlTemplates.isSecondaryWhereStarted() ? "and " : "where ") + secondaryWhereClause 
								: ""
						);
			if (log.isTraceEnabled()) {
				long t1 = System.nanoTime();
				log.trace("buildQuery - Query built in " + Utils.nanosToMillis(t1 - t0) + " ms. Query string length: " + q.length());
			}
			sq.setSql(q);
			return sq;
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ParameterizedQuery buildParameterizedFromQuery(Node<?> root) {
		Assert.assertNotNull(root, "root");
		Assert.assertTrue(root.isRoot(), "The Node parameter must be root.");
		Assert.assertNotNull(root.getValue(), "root.getValue()");
		
		long t0 = 0;
		if (log.isTraceEnabled()) {
			t0 = System.nanoTime(); 
		}

		final StringBuilder tables = new StringBuilder(" from ");

		ParameterizedQuery sq = new ParameterizedQuery();
		root.traverse(new NodeHandler() {
			@Override
			public void handleNode(Node childNode) {
				// this if is to accomodate the CacheEntityDescriptor
				// that does not participate in queries
				if (!isQueryReady(childNode)) {
					return;
				}
				QueryReady childDescriptor = (QueryReady) childNode.getValue();

				// tables
				if (childNode.isRoot()) {
					tables.append(childDescriptor.getTableName()).append(" ").append(childDescriptor.getTableAlias());
				} else {
					List<Object> params = appendJoinConditions(tables, childNode.getParent(), childNode, null);
					sq.addMainParameters(params);
				}
				
			}
		});
		
		if (log.isTraceEnabled()) {
			long t1 = System.nanoTime();
			log.trace("buildParameterizedFromQuery - From query built in " + Utils.nanosToMillis(t1 - t0) + " ms. From query string length: " + tables.length());
		}
		sq.setSql(tables.toString());
		return sq;
	}

	@Override
	public ParameterizedQuery buildPaginatedParameterizedQuery(QueryCriteria criteria, boolean useNamedParams) {
		Assert.assertNotNull(criteria, "criteria");
		
		long t0 = 0;
		if (log.isTraceEnabled()) {
			t0 = System.nanoTime(); 
		}
		ParameterizedQuery sq;
		Node<?> root = criteria.getQueryReadyNode();
		Assert.assertNotNull(root, "root");
		TreeBreakdown treeBreakdown = breakTree(root);
		if (!treeBreakdown.hasSubTrees()) {
			// the entity descriptor tree has only associations
			String sqlTemplate = useNamedParams 
					? sqlTemplates.getPaginationSqlOnlyAssociationsNamed() : sqlTemplates.getPaginationSqlOnlyAssociations();
			sq = buildParameterizedQuery(root, combineAdditionalColumns(sqlTemplates, criteria));
			StringBuilder sql = new StringBuilder(sq.getSql());
			appendWhereClause(sql, criteria);
			appendOrderByClause(sql, criteria);
			String secondaryWhereClause = criteria.getSecondaryWhereClause();
			String q = String.format(sqlTemplate, sql.toString(), 
					StringUtils.hasText(secondaryWhereClause) ? 
							(sqlTemplates.isSecondaryWhereStarted() ? "and " : "where ") + secondaryWhereClause 
							: ""
					);
			if (log.isTraceEnabled()) {
				long t1 = System.nanoTime();
				log.trace("buildPaginatedQuery - Query built in " + Utils.nanosToMillis(t1 - t0)  + " ms. Query string length: " + q.length());
			}
			sq.setSql(q);
			return sq;
		} else {
			// the entity descriptor tree has at least one entity that contains a collection
			String sqlTemplate = useNamedParams 
					? sqlTemplates.getPaginationSqlAtLeastOneCollectionNamed() : sqlTemplates.getPaginationSqlAtLeastOneCollection();
			Node<?> rootCopy = treeBreakdown.getMainTree();
			List<SubTree> subTrees = treeBreakdown.getSubTrees();
			logTreeBreakdown(treeBreakdown);
			sq = buildParameterizedQuery(rootCopy, combineAdditionalColumns(sqlTemplates, criteria));
			StringBuilder sql = new StringBuilder(sq.getSql());
			List<Object> params =  appendOutsideJoins(sql, criteria, treeBreakdown);
			sq.addMainParameters(params);
			appendWhereClause(sql, criteria);
			appendGroupByClause(sql, criteria, treeBreakdown);
			appendHavingClause(sql, criteria);			
			appendOrderByClause(sql, criteria);
			String secondaryWhereClause = criteria.getSecondaryWhereClause();
			ParameterizedQuery sqOutsideJoins = getOutsideJoins(subTrees, false);
			sq.addSecondaryParameters(sqOutsideJoins.getMainParameters());
			String q = String.format(sqlTemplate, 
						FIELD_SEPARATOR + getColumnNamesCsv(subTrees, true), 
						sql, 
						sqOutsideJoins.getSql(),
						StringUtils.hasText(secondaryWhereClause) ? 
								(sqlTemplates.isSecondaryWhereStarted() ? "and " : "where ") + secondaryWhereClause 
								: ""
						);
			if (log.isTraceEnabled()) {
				long t1 = System.nanoTime();
				log.trace("buildPaginatedQuery - Query built in " + Utils.nanosToMillis(t1 - t0) + " ms. Query string length: " + q.length());
			}
			sq.setSql(q);
			return sq;
		}
	}
	

	@Override
	public ParameterizedQuery buildCountParameterizedQuery(QueryCriteria criteria) {
		Assert.assertNotNull(criteria, "criteria");
		long t0 = 0;
		if (log.isTraceEnabled()) {
			t0 = System.nanoTime(); 
		}
		ParameterizedQuery sq;
		Node<?> root = criteria.getQueryReadyNode();
		Assert.assertNotNull(root, "root");
		TreeBreakdown treeBreakdown = breakTree(root);
		if (!treeBreakdown.hasSubTrees()) {
			// the entity descriptor tree has only associations
			sq = buildParameterizedQuery(treeBreakdown.getMainTree(), false, criteria.getMainAdditionalColumns());
			StringBuilder sql = new StringBuilder(sq.getSql());
			appendWhereClause(sql, criteria);
			String q = String.format(COUNT_SQL, sql.toString());
			if (log.isTraceEnabled()) {
				long t1 = System.nanoTime();
				log.trace("buildCountQuery - Query built in " + Utils.nanosToMillis(t1 - t0)  + " ms. Query string length: " + q.length());
			}
			sq.setSql(q);
			return sq;
		} else {
			// the entity descriptor tree has at least one entity that contains a collection
			sq = buildParameterizedQuery(treeBreakdown.getMainTree(), false, criteria.getMainAdditionalColumns());
			StringBuilder sql = new StringBuilder(sq.getSql());
			List<Object> params = appendOutsideJoins(sql, criteria, treeBreakdown);
			sq.addMainParameters(params);
			appendWhereClause(sql, criteria);
			appendGroupByClause(sql, criteria, treeBreakdown);
			appendHavingClause(sql, criteria);			
			String q =  String.format(COUNT_SQL, sql.toString());
			if (log.isTraceEnabled()) {
				long t1 = System.nanoTime();
				log.trace("buildCountQuery - Query built in " + Utils.nanosToMillis(t1 - t0)  + " ms. Query string length: " + q.length());
			}
			sq.setSql(q);
			return sq;
		}
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

	
	// helper methods

	private static List<Object> appendJoinConditions(
			StringBuilder tables, Node<?> parentNode, Node<?> childNode,
			String ljcSeparator
			) {
		
		// this is lazy inited
		List<Object> parameters = null;
		
		QueryReady parentDescriptor = (QueryReady) parentNode.getValue();
		QueryReady childDescriptor = (QueryReady) childNode.getValue();
		
		String leftJoinColumn;
		String rightJoinColumn;
		
		// TODO: the left/inner join logic should be cleaner
		boolean forceLeftJoin;
		if (!StringUtils.hasLength(ljcSeparator)) {
			// method was called from #buildQuery(Node)
			// we are in a simple query, we allow for normal left/inner join logic
			ljcSeparator = ".";
			forceLeftJoin = false;
		} else {
			// method was called from  #getOutsideJoins(List<SubTree>, boolean)
			// we are in a query with details (pagination), we force left joins
			forceLeftJoin = true;
		}
		
		if (childDescriptor.getParentRelationType() == RelationType.MANY_TO_MANY) {
			if (!(childDescriptor instanceof ManyToManyQueryReady)) {
				throw new ClassCastException("Descriptor " + childDescriptor + " must be ManyToManyQueryReady.");
			}
			ManyToManyQueryReady mmChildDescriptor = (ManyToManyQueryReady) childDescriptor; 
			leftJoinColumn = mmChildDescriptor.getManyToManyLeftFkName();
			if (!StringUtils.hasText(leftJoinColumn)) {
				leftJoinColumn = parentDescriptor.getPkName();
			}
			rightJoinColumn = mmChildDescriptor.getManyToManyRightFkName();
			if (!StringUtils.hasText(rightJoinColumn)) {
				rightJoinColumn = childDescriptor.getPkName();
			}
			
			StringBuilder defaultLeftJoinCondition = new StringBuilder()
				.append(parentDescriptor.getTableAlias()).append(ljcSeparator).append(parentDescriptor.getPkName())
				.append(" = ")
				.append(mmChildDescriptor.getManyToManyTableAlias()).append(".").append(leftJoinColumn);
			
			tables
				.append(" left join ").append(mmChildDescriptor.getManyToManyTable()).append(" ").append(mmChildDescriptor.getManyToManyTableAlias())
				.append(" on ");
			String leftJoinConditionsOverride = mmChildDescriptor.getManyToManyLeftJoinConditionsOverride(); 
			if (StringUtils.hasText(leftJoinConditionsOverride)) {
				String joinConditions = leftJoinConditionsOverride
						.replace(QueryReady.PLACEHOLDER_DEFAULT_JOIN_CONDITION, defaultLeftJoinCondition)
						.replace(ManyToManyQueryReady.PLACEHOLDER_MTM_TABLE_ALIAS, mmChildDescriptor.getManyToManyTableAlias())
						.replace(QueryReady.PLACEHOLDER_PARENT_TABLE_ALIAS, parentDescriptor.getTableAlias());
				tables.append(joinConditions);
				if (!CollectionUtils.isEmpty(mmChildDescriptor.getManyToManyLeftJoinConditionsOverrideParams())) {
					if (parameters == null) {
						parameters = new ArrayList<>();
					}
					parameters.addAll(mmChildDescriptor.getManyToManyLeftJoinConditionsOverrideParams());
				}
			} else {
				tables.append(defaultLeftJoinCondition);
			}
			
			StringBuilder defaultRightJoinCondition = new StringBuilder()
				.append(mmChildDescriptor.getManyToManyTableAlias()).append(".").append(rightJoinColumn)
				.append(" = ")
				.append(childDescriptor.getTableAlias()).append(".").append(childDescriptor.getPkName());			
			
			tables
				.append(" left join ").append(childDescriptor.getTableName()).append(" ").append(childDescriptor.getTableAlias())
				.append(" on ");
			String rightJoinConditionsOverride = mmChildDescriptor.getManyToManyRightJoinConditionsOverride(); 
			if (StringUtils.hasText(rightJoinConditionsOverride)) {
				String joinConditions = rightJoinConditionsOverride
						.replace(QueryReady.PLACEHOLDER_DEFAULT_JOIN_CONDITION, defaultRightJoinCondition)
						.replace(ManyToManyQueryReady.PLACEHOLDER_MTM_TABLE_ALIAS, mmChildDescriptor.getManyToManyTableAlias())
						.replace(QueryReady.PLACEHOLDER_CHILD_TABLE_ALIAS, childDescriptor.getTableAlias());
				tables.append(joinConditions);
				if (!CollectionUtils.isEmpty(mmChildDescriptor.getManyToManyRightJoinConditionsOverrideParams())) {
					if (parameters == null) {
						parameters = new ArrayList<>();
					}
					parameters.addAll(mmChildDescriptor.getManyToManyRightJoinConditionsOverrideParams());
				}
			} else {
				tables.append(defaultRightJoinCondition);
			}
			return parameters;
		}
		
		tables.append(" ");
		
		if (childDescriptor.getParentRelationType() == RelationType.ONE_TO_ONE) {
			if (!forceLeftJoin && shouldApplyInnerJoinForAssociation(childNode)) {
				tables.append(JoinType.INNER.getKeyword());
			} else {
				tables.append(JoinType.LEFT.getKeyword());
			}
			leftJoinColumn = parentDescriptor.getPkName();
			rightJoinColumn = childDescriptor.getPkName();
		} else if (childDescriptor.getParentRelationType() == RelationType.ONE_TO_MANY) {
			if (!forceLeftJoin && shouldApplyInnerJoinForAssociation(childNode)) {
				tables.append(JoinType.INNER.getKeyword());
			} else {
				tables.append(JoinType.LEFT.getKeyword());
			}
			leftJoinColumn = childDescriptor.getFkName() != null ? childDescriptor.getFkName(): childDescriptor.getPkName();
			rightJoinColumn = childDescriptor.getPkName();
		} else if (childDescriptor.getParentRelationType() == RelationType.MANY_TO_ONE) {
			tables.append(JoinType.LEFT.getKeyword());
			leftJoinColumn = parentDescriptor.getPkName();
			rightJoinColumn = childDescriptor.getFkName() != null ? childDescriptor.getFkName() : parentDescriptor.getPkName();
		} else {
			throw new IllegalArgumentException("Unsupported relation type " + childDescriptor.getParentRelationType() + ".");
		}
		
		tables.append(" join ")
			.append(childDescriptor.getTableName()).append(" ").append(childDescriptor.getTableAlias())
			.append(" on ");
		
		StringBuilder defaultJoinCondition = new StringBuilder()
				.append(parentDescriptor.getTableAlias()).append(ljcSeparator).append(leftJoinColumn)
				.append(" = ")
				.append(childDescriptor.getTableAlias()).append(".").append(rightJoinColumn);
		String joinConditionsOverride = childDescriptor.getJoinConditionsOverride(); 
		if (StringUtils.hasText(joinConditionsOverride)) {
			String joinConditions = joinConditionsOverride
					.replace(QueryReady.PLACEHOLDER_DEFAULT_JOIN_CONDITION, defaultJoinCondition)
					.replace(QueryReady.PLACEHOLDER_CHILD_TABLE_ALIAS, childDescriptor.getTableAlias())
					.replace(QueryReady.PLACEHOLDER_PARENT_TABLE_ALIAS, parentDescriptor.getTableAlias());
			tables.append(joinConditions);
			if (!CollectionUtils.isEmpty(childDescriptor.getJoinConditionsOverrideParams())) {
				if (parameters == null) {
					parameters = new ArrayList<>();
				}
				parameters.addAll(childDescriptor.getJoinConditionsOverrideParams());
			}
		} else {
			tables.append(defaultJoinCondition);
		}
		return parameters == null ? Collections.emptyList() : parameters;
	}

	static boolean shouldApplyInnerJoinForAssociation(Node<?> node) {
		QueryReady descriptor = (QueryReady) node.getValue();
		while (!node.isRoot()) {
			if (descriptor.getParentJoinType() != JoinType.INNER) {
				return false;
			}
			if (descriptor.isMany()) {
				return false;
			}
			node = node.getParent();
			descriptor = (QueryReady) node.getValue();
		}
		return true;
	}

	
	private static boolean isTargetInList(Node<?> target, List<SubTree> subTrees) {
		if (target == null || subTrees == null) {
			return false;
		}
		for (SubTree subTree: subTrees) {
			List<?> descendants = subTree.getSubTree().getDescendants();
			for (Object descendant: descendants) {
				if (descendant == target) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static void prepareColumnName(StringBuilder sb, QueryReady childDescriptor, String colName) {
		sb.append(childDescriptor.getTableAlias()).append(".");
		if (Character.isLetter(colName.charAt(0))) {
			sb.append(colName);	
		} else {
			sb.append("\"").append(colName).append("\"");
		}
	}
	
	private static void addColumnName(StringBuilder fields, QueryReady childDescriptor, String colName, boolean withAlias) {
		if (!StringUtils.hasText(colName)) {
			log.warn("addColumnName - Empty column found in the columns list for descriptor " + childDescriptor + ". The column will be ignored.");
			return;
		}
		
		UnaryOperator<String> formula = childDescriptor.getFormula(colName);
		if (formula == null) {
			prepareColumnName(fields, childDescriptor, colName);
		} else {
			StringBuilder finalColName = new StringBuilder();
			prepareColumnName(finalColName, childDescriptor, colName);
			fields.append(formula.apply(finalColName.toString()));
		}
		
		if (withAlias) {
			fields
				.append(" ")
				.append(childDescriptor.getTableAlias()).append(childDescriptor.getColumnAliasSeparator()).append(colName)
			;
		}
		fields.append(FIELD_SEPARATOR);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static String getColumnNamesCsv(Node<?> root, final boolean withAlias) {
		if (root == null) {
			return "";
		}
		final StringBuilder fields = new StringBuilder();
		root.traverse(new NodeHandler() {
				@Override
				public void handleNode(Node childNode) {
					if (!isQueryReady(childNode)) {
						return;
					}
					QueryReady childDescriptor = (QueryReady) childNode.getValue();
					Collection<String> colNames = childDescriptor.getColumnNames();
					for (String colName: colNames) {
						addColumnName(fields, childDescriptor, colName, withAlias);
					}
				}
			}
		);
		if (StringUtils.hasLength(fields)) {
			fields.delete(fields.length() - FIELD_SEPARATOR.length(), fields.length());
		}
		return fields.toString();
	}
	
	private static String getColumnNamesCsv(List<SubTree> subTrees, boolean withAlias) {
		if (subTrees == null) {
			return "";
		}
		StringBuilder fields = new StringBuilder();
		for (SubTree subTree: subTrees) {
			fields.append(getColumnNamesCsv(subTree.getSubTree(), withAlias))
				.append(FIELD_SEPARATOR);
		}
		if (StringUtils.hasLength(fields)) {
			fields.delete(fields.length() - FIELD_SEPARATOR.length(), fields.length());
		}
		return fields.toString();
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ParameterizedQuery getOutsideJoins(List<SubTree> subTrees, final boolean forceDotAsLjcSeparator) {
		if (subTrees == null) {
			return new ParameterizedQuery();
		}
		ParameterizedQuery sq = new ParameterizedQuery();
		final StringBuilder tables = new StringBuilder();
		for (SubTree subTree: subTrees) {
			final Node<?> rootParent = subTree.getParent();
			Node<?> child = subTree.getSubTree();
			child.traverse(new NodeHandler() {
				@Override
				public void handleNode(Node childNode) {
					if (!isQueryReady(childNode)) {
						return;
					}
					
					Node<?> parentNode;
					QueryReady parentDescriptor;
					if (childNode.isRoot()) {
						parentNode = rootParent;
					} else {
						parentNode = childNode.getParent();
					}
					parentDescriptor = (QueryReady) parentNode.getValue();
					
					String ljcSeparator;
					if (forceDotAsLjcSeparator) {
						ljcSeparator = ".";
					} else {
						ljcSeparator = childNode.isRoot() ? parentDescriptor.getColumnAliasSeparator() : ".";
					}
					List<Object> params = appendJoinConditions(tables, parentNode, childNode, ljcSeparator);
					sq.addMainParameters(params);
				}
			});
			
		}
		sq.setSql(tables.toString());
		return sq;
	}

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static TreeBreakdown breakTree(Node<?> root) {
		Assert.assertNotNull(root, "root");
		// copy the structure of the tree parameter
		Node<?> rootCopy = TreeUtils.copy(root);
		
		// select nodes that contain collections of entities
		final List<SubTree> subTrees = new ArrayList<SubTree>();
		rootCopy.traverse(new NodeHandler() {
			@Override
			public void handleNode(Node node) {
				if (node.isRoot() ) {
					return;
				}
				// this if is to accommodate the proxy EntityDescriptors and the CacheEntityDescriptor
				// that do not participate in queries
				if (!isQueryReady(node)) {
					return;
				}
				QueryReady qr = (QueryReady) node.getValue();
				if (qr.isMany() && !qr.isForceManyAsOneInPaginatedQueries() 
						&& !isTargetInList(node, subTrees)) {
					subTrees.add(new SubTree(node.getParent(), node));
				}
			}
		});
		
		// disconnect the subtrees from the main tree
		for (SubTree subTree: subTrees) {
			Node parent = subTree.getParent();
			parent.removeChild(subTree.getSubTree());
		}
		
		TreeBreakdown tbd = new TreeBreakdown(rootCopy, subTrees);
		
		if (log.isTraceEnabled()) {
			if (tbd.hasSubTrees()) {
				log.trace("breakTree - The descriptor tree references entities that contain collections.");
			} else {
				log.trace("breakTree - The descriptor tree references only entities that have associations.");
			}
		}
		
		return tbd;
	}
	
	

	private static List<Object> appendOutsideJoins(StringBuilder query, QueryCriteria criteria, TreeBreakdown treeBreakdown) {
		List<SubTree> subTrees = treeBreakdown.getSubTrees();
		if (criteria.isUseGroupByOnMainQuery()) {
			ParameterizedQuery outsideJoins = getOutsideJoins(subTrees, true); 
			query.append(outsideJoins.getSql());
			return outsideJoins.getMainParameters();
		}
		return Collections.emptyList();
	}

	
	private static void appendWhereClause(StringBuilder query, QueryCriteria criteria) {
		String mainWhereClause = criteria.getMainWhereClause();
		if (StringUtils.hasText(mainWhereClause)) {
			query.append(" where ").append(mainWhereClause);
		}
	}
	
	private static void appendHavingClause(StringBuilder query, QueryCriteria criteria) {
		String mainHavingClause = criteria.getMainHavingClause();
		if (StringUtils.hasText(mainHavingClause)) {
			query.append(" having ").append(mainHavingClause);
		}
	}
	
	
	private static void appendOrderByClause(StringBuilder query, QueryCriteria criteria) {
		String orderByClause = criteria.getMainOrderByClauseSafe();
		if (StringUtils.hasText(orderByClause)) {
			query.append(" order by ").append(orderByClause);
		}
	}
	

	private static void appendGroupByClause(StringBuilder query, QueryCriteria criteria, TreeBreakdown treeBreakdown) {
		String[] groupByAdditionalColumns = criteria.getMainGroupByAdditionalColumns();
		if (criteria.isUseGroupByOnMainQuery()) {
			query.append(" group by ");
			if (groupByAdditionalColumns != null) {
				for (String c: groupByAdditionalColumns) {
					if (StringUtils.hasText(c)) {
						query.append(c).append(FIELD_SEPARATOR);
					}
				}
			}
			query.append(getColumnNamesCsv(treeBreakdown.getMainTree(), false));
		}
	}
	
	private static String[] combineAdditionalColumns(SqlTemplates templates, QueryCriteria criteria) {
		String[] userCols = criteria.getMainAdditionalColumns();
		String[] frameworkCols = templates.getPaginationAdditionalColumns(criteria.getMainOrderByClauseSafe());
		String[] finalCols = new String[userCols.length + frameworkCols.length];
		System.arraycopy(frameworkCols, 0, finalCols, 0, frameworkCols.length);
		System.arraycopy(userCols, 0, finalCols, frameworkCols.length, userCols.length);
		return finalCols;
	}
	
	private static void logTreeBreakdown(TreeBreakdown treeBreakdown) {
		if (log.isTraceEnabled()) {
			Node<?> rootCopy = treeBreakdown.getMainTree();
			List<SubTree> subTrees = treeBreakdown.getSubTrees();
			log.trace("logTreeBreakdown - EntityDescriptor main tree:\n" + rootCopy.toStringAsTree());
			StringBuilder sb = new StringBuilder();
			for (SubTree subTree: subTrees) {
				sb.append(subTree.getSubTree().toStringAsTree());			
			}
			log.trace("logTreeBreakdown - EntityDescriptor subtrees:\n" + sb);
		}
	}
	
	private static boolean isQueryReady(Node<?> node) {
		if (!(node.getValue() instanceof QueryReady)) {
			if (!node.isLeaf()) {
				throw new IllegalArgumentException("Non QueryReady object " + node.getValue() 
						+ " has children. This is illegal.");
			}
			return false;
		}
		return true;
	}
	
	
	// private helper classes
	
	private static class SubTree {
		private final Node<?> parent;
		private final Node<?> subTree;
		
		public SubTree(Node<?> parent, Node<?> subTree) {
			this.parent = parent;
			this.subTree = subTree;
		}

		public Node<?> getParent() {
			return parent;
		}

		public Node<?> getSubTree() {
			return subTree;
		}
	}
	
	static class TreeBreakdown {
		private final Node<?> mainTree;
		private final List<SubTree> subTrees;
		
		public TreeBreakdown(Node<?> mainTree, List<SubTree> subTrees) {
			this.mainTree = mainTree;
			this.subTrees = subTrees;
		}
		
		public Node<?> getMainTree() {
			return mainTree;
		}
		
		public List<SubTree> getSubTrees() {
			return subTrees;
		}
		
		public boolean hasSubTrees() {
			return !subTrees.isEmpty();
		}
	}

}
