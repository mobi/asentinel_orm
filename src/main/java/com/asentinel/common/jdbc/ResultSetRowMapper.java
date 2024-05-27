package com.asentinel.common.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * {@link RowMapper} implementation that expects the target resultset to have exactly one column and
 * multiple rows, each row representing another {@link ResultSet}. In essence this mapper should be used
 * to map a resultset of resultsets to a list of lists. While this is the most common use case, the client 
 * could use any combination of {@link RowMapper}s and/or {@link RowCallbackHandler}s to process the results. 
 * See the {@link #ResultSetRowMapper(ResultSetSqlParameter...)} constructor.  
 * <br>
 * <br>
 * 
 * It was coded to deal with the peculiar way PostgresSQL handles functions that return multiple resultsets.<br>
 * 
 * The following usage example assumes that the <code>refcursorfunc</code> function returns 2 resultsets 
 * each containing one column with multiple integer values:<br>
 * <pre>
 * 		SqlQuery queryEx = ....;
 * 		List&lt; List &lt; ? &gt; &gt; list = queryEx.query("select * from  refcursorfunc(?, ?, ?)",
 *			new ResultSetRowMapper(
 *				ReusableRowMappers.ROW_MAPPER_INTEGER,
 *				ReusableRowMappers.ROW_MAPPER_INTEGER
 *			),
 *		10, 20, 30);
 * </pre>
 * 
 * @see ResultSetUtils#getResults(java.sql.CallableStatement, List, int)
 * 
 * @author Razvan Popian
 */
public class ResultSetRowMapper implements RowMapper<List<?>> {
	
	private final ResultSetSqlParameter[] rsParams;
	
	/**
	 * Constructor taking an array of {@link ResultSetSqlParameter}s as parameter. The number of 
	 * elements in the array must match the number of rows in the target resultset,
	 * otherwise {@link #mapRow(ResultSet, int)} will throw {@link IllegalArgumentException}.  
	 * @param rsParams the array of result set processing instances, these will encapsulate 
	 * 			either {@link RowMapper}s or {@link RowCallbackHandler}s. A <code>null</code>
	 * 			will force this mapper to ignore the corresponding row.
	 */
	public ResultSetRowMapper(ResultSetSqlParameter ... rsParams) {
		if (rsParams == null) {
			this.rsParams = new ResultSetSqlParameter[0];
		} else {
			this.rsParams = rsParams;
		}
	}

	/**
	 * Constructor taking an array of {@link RowMapper}s as parameter. The number of 
	 * elements in the array must match the number of rows in the target resultset,
	 * otherwise {@link #mapRow(ResultSet, int)} will throw {@link IllegalArgumentException}.  
	 * @param mappers
	 * 			the array of {@link RowMapper}s. A <code>null</code>
	 * 			will force this mapper to ignore the corresponding row.
	 */
	public ResultSetRowMapper(RowMapper<?> ... mappers) {
		this.rsParams = ResultSetUtils.toResultSetSqlParameters(mappers);
	}


	@Override
	public List<?> mapRow(ResultSet rs, int rowNum) throws SQLException {
		// Is the rowNum 1 based or 0 based ? It seems to be 0 based when 
		// coming from the default ResultSetExtractor used by JdbcTemplate.
		int index = rowNum; 
		if (index < 0) {
			throw new IllegalArgumentException("Invalid index: " + index);
		}
		if (index >= rsParams.length) {
			throw new IllegalArgumentException("Invalid result set row index " + index 
					+ ". Looks like there are more results then you expected.");
		}
		
		ResultSet rsRow = (ResultSet) rs.getObject(1);
		ResultSetSqlParameter rsParam = rsParams[index];
		if (rsParam == null) {
			// we are not interested in this resultset
			// so we close it, and we return null
			JdbcUtils.closeResultSet(rsRow);
			return null;
		} else if (rsParam.isRowMapper()) {
			return ResultSetUtils.asList(rsRow, rsParam.getRowMapper());
		} else if (rsParam.isRowCallbackHandler()) {
			ResultSetUtils.processResultSet(rsRow, rsParam.getRowCallbackHandler());
			return null;
		} else {
			throw new IllegalArgumentException("Invalid ResultSetSqlParameter.");
		}
	}

}
