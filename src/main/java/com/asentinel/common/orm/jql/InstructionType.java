package com.asentinel.common.orm.jql;

import com.asentinel.common.orm.EntityDescriptor;

/**
 * Enum that defines all supported {@link SqlBuilder} instructions.
 * 
 * @see SqlBuilder
 * @see SqlBuilder#compile()
 * @see SqlBuilder#compile(com.asentinel.common.collections.tree.Node)
 * @see Instructions#compile()
 * 
 * @author Razvan Popian
 */
enum InstructionType {
	/**
	 * Specifies an initial select query without a where, order by clauses.
	 */
	INITIAL_QUERY,
	
	FROM_QUERY,	
	
	PAGED_INITIAL_QUERY,
	
	PAGED_MAIN_WHERE,
	
	PAGED_MAIN_ORDER_BY,	
	
	PAGED_SECONDARY_WHERE,
	
	PAGED_USE_GROUP_BY,
	
	PAGED_HAVING,
	
	/**
	 * Sets the path to a certain node in the {@link EntityDescriptor} tree. The path
	 * set by this instruction is used by all subsequent column related instructions to 
	 * determine the table alias. 
	 */
	PATH,
	
	/**
	 * Sets the root of the {@link EntityDescriptor} tree as the current path. The path
	 * set by this instruction is used by all subsequent column related instructions to 
	 * determine the table alias. 
	 */
	PATH_ROOT,
	
	/**
	 * Sets the separator to be used by all subsequent column related instructions. By default
	 * this is the <code>"."</code>, but there are situations when this needs to change.
	 */
	SEPARATOR,
	
	/**
	 * Plain string that will get added to the compiled SQL. 
	 */
	STRING,
	
	/**
	 * The name of the primary key will get added to the compiled SQL.
	 */
	ID_COLUMN,
	
	/**
	 * The primary key alias will get added to the compiled SQL. This is for
	 * scenarios where the user controls the columns in the select clause.
	 */
	ID_ALIAS,

	/**
	 * The specified column name will get added to the compiled SQL.
	 */
	COLUMN,

	/**
	 * The specified column alias will get added to the compiled SQL. This is for
	 * scenarios where the user controls the columns in the select clause.
	 */
	COLUMN_ALIAS,

	/**
	 * Adds a parameter to the compiled SQL.
	 */
	PARAM,
	
	/**
	 * Adds a stringparameter to the compiled SQL.
	 */
	STRING_PARAM, 

	/**
	 * Adds an array parameter to the compiled SQL.
	 */
	ARRAY_PARAM,
	
	/**
	 * The plain SQL string specified and any parameters will get added to the compiled SQL.
	 */
	SQL,
	
	/**
	 * The name of the table will get added to the compiled SQL.
	 */
	TABLE
}
