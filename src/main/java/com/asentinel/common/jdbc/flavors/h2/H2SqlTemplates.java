package com.asentinel.common.jdbc.flavors.h2;

import org.springframework.util.StringUtils;

import com.asentinel.common.jdbc.flavors.SqlTemplates;
import com.asentinel.common.util.Assert;

/**
 * @since 1.70.0
 * @author Razvan Popian
 */
public class H2SqlTemplates implements SqlTemplates {
	
	/*
	 * Note that that limit and offset keywords are reversed compared to the postgres flavor
	 */
	
	private final static String SQL_ONLY_ASSOCIATIONS = "%s";
	
	private final static String SQL_AT_LEAST_ONE_COLLECTION  
		= "select main.* %s from ( %s ) main" 
		+ " %s %s order by " + ROW_INDEX_COL;
		// 2 %s on the last row: join with the details tables, secondary where clause
	
	private final static String SQL_FIRST_ROW_ONLY = "select " + ROW_INDEX_COL + " from ( %s ) inner_select limit 1 offset 0";
	
	// pagination query constants
	private final static String PARAM_OFFSET = "param_offset";
	private final static String PARAM_LIMIT = "param_limit";
	
	private final static String ROW_INDEX_COL_TEMPLATE = "row_number() over (order by %s) - 1 " + ROW_INDEX_COL;
	
	private final static String PAGINATION_SQL_ONLY_ASSOCIATIONS 
		= "select * from (%s limit ? offset ?) main %s";

	private final static String PAGINATION_SQL_ONLY_ASSOCIATIONS_NAMED
		= "select * from (%s limit :" + PARAM_LIMIT + " offset :" + PARAM_OFFSET + ") main %s";

	private final static String PAGINATION_SQL_AT_LEAST_ONE_COLLECTION 
		= "select main.* %s from ( %s limit ? offset ?) main" 
		+ " %s %s order by " + ROW_INDEX_COL;
		// 2 %s on the last row: join with the details tables, secondary where clause

	private final static String PAGINATION_SQL_AT_LEAST_ONE_COLLECTION_NAMED
		= "select main.* %s from ( %s limit :" + PARAM_LIMIT + " offset :" + PARAM_OFFSET + ") main" 
		+ " %s %s order by " + ROW_INDEX_COL;
		// 2 %s on the last row: join with the details tables, secondary where clause
	
	
	private final static String SQL_SEQ_NEXT_VAL = "nextval('%s')";
	
	// private final static String SQL_IN_ARRAY_SELECT = " in (select * from unnest(?))";	

	@Override
	public String getSqlOnlyAssociations() {
		return SQL_ONLY_ASSOCIATIONS;
	}

	@Override
	public String getSqlAtLeastOneCollection() {
		return SQL_AT_LEAST_ONE_COLLECTION;
	}
	
	@Override
	public String getSqlForFirstRowOnly() {
		return SQL_FIRST_ROW_ONLY;
	}

	@Override
	public String getPaginationSqlOnlyAssociations() {
		return PAGINATION_SQL_ONLY_ASSOCIATIONS;
	}

	@Override
	public String getPaginationSqlAtLeastOneCollection() {
		return PAGINATION_SQL_AT_LEAST_ONE_COLLECTION;
	}
	
	@Override
	public String getPaginationNamedParam1() {
		return PARAM_OFFSET;
	}

	@Override
	public String getPaginationNamedParam2() {
		return PARAM_LIMIT;
	}

	@Override
	public String getPaginationSqlOnlyAssociationsNamed() {
		return PAGINATION_SQL_ONLY_ASSOCIATIONS_NAMED;
	}

	@Override
	public String getPaginationSqlAtLeastOneCollectionNamed() {
		return PAGINATION_SQL_AT_LEAST_ONE_COLLECTION_NAMED;
	}
	
	@Override
	public String[] getPaginationAdditionalColumns(String mainOrderByString) {
		// if we don't have order columns we can not generate the row index
		if (!StringUtils.hasText(mainOrderByString)) {
			throw new IllegalArgumentException("We can not create the query because no order by columns where specified.");
		}
		Assert.assertNotEmpty(mainOrderByString, "mainOrderByString");
		String[] cols = new String[1];
		cols[0] = String.format(ROW_INDEX_COL_TEMPLATE, mainOrderByString);
		return cols;
	}

	@Override
	public Long[] applyRangeTransformation(long beginIndex, long endIndex) {
		return new Long[] {
			endIndex <= beginIndex ? 0 : endIndex - beginIndex,
			beginIndex
		};
	}
	
	@Override
	public String getSqlForNextSequenceVal(String sequenceName) {
		Assert.assertNotEmpty(sequenceName, "sequenceName");
		return SQL_SEQ_NEXT_VAL;
	}
	
	@Override
	public String getSqlForInArray() {
		throw new UnsupportedOperationException("The " + getClass().getSimpleName() + " does not support array statements yet.");
	}
	
	@Override
	public String getSqlForCaseSensitiveColumn(String tableAlias, String separator, String column) {
		throw new UnsupportedOperationException("The " + getClass().getSimpleName() + " does not support case sensitive columns yet.");
	}
	
	@Override
	public String getSqlForCaseInsensitiveColumn(String tableAlias, String separator, String column) {
		throw new UnsupportedOperationException("The " + getClass().getSimpleName() + " does not support case insensitive columns yet.");
	}

	@Override
	public String getSqlForUpsert(String sqlInsert, String sqlUpdate, Object ... hints) {
		throw new UnsupportedOperationException("The " + getClass().getSimpleName() + " does not support upsert statements yet.");
	}


	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
