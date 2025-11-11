package com.asentinel.common.orm;

import java.util.Collections;
import java.util.List;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.orm.query.SqlFactory;

/**
 * Extension of the {@link QueryReady} interface used if this {@link EntityDescriptor}
 * links to its parent through another table (many to many relationship).
 * 
 * <br><br>
 * 
 * Important: This is a framework interface, it should not be used in application code.
 * 
 * @see QueryReady
 * @see SqlFactory
 * @see SqlFactory#buildQuery(Node)
 * @see RelationType
 * 
 * @author Razvan Popian
 */
public interface ManyToManyQueryReady extends QueryReady {
	
	/**
	 * Placeholder to be used in the string returned by the {@link #getManyToManyLeftJoinConditionsOverride()} and 
	 * {@link #getManyToManyRightJoinConditionsOverride()} methods.<br>
	 * This placeholder will be replaced with the table alias of the link table.
	 */
	String PLACEHOLDER_MTM_TABLE_ALIAS = "#{manyToManyTableAlias}";

	/**
	 * @return the name of the table that links the table
	 * 			represented by this entity to the table
	 * 			represented by the parent entity.
	 */
	String getManyToManyTable();

	/**
	 * @return the alias of the link table in the many to many relationship.
	 */
	String getManyToManyTableAlias();

	/**
	 * @return the name of the column in the link table that connects
	 * 			the link table to the parent entity table. If this is 
	 * 			{@code null} the {@link SqlFactory#buildQuery(Node)} method will use the same 
	 * 			name as the name of the primary key in the parent entity table.
	 */
	String getManyToManyLeftFkName();
	
	/**
	 * @return the name of the column in the link table that connects
	 * 			the link table to this entity table. If this is {@code null}
	 * 			the {@link SqlFactory#buildQuery(Node)} method will use the same 
	 * 			name as the name of the primary key in this entity table.
	 */
	String getManyToManyRightFkName();
	
	/**
	 * @return a SQL snippet defining the join conditions between the parent and the link table. 
	 * 		It returns <code>null</code> by default, indicating no override. It can contain
	 * 		question mark placeholders for parameters and each question mark should correspond
	 * 		to a parameter in the list returned by {@code #getManyToManyLeftJoinConditionsOverrideParams()}. 
	 * 
	 * @see #getManyToManyLeftJoinConditionsOverrideParams()
	 * @see #PLACEHOLDER_DEFAULT_JOIN_CONDITION
	 * @see #PLACEHOLDER_PARENT_TABLE_ALIAS
	 * @see #PLACEHOLDER_MTM_TABLE_ALIAS
	 */
	default String getManyToManyLeftJoinConditionsOverride() {
		return getJoinConditionsOverride();
	}
	
	/**
	 * @return the parameters used in the left join condition override string. Used 
	 * 		only if the {@link #getManyToManyLeftJoinConditionsOverride()} method returns a not empty string.<br>
	 * 		Each parameter must correspond to a question mark in the string returned 
	 * 		by {@code #getManyToManyLeftJoinConditionsOverride()}.<br>
	 * 		It returns an empty list by default.
	 * 
	 * @see #getManyToManyLeftJoinConditionsOverride()
	 */
	default List<Object> getManyToManyLeftJoinConditionsOverrideParams() {
		return getJoinConditionsOverrideParams();
	}

	/**
	 * @return a SQL snippet defining the join conditions between the the link table and the child. 
	 * 		It returns <code>null</code> by default, indicating no override. It can contain
	 * 		question mark placeholders for parameters and each question mark should correspond
	 * 		to a parameter in the list returned by {@code #getManyToManyRightJoinConditionsOverrideParams()}. 
	 * 
	 * @see #getManyToManyRightJoinConditionsOverrideParams()
	 * @see #PLACEHOLDER_DEFAULT_JOIN_CONDITION
	 * @see #PLACEHOLDER_CHILD_TABLE_ALIAS
	 * @see #PLACEHOLDER_MTM_TABLE_ALIAS
	 */
	default String getManyToManyRightJoinConditionsOverride() {
		return null;
	}

	/**
	 * @return the parameters used in the right join condition override string. Used 
	 * 		only if the {@link #getManyToManyRightJoinConditionsOverride()} method returns a not empty string.<br>
	 * 		Each parameter must correspond to a question mark in the string returned 
	 * 		by {@code #getManyToManyRightJoinConditionsOverride()}.<br>
	 * 		It returns an empty list by default.
	 * 
	 * @see #getManyToManyRightJoinConditionsOverride()
	 */
	default List<Object> getManyToManyRightJoinConditionsOverrideParams() {
		return Collections.emptyList();
	}

}
