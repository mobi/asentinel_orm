package com.asentinel.common.jdbc.flavors;


/**
 * Strategy interface to isolate the SQL code that is database specific. 
 * 
 * @author Razvan Popian
 */
public interface SqlTemplates {
	
	public final static String ROW_INDEX_COL = "RowIndex";
	
	public final static String[] NO_COLS = new String[0];
	
	public final static String UPSERT_INSERT_PLACEHOLDER = "#{insert}";
	public final static String UPSERT_UPDATE_PLACEHOLDER = "#{update}";
	
	public String getSqlForFirstRowOnly();

	public String getSqlOnlyAssociations();
	
	public String getSqlAtLeastOneCollection();

	
	public String getPaginationSqlOnlyAssociations();

	public String getPaginationSqlAtLeastOneCollection();
	
	
	public String getPaginationNamedParam1();
	
	public String getPaginationNamedParam2();
	
	public String getPaginationSqlOnlyAssociationsNamed();
	
	public String getPaginationSqlAtLeastOneCollectionNamed();
	
	public default String[] getPaginationAdditionalColumns(String mainOrderByString) {
		return NO_COLS;
	}

	public default boolean isSecondaryWhereStarted() {
		return false;
	}
	
	public default Long[] applyRangeTransformation(long beginIndex, long endIndex) {
		return new Long[] {beginIndex, endIndex};
	}
	
	public String getSqlForNextSequenceVal(String sequenceName);
	
	public String getSqlForInArray();
	
	public String getSqlForCaseSensitiveColumn(String tableAlias, String separator, String column);
	
	public String getSqlForCaseInsensitiveColumn(String tableAlias, String separator, String column);
	
	public String getSqlForUpsert(String sqlInsert, String sqlUpdate, Object ... hints);
}
