package com.asentinel.common.jdbc.flavors.postgres;

import org.springframework.util.StringUtils;

import com.asentinel.common.jdbc.flavors.SqlTemplates;
import com.asentinel.common.util.Assert;


public class PostgresSqlTemplates implements SqlTemplates {
	
	private final static String SQL_ONLY_ASSOCIATIONS = "%s";

	private final static String SQL_AT_LEAST_ONE_COLLECTION  
		= "select main.* %s from ( %s ) main" 
		+ " %s %s order by " + ROW_INDEX_COL;
	// 2 %s on the last row: join with the details tables, secondary where clause
	
	private final static String SQL_FIRST_ROW_ONLY = "select " + ROW_INDEX_COL + " from ( %s ) inner_select offset 0 limit 1";
	
	
	// pagination query constants
	private final static String PARAM_OFFSET = "param_offset";
	private final static String PARAM_LIMIT = "param_limit";
	
	private final static String ROW_INDEX_COL_TEMPLATE = "row_number() over (order by %s) - 1 " + ROW_INDEX_COL;

	private final static String PAGINATION_SQL_ONLY_ASSOCIATIONS 
		= "select * from (%s offset ? limit ?) main %s";
	
	private final static String PAGINATION_SQL_ONLY_ASSOCIATIONS_NAMED
		= "select * from (%s offset :" + PARAM_OFFSET + " limit :" + PARAM_LIMIT + ") main %s";
	
	private final static String PAGINATION_SQL_AT_LEAST_ONE_COLLECTION 
		= "select main.* %s from ( %s offset ? limit ?) main" 
		+ " %s %s order by " + ROW_INDEX_COL;
	// 2 %s on the last row: join with the details tables, secondary where clause
	
	private final static String PAGINATION_SQL_AT_LEAST_ONE_COLLECTION_NAMED
		= "select main.* %s from ( %s offset :" + PARAM_OFFSET + " limit :" + PARAM_LIMIT +") main" 
		+ " %s %s order by " + ROW_INDEX_COL;
	// 2 %s on the last row: join with the details tables, secondary where clause

	private final static String SQL_SEQ_NEXT_VAL = "nextval('%s')";
	private final static String SQL_IN_ARRAY_SELECT = " = any(?)";
	
	final static String UPSERT_CONFLICT_PLACEHOLDER = "#{conflict}";
	
	private final static String SQL_UPSERT 
			= UPSERT_INSERT_PLACEHOLDER 
			+ " on conflict "
			+ UPSERT_CONFLICT_PLACEHOLDER
			+ " do "
			+ UPSERT_UPDATE_PLACEHOLDER;
				
	
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
			beginIndex,
			endIndex <= beginIndex ? 0 : endIndex - beginIndex 
		};
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
			.append(tableAlias).append(separator).append(column).append(" collate \"C\"")
			.toString();
	}
	
	@Override
	public String getSqlForCaseInsensitiveColumn(String tableAlias, String separator, String column) {
		return new StringBuilder(tableAlias.length() + separator.length() + column.length())	
			.append(tableAlias).append(separator).append(column)
			.toString();
	}

	@Override
	public String getSqlForUpsert(String sqlInsert, String sqlUpdate, Object ... hints) {
		String sqlConflict = null;
		for (int i = 0; i < hints.length; i = i + 2) {
			if (UPSERT_CONFLICT_PLACEHOLDER.equals(hints[i])) {
				if (i == hints.length - 1) {
					throw new IllegalArgumentException(
							"The hints must be a collection of key-value pairs, so the number of hints should be a multiple of 2.");
				}
				sqlConflict = hints[i + 1].toString();
				break;
			}
		}
		if (sqlConflict == null) {
			throw new IllegalArgumentException("Can not find the " + UPSERT_CONFLICT_PLACEHOLDER + " parameter in the hints array. "
					+ "This is needed for Postgres to determine the conflict you are targetting. Please see the Postgres insert documentation.");
		}
		
		StringBuilder sb = new StringBuilder(SQL_UPSERT);
		
		int conflictStart = sb.indexOf(UPSERT_CONFLICT_PLACEHOLDER);
		int conflictEnd = conflictStart + UPSERT_CONFLICT_PLACEHOLDER.length();
		sb.replace(conflictStart, conflictEnd, sqlConflict);

		
		int insertStart = sb.indexOf(UPSERT_INSERT_PLACEHOLDER);
		int insertEnd = insertStart + UPSERT_INSERT_PLACEHOLDER.length();
		sb.replace(insertStart, insertEnd, sqlInsert);

		int updateStart = sb.indexOf(UPSERT_UPDATE_PLACEHOLDER);
		int updateEnd = updateStart + UPSERT_UPDATE_PLACEHOLDER.length();
		sb.replace(updateStart, updateEnd, sqlUpdate);
		
		return sb.toString();
	}


	@Override
	public String toString() {
		return "PostgresSqlTemplates";
	}
}
