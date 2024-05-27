package com.asentinel.common.jdbc;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.support.KeyHolder;

/**
 * Central interface that defines the methods for executing sql queries. Implementations
 * normally delegate to the Spring JDBC interface {@link JdbcOperations},
 * but they perform other operations like logging the sql queries. Use {@link SqlQueryTemplate} as
 * implementation of this interface.
 * <br><br>
 * 
 * Implementations should be thread safe or function as effectively immutable objects (they
 * should be configured using set methods, published and after that they should not be changed).
 * <br><br>
 * 
 * The inParams varargs parameter of all the methods should support any java basic type (Integer, Double etc)
 * and also Spring's {@link SqlParameterValue} and {@link SqlTypeValue}. Booleans are supported as arguments, 
 * but implementations will convert them to a database native type (using {@link BooleanParameterConverter}). 
 * The {@link #update(String, Object...)} methods also supports byte[] and InputStreams as
 * IN parameters where blobs are expected.
 * <br> 
 * 
 * @see #query(String, RowMapper, Object...)
 * @see #update(String, Object...)
 * 
 * @see SqlQueryTemplate
 * 
 * @author Razvan Popian
 */
public interface SqlQuery {

	/**
	 * Executes a SELECT sql query using the mapper to extract the resultset rows
	 * and passes the inParams to the underlying {@link JdbcOperations} implementation. 
	 * @param sql the query to be executed.
	 * @param mapper mapper used for extracting rows. 
	 * @param inParams input parameters for the query.
	 * @return List, one element for each row.
	 * @throws DataAccessException
	 */
	public <T> List<T> query(String sql, RowMapper<T> mapper, Object ... inParams) throws DataAccessException;
	
	/**
	 * Executes a SELECT sql query using a {@link RowCallbackHandler} to process the resultset rows
	 * and passes the inParams to the underlying {@link JdbcOperations} implementation. 
	 * @param sql the query to be executed.
	 * @param handler {@link RowCallbackHandler} implementation used for processing each row. 
	 * @param inParams input parameters for the query.
	 */
	public void query(String sql, RowCallbackHandler handler, Object ... inParams) throws DataAccessException;
	
	/**
	 * Executes a SELECT query in the same conditions as {@link #query(String, RowMapper, Object...)}
	 * but has a Class parameter instead of {@link RowMapper}. Implementations will normally
	 * use a {@link RowMapperFactory} to create a mapper and then 
	 * call {@link #query(String, RowMapper, Object...)}. 
	 * @param sql the query to be executed.
	 * @param clasz the class to which each row will be converted. Can be byte[] or InputStream for blobs.
	 * @param inParams input parameters for the query.
	 * @return List, one element for each row, each element type is clasz.
	 * @throws DataAccessException
	 */
	public <T> List<T> query(String sql, Class<T> clasz, Object ... inParams) throws DataAccessException;

	/**
	 * Executes a SELECT query that returns exactly one row.  If the resultset is empty 
	 * an {@link EmptyResultDataAccessException} is thrown. If it has more than one row
	 * an {@link IncorrectResultSizeDataAccessException} is thrown. Uses the mapper parameter 
	 * to convert the first row to an object. 
	 * @param sql the query to be executed.
	 * @param mapper mapper used for extracting rows.
	 * @param inParams input parameters for the query.
	 * @return object resulted from the query.
	 * @throws EmptyResultDataAccessException if the resultset in empty.
	 * @throws IncorrectResultSizeDataAccessException if the resultset has more than 1 row.
	 * @throws DataAccessException
	 */
	public <T> T queryForObject(String sql, RowMapper<T> mapper, Object ... inParams) 
		throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException, DataAccessException;
	

	/**
	 * Executes a SELECT query in the same conditions as {@link #queryForObject(String, RowMapper, Object...)}
	 * but has a Class parameter instead of {@link RowMapper}. Implementations will normally
	 * use a {@link RowMapperFactory} to create a mapper and then 
	 * call {@link #queryForObject(String, RowMapper, Object...)}.
	 * @param sql the query to be executed.
	 * @param clasz the class to which the first row will be converted. Can be byte[] or InputStream for blobs.
	 * @param inParams input parameters for the query.
	 * @return object of type clasz.
	 * @throws EmptyResultDataAccessException if the resultset in empty.
	 * @throws IncorrectResultSizeDataAccessException if the resultset has more than 1 row.
	 * @throws DataAccessException
	 */
	public <T> T queryForObject(String sql, Class<T> clasz, Object ... inParams) 
		throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException, DataAccessException;
	
	/**
	 * Convenience method that executes a SELECT query that returns an int.
	 * @return the int resulted from the query. If the query returns {@code null}
	 * 			implementations should return 0. 
	 */
	public int queryForInt(String sql, Object ... inParams) 
		throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException, DataAccessException;
	
	/**
	 * Convenience method that executes a SELECT query that returns an long.
	 * @return the long resulted from the query. If the query returns {@code null}
	 * 			implementations should return 0. 
	 */
	public long queryForLong(String sql, Object ... inParams) 
		throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException, DataAccessException;
	

	/**
	 * Convenience method that executes a SELECT query that returns a String.
	 * @return the String resulted from the query. If the query returns {@code null}
	 * 			implementations should return "". 
	 */
	public String queryForString(String sql, Object ... inParams) 
		throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException, DataAccessException;
	
	
	/**
	 * Convenience method that executes a select query that returns one row
	 * and creates a {@link Map} for that row. 
	 * @return the map resulted from the row.
	 * 
	 * @see #queryForObject(String, Class, Object...)
	 * @see RowAsMapRowMapper
	 */
	public Map<String, Object> queryForMap(String sql, Object ... inParams)
			throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException, DataAccessException;
	
	
	/**
	 * Executes an INSERT, UPDATE or DELETE query. The inParams varargs
	 * can contain byte arrays or InputStreams. Implementations must convert
	 * these to BLOBs. Implementations should close any InputStreams that they receive.  
	 * @param sql the query to be executed.
	 * @param inParams input parameters for the update query. Can contain instances of
	 * 					byte[] or InputStream.
	 * @return the update count for the query.
	 * @throws DataAccessException
	 */
	public int update(String sql, Object ... inParams) throws DataAccessException;

	/**
	 * This method should be used to execute INSERT operations that generate new ids in the database. 
	 * The inParams varargs can contain byte arrays or InputStreams. Implementations must convert
	 * these to BLOBs. Implementations should close any InputStreams that they receive.  
	 * @param sql the query to be executed.
	 * @param keyColumnNames the names of the id columns for which to store values in the <code>keyHolder</code>
	 * 			parameter.
	 * @param keyHolder {@link KeyHolder} instance that will contain the generated ids.
	 * @param inParamsUpdate input parameters for the update query. Can contain instances of
	 * 					byte[] or InputStream.
	 * @return the update count for the query.
	 * @throws DataAccessException
	 */
	public int update(String sql, String[] keyColumnNames, KeyHolder keyHolder, Object ... inParamsUpdate) throws DataAccessException;
	
	/**
	 * @return the underlying {@link JdbcOperations} implementation. This
	 * 		is a convenience method.
	 */
	public JdbcOperations getJdbcOperations();
	
}
