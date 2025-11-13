package com.asentinel.common.jdbc;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.object.StoredProcedure;

import com.asentinel.common.jdbc.flavors.postgres.InOutCallableStatementCallback;
import com.asentinel.common.jdbc.flavors.postgres.InOutCallableStatementCreator;

/**
 * Central interface that defines the methods for calling stored procedures that fulfill the following
 * conditions:
 * 	<li> their IN parameters are the first parameters declared in the stored procedure signature,<br>
 * 	<li> their OUT parameters are the last parameters declared in the stored procedure signature,<br>
 *  <li> their OUT parameters are cursors, other types of out parameters are not supported, see
 *  	the {@link StoredProcedure} class for other types of calls.<br>
 *  <br>
 *  
 *  Implementations should be thread safe or function as effectively immutable objects (they
 *  should be configured using set methods, published and after that they should not be changed).
 *  <br><br>
 *  
 * The inParams varargs parameter of all the methods should support any java basic type (Integer, Double etc)
 * and also Spring's {@link SqlParameterValue} and {@link SqlTypeValue}.  
 * Also all the methods support byte[] and InputStreams as arguments for blobs.
 * <br><br>
 *  
 *  @see #call(String, RowMapper[], Object...)
 *  @see #call(String, Class[], Object...)
 *  @see #call(String, ResultSetSqlParameter[], Object...)
 *  @see #call(String, RowCallbackHandler, Object...)
 *  
 *  @see InOutCallTemplate
 *  @see InOutCallableStatementCreator
 *  @see InOutCallableStatementCallback
 *  @see BooleanParameterConverter
 *  
 *  @see StoredProcedure
 *  
 *  @author Razvan Popian
 */
public interface InOutCall {
	
	/**
	 * Calls a stored procedure that will be executed in the specified {@link CallableStatementCallback}.
	 * The call string is created using the resultCount parameter and the inParams.
	 * 
	 * @param spName name of the stored procedure to be called (including package name if necessary).
	 * @param action the CallableStatementCallback that will execute the stored procedure and process
	 * 			the results.
	 * @param resultCount number of cursors returned, cursors must be the last declared parameters in the
	 * 			stored procedure signature.
	 * @param inParams stored procedure IN parameters as Object array. These parameters can be any type that matches the actual stored
	 * 					procedure argument. Objects with class {@link SqlParameterValue} and {@link SqlTypeValue} are also accepted if more
	 * 					exact parameter matching is necessary. Also byte arrays or InputStreams can be used where the stored procedure 
	 * 					expects blobs. Any InputStreams passed as parameters should be closed by the implementation.
	 * @return the object that the action returns.
	 * @throws DataAccessException if any database error occurs.
	 */
	Object call(String spName, CallableStatementCallback<?> action, int resultCount, Object ... inParams) throws DataAccessException;

	
	// ---------  Calls with RowMapper parameters ---------------- //
	
	/**
	 * Calls a stored procedure with the specified parameters. The IN parameters must match the
	 * inParams array size and types. The OUT parameters must match the size of the rowMappers array.
	 * 
	 * @param spName name of the stored procedure to be called (including package name if necessary).
	 * @param rowMappers array of mappers used to convert each resultset to a {@code List<?>}, one mapper for each cursor returned. 
	 * 					If you are not interested in one of the returned cursors, you can pass {@code null} as a replacement for 
	 * 					the corresponding mapper. A {@code null} will be inserted in the returned list in that position in this case. 
	 * @param inParams stored procedure IN parameters as Object array. These parameters can be any type that matches the actual stored
	 * 					procedure argument. Objects with class {@link SqlParameterValue} and {@link SqlTypeValue} are also accepted if more
	 * 					exact parameter matching is necessary. Also byte arrays or InputStreams can be used where the stored procedure 
	 * 					expects blobs. Any InputStreams passed as parameters should be closed by the implementation.
	 * 
	 * @return a list of lists. Each member list is the representation of a cursor.
	 * @throws DataAccessException if any database error occurs.
	 * 
	 * @see RowMapper
	 * @see SqlParameterValue
	 * @see SqlTypeValue
	 */
	List<List<?>> call(String spName, RowMapper<?>[] rowMappers, Object ... inParams) throws DataAccessException;
	
	/**
	 * Calls a stored procedure that returns just one cursor.
	 * @see #call(String, RowMapper[], Object...)
	 */
	<T> List<T> call(String spName, RowMapper<T> rowMapper, Object ... inParams) throws DataAccessException;
	
	/**
	 * Calls a stored procedure that returns exactly one cursor
	 * with exactly one row.
	 * @see #call(String, RowMapper[], Object...)
	 * 
	 * @throws EmptyResultDataAccessException if the cursor is empty.
	 * @throws IncorrectResultSizeDataAccessException if the cursor has more than 1 row.
	 */
	<T> T callForObject(String spName, RowMapper<T> rowMapper, Object ... inParams)
		throws DataAccessException, EmptyResultDataAccessException, IncorrectResultSizeDataAccessException;
	
	
	// ---------  Calls with Class parameters ----------------- //

	/**
	 * Calls a stored procedure with the specified parameters. The IN paramerers must match the
	 * inParams array size and types. The OUT paramters must match the size of the classes array.
	 * 
	 * @param spName  name of the stored procedure to be called (including package name if necessary).
	 * @param classes - array of classes, one class for each returned cursor. The implementations will somehow
	 * 					convert each class to a RowMapper and then call {@link #call(String, RowMapper[], Object...)}
	 * 					method. A {@link RowMapperFactory} is a way to obtain a mapper for a class.
	 * 					Byte arrays and InputStreams are supported classes for procedures that return blob cursors.
	 *  				If you are not interested in one of the returned cursors, you can pass {@code null} as a replacement 
	 *  				for the corresponding resultset. A {@code null} will be inserted in the returned list 
	 * 					in that position in this case.
	 * @param inParams stored procedure IN parameters as Object array. These parameters can be any type that matches the actual stored
	 * 					procedure argument. Objects with class {@link SqlParameterValue} and and {@link SqlTypeValue} are also accepted if more
	 * 					exact parameter matching is necessary. Also byte arrays or InputStreams can be used where the stored procedure 
	 * 					expects blobs. Any InputStreams passed as parameters should be closed by the implementation.
	 * 
	 * @return a list of lists. Each member list is the representation of a cursor.
	 * @throws DataAccessException - if any database error occurs.
	 * 
	 * @see RowMapper
	 * @see SqlParameterValue
	 * @see SqlTypeValue
	 */
	List<List<?>> call(String spName, Class<?>[] classes, Object ... inParams) throws DataAccessException;
	
	/**
	 * Calls a stored procedure that returns just one cursor.
	 * @see #call(String, Class[], Object...)
	 */
	<T> List<T> call(String spName, Class<T> clasz, Object ... inParams) throws DataAccessException;
	
	/**
	 * Calls a stored procedure that returns exactly one cursor
	 * with exactly one row.
	 * @see #call(String, Class[], Object...)
	 * 
	 * @throws EmptyResultDataAccessException if the cursor is empty.
	 * @throws IncorrectResultSizeDataAccessException if the cursor has more than 1 row.
	 */
	<T> T callForObject(String spName, Class<T> clasz, Object ... inParams)
		throws DataAccessException, EmptyResultDataAccessException, IncorrectResultSizeDataAccessException;
	
	// -------------------------------------------------------- //

	/**
	 * Calls a stored procedure that does not return results.
	 * 
	 * @see #call(String, RowMapper[], Object...)
	 * @see #call(String, Class[], Object...)
	 */
	void callWithoutResult(String spName, Object ... inParams) throws DataAccessException;
	
	
	// -------------------------------------------------------- //
	
	/**
	 * Calls a stored procedure that returns multiple resultsets that will be processed 
	 * using the {@link ResultSetSqlParameter} objects received as parameters. The IN parameters must 
	 * match the inParams array size and types. The OUT parameters must match the size of the 
	 * rowMappers array.<br>
	 * This method is useful for writing some of the resultsets to XML, XLS etc.
	 *   
	 * @param spName name of the stored procedure to be called (including package name if necessary).
	 * @param rsParams array of {@link ResultSetSqlParameter} used to process each resultset , one ResultSetSqlParameter 
	 * 					for each cursor returned. If you are not interested in one of the returned cursors, you can pass 
	 * 					{@code null} as a replacement for the corresponding resultset. A {@code null} will be inserted in the returned list 
	 * 					in that position in this case.
	 * @param inParams stored procedure IN parameters as Object array. These parameters can be any type that matches the actual stored
	 * 					procedure argument. Objects with class {@link SqlParameterValue} and {@link SqlTypeValue} are also accepted if more
	 * 					exact parameter matching is necessary. Also byte arrays or InputStreams can be used where the stored procedure 
	 * 					expects blobs. Any InputStreams passed as parameters should be closed by the implementation.
	 * @return list of lists, one list for each {@code rsParams} element
	 *
	 * @see ResultSetSqlParameter
	 */
	List<List<?>> call(String spName, ResultSetSqlParameter[] rsParams, Object ... inParams) throws DataAccessException;
	
	/**
	 * Calls a stored procedure that returns one resultset that will be processed using
	 * a {@link RowCallbackHandler}. This method is useful for writing the resultset to 
	 * XML, XLS etc.
	 * 
	 * @see #call(String, ResultSetSqlParameter[], Object...)
	 */
	void call(String spName, RowCallbackHandler handler, Object ... inParams) throws DataAccessException;
}
