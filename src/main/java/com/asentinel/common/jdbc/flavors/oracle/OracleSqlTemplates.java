package com.asentinel.common.jdbc.flavors.oracle;

import com.asentinel.common.jdbc.flavors.SqlTemplates;
import com.asentinel.common.orm.QueryCriteria;
import com.asentinel.common.util.Assert;


public class OracleSqlTemplates implements SqlTemplates {
	
	/**
	 * @seee {@link #buildQuery(QueryCriteria)}
	 */
	public final static String PARAM_ROW_INDEX_BEGIN = "rowIndexBegin";
	
	/**
	 * @seee {@link #buildQuery(QueryCriteria)}
	 */
	public final static String PARAM_ROW_INDEX_END = "rowIndexEnd";

	private final static String SQL_ONLY_ASSOCIATIONS 
		= "select main.* from (select rownum - 1 " + ROW_INDEX_COL + ", indexed.* from ( %s ) indexed) main " 
		+ "order by " + ROW_INDEX_COL;

	
	private final static String SQL_AT_LEAST_ONE_COLLECTION 
		= "select main.* %s from (select rownum - 1 " + ROW_INDEX_COL + ", indexed.* from ( %s ) indexed) main %s " 
		+ "%s order by " + ROW_INDEX_COL;
	
	private final static String SQL_FIRST_ROW_ONLY = "select "+ ROW_INDEX_COL + " from ( %s ) where rownum = 1";
	

	
	// pagination query constants
	private final static String PAGINATION_SQL_ONLY_ASSOCIATIONS 
		= "select main.* from (select rownum - 1 " + ROW_INDEX_COL + ", indexed.* from ( %s ) indexed) main " 
		+ "where " + ROW_INDEX_COL + " >= ? and " + ROW_INDEX_COL + " < ? "
		+ "%s order by " + ROW_INDEX_COL;
	
	private final static String PAGINATION_SQL_ONLY_ASSOCIATIONS_NAMED 
		= "select main.* from (select rownum - 1 " + ROW_INDEX_COL + ", indexed.* from ( %s ) indexed) main " 
		+ "where " + ROW_INDEX_COL + " >= :" + PARAM_ROW_INDEX_BEGIN + " and " + ROW_INDEX_COL + " < :" + PARAM_ROW_INDEX_END 
		+ " %s order by " + ROW_INDEX_COL;
	
	private final static String PAGINATION_SQL_AT_LEAST_ONE_COLLECTION
		= "select main.* %s from (select rownum - 1 " + ROW_INDEX_COL + ", indexed.* from ( %s ) indexed) main %s " 
		+ "where main." + ROW_INDEX_COL + " >= ? and main." + ROW_INDEX_COL + " < ? %s order by " + ROW_INDEX_COL;

	
	private final static String PAGINATION_SQL_AT_LEAST_ONE_COLLECTION_NAMED 
		= "select main.* %s from (select rownum - 1 " + ROW_INDEX_COL + ", indexed.* from ( %s ) indexed) main %s " 
		+ "where main." + ROW_INDEX_COL + " >= :" + PARAM_ROW_INDEX_BEGIN + " and main." + ROW_INDEX_COL + " < :" + PARAM_ROW_INDEX_END
		+ " %s order by " + ROW_INDEX_COL;
	
	
	private final static String SQL_SEQ_NEXT_VAL = "%s.nextval";
	private final static String SQL_IN_ARRAY_SELECT = " in (select * from table(?))";	
	private final static String SQL_NLS_SORT = "nlssort(%s%s%s, 'NLS_SORT=BINARY_CI')";
	
	
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
	public String getPaginationSqlOnlyAssociationsNamed() {
		return PAGINATION_SQL_ONLY_ASSOCIATIONS_NAMED;
	}

	@Override
	public String getPaginationSqlAtLeastOneCollection() {
		return PAGINATION_SQL_AT_LEAST_ONE_COLLECTION;
	}
	
	@Override
	public String getPaginationSqlAtLeastOneCollectionNamed() {
		return PAGINATION_SQL_AT_LEAST_ONE_COLLECTION_NAMED;
	}
	
	@Override
	public boolean isSecondaryWhereStarted() {
		return true;
	}
	

	@Override
	public String getPaginationNamedParam1() {
		return PARAM_ROW_INDEX_BEGIN;
	}

	@Override
	public String getPaginationNamedParam2() {
		return PARAM_ROW_INDEX_END;
	}

	@Override
	public String getSqlForNextSequenceVal(String sequenceName) {
		Assert.assertNotEmpty(sequenceName, "sequenceName");
		return String.format(SQL_SEQ_NEXT_VAL, sequenceName);
	}
	
	@Override
	public String getSqlForInArray() {
		return SQL_IN_ARRAY_SELECT;
	}
	
	@Override
	public String getSqlForCaseSensitiveColumn(String tableAlias, String separator, String column) {
		return new StringBuilder(tableAlias.length() + separator.length() + column.length())	
			.append(tableAlias).append(separator).append(column)
			.toString();
	}
	
	@Override
	public String getSqlForCaseInsensitiveColumn(String tableAlias, String separator, String column) {
		return String.format(SQL_NLS_SORT, tableAlias, separator, column);
	}
	
	@Override
	public String getSqlForUpsert(String sqlInsert, String sqlUpdate, Object ... hints) {
		throw new UnsupportedOperationException("The upsert functionality is not implemented for Oracle yet. "
				+ "If you really, really need it go ahead and get your hands dirty :)");
	}

	@Override
	public String toString() {
		return "OracleSqlTemplates";
	}
}
