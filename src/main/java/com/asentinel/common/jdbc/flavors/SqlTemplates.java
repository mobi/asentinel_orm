package com.asentinel.common.jdbc.flavors;


/**
 * Strategy interface to isolate the SQL code that is database specific. 
 * 
 * @author Razvan Popian
 */
public interface SqlTemplates {
	
	String ROW_INDEX_COL = "RowIndex";
	
	String[] NO_COLS = new String[0];
	
	String UPSERT_INSERT_PLACEHOLDER = "#{insert}";
	String UPSERT_UPDATE_PLACEHOLDER = "#{update}";
	
	String getSqlForFirstRowOnly();

	String getSqlOnlyAssociations();
	
	String getSqlAtLeastOneCollection();

	String getPaginationSqlOnlyAssociations();

	String getPaginationSqlAtLeastOneCollection();

	String getPaginationNamedParam1();
	
	String getPaginationNamedParam2();
	
	String getPaginationSqlOnlyAssociationsNamed();
	
	String getPaginationSqlAtLeastOneCollectionNamed();
	
	default String[] getPaginationAdditionalColumns(String mainOrderByString) {
		return NO_COLS;
	}

	default boolean isSecondaryWhereStarted() {
		return false;
	}
	
	default Long[] applyRangeTransformation(long beginIndex, long endIndex) {
		return new Long[] {beginIndex, endIndex};
	}
	
	String getSqlForNextSequenceVal(String sequenceName);
	
	String getSqlForInArray();
	
	String getSqlForCaseSensitiveColumn(String tableAlias, String separator, String column);
	
	String getSqlForCaseInsensitiveColumn(String tableAlias, String separator, String column);
	
	String getSqlForUpsert(String sqlInsert, String sqlUpdate, Object ... hints);
}
