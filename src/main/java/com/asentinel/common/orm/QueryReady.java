package com.asentinel.common.orm;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.orm.query.SqlFactory;

/**
 * Interface implemented usually by {@link EntityDescriptor} subclasses to show
 * that they can be used to create SQL queries using the query building methods
 * found in the {@link SqlFactory} interface. 
 * 
 * <br><br>
 * 
 * Important: This is a framework interface, it should not be used in application code.
 * 
 * @see SqlFactory
 * @see SqlFactory#buildQuery(Node)
 * @see SqlFactory#buildQuery(QueryCriteria)
 * @see SqlFactory#buildPaginatedQuery(QueryCriteria)
 * 
 * @author Razvan Popian
 */
public interface QueryReady {
	
	/**
	 * Placeholder to be used in the string returned by the {@link #getJoinConditionsOverride()} method.<br>
	 * This placeholder will be replaced with the default join condition.
	 */
	final static String PLACEHOLDER_DEFAULT_JOIN_CONDITION = "#{defaultJoinCondition}";
	
	/**
	 * Placeholder to be used in the string returned by the {@link #getJoinConditionsOverride()} method.<br>
	 * This placeholder will be replaced with the parent table alias.
	 */
	final static String PLACEHOLDER_PARENT_TABLE_ALIAS = "#{parentTableAlias}";

	/**
	 * Placeholder to be used in the string returned by the {@link #getJoinConditionsOverride()} method.<br>
	 * This placeholder will be replaced with the child table alias.
	 */
	final static String PLACEHOLDER_CHILD_TABLE_ALIAS = "#{childTableAlias}";
	
	/**
 	 * @return relation type of this descriptor to its parent.
 	 * 
 	 * @see RelationType
	 */
	RelationType getParentRelationType();
	
	/**
	 * @return the type of SQL join (INNER, LEFT or RIGHT) to be used
	 * 			for joining the table represented by this descriptor to
	 * 			the table represented by its parent descriptor. Clients of this
	 * 			interface should assume that the parent table is on the left side
	 * 			of the join and the child table is on the right side.
	 * 
	 * @see JoinType  
	 */
	JoinType getParentJoinType();
	
	/**
	 * @return the name of the primary key, implementations
	 * 			will either return a constant or locate the primary
	 * 			key name in the target object using annotations or other
	 * 			methods
	 * 
	 * @see SqlFactory#buildQuery(Node)
	 */
	String getPkName();
	
	/**
	 * @return the name of the foreign key, if <code>null</code> the {@link SqlFactory#buildQuery(Node)}
	 * 		method will assume it has the same name as the primary key.
	 */
	String getFkName();
	
	/**
	 * @return the name of the table this descriptor
	 * 			is associated with.
	 * 
	 * @see SqlFactory#buildQuery(Node)
	 */
	String getTableName();
	
	/**
	 * @return the alias of the table from which
	 * 			data will be fetched for this descriptor.
	 */
	String getTableAlias();
	
	/**
	 * @return the column alias separator, the convention is to use 
	 * 			the "_" character, but this is not an absolute requirement.
	 */
	String getColumnAliasSeparator();
	
	/**
	 * @return the collection of columns that will be used in the SELECT clause.
	 * 
	 * @see #getFormula(String)
	 */
	Collection<String> getColumnNames();
	
	/**
	 * @param columnName the name of the table column as returned by the method
	 *                   {@code #getColumnNames()}
	 * @return a {@code UnaryOperator} that will transform the column name into a
	 *         formula. For example a column called {@code Total}  could be turned into the
	 *         formula {@code "Total - 10"}.
	 *         
	 * @see #getColumnNames()
	 */
	default UnaryOperator<String> getFormula(String columnName) {
		return null;
	}

	/**
	 * @return a SQL snippet defining the join conditions between the parent and child. 
	 * 		It returns <code>null</code> by default, indicating no override. It can contain
	 * 		question mark placeholders for parameters and each question mark should correspond
	 * 		to a parameter in the list returned by {@code #getJoinConditionsOverrideParams()}. 
	 * 
	 * @see #getJoinConditionsOverrideParams()
	 * @see #PLACEHOLDER_DEFAULT_JOIN_CONDITION
	 * @see #PLACEHOLDER_PARENT_TABLE_ALIAS
	 * @see #PLACEHOLDER_CHILD_TABLE_ALIAS
	 */
	default String getJoinConditionsOverride() {
		return null;
	}

	/**
	 * @return the parameters used in the join condition override string. Used 
	 * 		only if the {@link #getJoinConditionsOverride()} method returns a not empty string.<br>
	 * 		Each parameter must correspond to a question mark in the string returned 
	 * 		by {@code #getJoinConditionsOverride()}.<br>
	 * 		It returns an empty list by default.
	 * 
	 * @see #getJoinConditionsOverride()
	 */
	default List<Object> getJoinConditionsOverrideParams() {
		return Collections.emptyList();
	}
	
	// TODO: we may want to add a #isOne() method to depict "one" relationships (single item associations)

	/**
	 * @return {@code true} if this {@code QueryReady} represents a "many"
	 *         relationship ({@code RelationType#MANY_TO_ONE} or
	 *         {@code RelationType#MANY_TO_MANY}), {@code false} otherwise. Usually
	 *         a collection of items is associated with the parent if this method
	 *         returns {@code true}.
	 */
	default boolean isMany() {
		return getParentRelationType() == RelationType.MANY_TO_MANY || getParentRelationType() == RelationType.MANY_TO_ONE;
	}
	
	/**
	 * Paginated queries that include at least one "many" node (a collection node)
	 * are created differently from the ones that contain only associations. There
	 * are scenarios where we would like a paginated query with collections to be
	 * created like a standard associations only query. One such scenario is when we
	 * are sure that the collection will always contain at most one element. If this
	 * method returns {@code true}, the query building engine ({@link SqlFactory}) is
	 * forced to treat the node containing this {@code QueryReady} as a simple
	 * association node (as if the parent relation type is
	 * {@code RelationType#ONE_TO_MANY}).
	 * 
	 * @return {@code true} to force this {@code QueryReady} as an association,
	 *         {@code false} to deal with it normally. The default is {@code false}.
	 *         
	 * @see SqlFactory
	 */
	default boolean isForceManyAsOneInPaginatedQueries() {
		return false;
	}

}
