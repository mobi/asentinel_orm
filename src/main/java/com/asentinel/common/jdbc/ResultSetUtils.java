package com.asentinel.common.jdbc;

import static java.util.stream.Collectors.joining;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.StringUtils;

import com.asentinel.common.util.Assert;
import com.asentinel.common.util.Utils;

/**
 * Helper class useful for:<br>
 * 	- converting resultsets to lists/arrays, see #asList(...) and #asArray(...) methods. Note that these
 * 		methods close the resultset after they convert it to a list/array;<br>
 *  - extract values from a resultset column, see #getXXX methods.<br>
 *  - extract resultsets from a {@link CallableStatement}.<br>
 *  <br>
 *  
 * The methods defined in this class should be used by any classes that process ResultSets 
 * to ensure consistency.
 * <br><br>
 * 
 * @see InOutCall
 * @see InOutCallTemplate
 * 
 * @see SqlQuery
 * @see SqlQueryTemplate
 * 
 * @author Razvan Popian
 */
public final class ResultSetUtils {
	private final static Logger log = LoggerFactory.getLogger(ResultSetUtils.class);
	private ResultSetUtils(){}
	
	public final static int DEFAULT_INITIAL_LIST_SIZE = 30;
	
	// ---------------------------- //

	/**
	 * Converts the resultset rs to a List of T objects, using the RowMapper. The fetch size of 
	 * the resultset can be set before calling this method. This method closes the resultset after it extracts 
	 * the last record or if an error occurs during conversion.
	 * @param rs the resultset to convert.
	 * @param mapper the mapper used for conversion.
	 * @param initialListSize the list initial size. Use higher values for large resultsets.
	 * @return the List resulted from the rs resultset.
	 * @throws SQLException
	 */
	public static <T> List<T> asList(ResultSet rs, RowMapper<T> mapper, int initialListSize) throws SQLException {
		Assert.assertNotNull(rs, "rs");
		Assert.assertNotNull(mapper, "mapper");
		Assert.assertStrictPositive(initialListSize, "initialListSize");
		try {
			long t0 = 0, t1 = 0;
	        if (log.isTraceEnabled()){
	        	log.trace("asList - mapper: " + mapper);
	        	t0 = System.nanoTime();
	        }
			List<T> list = new ArrayList<T>(initialListSize);
			while (rs.next()) {
				list.add(mapper.mapRow(rs, rs.getRow()));
			}
	        if (log.isTraceEnabled()){
	        	t1 = System.nanoTime();
	        	log.trace("asList - Resultset size: " + list.size() + ", pulled in " + ((t1 - t0)/1000000) 
	        			+ " ms / fetch: " + rs.getFetchSize());
	        }
			return list;
		} finally {
			JdbcUtils.closeResultSet(rs);
		}
	}
	
	/** @see #asList(ResultSet, RowMapper, int) */
	public static <T> List<T> asList(ResultSet rs, RowMapper<T> mapper) throws SQLException {
		return asList(rs, mapper, DEFAULT_INITIAL_LIST_SIZE);
	}
	
	// ---------------------------- //
	
	
	/** {@link #asList(ResultSet, Class, int, LobHandler)} */
	public static <T> List<T> asList(ResultSet rs, Class<T> clasz, int initialListSize) throws SQLException {
		return asList(rs, clasz, initialListSize, null);
	}
	
	/** @see #asList(ResultSet, RowMapper, int) */
	public static <T> List<T> asList(ResultSet rs, Class<T> clasz, int initialListSize, LobHandler lobHandler) throws SQLException {
		return (List<T>) asList(rs, rowMapperForClass(clasz, lobHandler), initialListSize);
	}
	
	/** @see #asList(ResultSet, RowMapper, int) */
	public static <T> List<T> asList(ResultSet rs, Class<T> clasz) throws SQLException {
		return asList(rs, clasz, DEFAULT_INITIAL_LIST_SIZE);
	}

	
	// ---------------------------- //

	/**
	 * Converts a list to an array of the specified Class type. 
	 * @param <T>
	 * @param list list to be converted.
	 * @param clasz class of each array element.
	 * @return the resulted array.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] asArray(List<T> list, Class<T> clasz) {
		Object[] objects = (Object[])java.lang.reflect.Array.newInstance(clasz, list.size());
		return (T[])list.toArray(objects);
	}
	
	/** @see #asArray(ResultSet, Class, int, LobHandler) */
	public static <T> T[] asArray(ResultSet rs, Class<T> clasz, int initialListSize) throws SQLException {
		return asArray(rs, clasz, initialListSize, null);
	}
	
	/**
	 * Converts the resultset rs to an array of T objects. This method
	 * closes the resultset after it extracts the last record or if an error occurs 
	 * during conversion.
	 *  
	 * @see #asList(ResultSet, RowMapper, int) 
	 */
	public static <T> T[] asArray(ResultSet rs, Class<T> clasz, int initialListSize, LobHandler lobHandler) throws SQLException {
		List<T> list = asList(rs, rowMapperForClass(clasz, lobHandler), initialListSize);
		return asArray(list, clasz);
	}
	
	/** @see #asArray(ResultSet, Class, int) */
	public static <T> T[] asArray(ResultSet rs, Class<T> clasz) throws SQLException {
		return asArray(rs, clasz, DEFAULT_INITIAL_LIST_SIZE);
	}
	
	/**
	 * Converts the row mappers array to an array of ResultSetSqlParameter. A null
	 * item into the rowMappers parameter will be converted to a null item in the
	 * resulting array.
	 * @param rowMappers
	 * @return the resulted array.
	 */
	public static ResultSetSqlParameter[] toResultSetSqlParameters(RowMapper<?>[] rowMappers) {
		if (rowMappers == null) {
			return new ResultSetSqlParameter[0];
		}
		ResultSetSqlParameter[] rsParams = new ResultSetSqlParameter[rowMappers.length];
		for (int i=0; i<rowMappers.length; i++) {
			if (rowMappers[i] == null) {
				rsParams[i] = null;
			} else {
				rsParams[i] = new ResultSetSqlParameter(rowMappers[i]);
			}
		}
		return rsParams;
	}
	
	/**
	 * Processes a resultset using a handler could be useful for writing
	 * the resultset to XML, XLS etc.
	 * @param rs the resultset to process.
	 * @param handler the handler that will deal with the resultset.
	 * @throws SQLException
	 */
	public static void processResultSet(ResultSet rs, RowCallbackHandler handler) throws SQLException {
		Assert.assertNotNull(rs, "rs");
		Assert.assertNotNull(handler, "handler");
		try {
			long t0 = 0, t1 = 0;
	        if (log.isTraceEnabled()){
	        	log.trace("processResultSet - handler: " + handler);
	        	t0 = System.nanoTime();
	        }
	        RowCallbackHandlerDecorator handlerDecorator = new RowCallbackHandlerDecorator(handler);
			while (rs.next()) {
				handlerDecorator.processRow(rs);
			}
	        if (log.isTraceEnabled()){
	        	t1 = System.nanoTime();
	        	log.trace("processResultSet - Resultset size: " + handlerDecorator.size() + ", pulled in " + ((t1 - t0)/1000000) 
	        			+ " ms / fetch: " + rs.getFetchSize());
	        }
		} finally {
			JdbcUtils.closeResultSet(rs);
		}
		
	}

	
	/**
	 * Converts the resultsets returned by a {@link CallableStatement} to a list of lists, one list for
	 * each resultset according to the {@link ResultSetSqlParameter} list received as argument. Null values can
	 * be put in the list for resultsets the caller is not interested in. In this case, a null will be put 
	 * in the corresponding position of the returned list of lists. Also, if the ResultSetSqlParameter is
	 * using a {@link RowCallbackHandler} to process the resultset a null will be inserted in the corresponding
	 * position in the returned list.
	 * 
	 * @param cs the CallableStatement
	 * @param rsParams the parameters used to convert the resultsets
	 * @param offset number of IN parameters, this method assumes that the OUT resultset parameters are the
	 * 				declared last in the stored procedure signature, after the last IN parameter. 
	 * @return list of lists
	 * @throws SQLException
	 */	
	public static List<List<?>> getResults(CallableStatement cs, List<ResultSetSqlParameter> rsParams, int offset) throws SQLException {
		Assert.assertNotNull(cs, "cs");
		Assert.assertPositive(offset, "offset");
		Assert.assertNotNull(rsParams, "rsParams");
		List<List<?>> list = new ArrayList<List<?>>();
		for (int i=0; i<rsParams.size(); i++) {
			ResultSetSqlParameter rsParam = rsParams.get(i);
			int finalOffset = offset + i + 1;
			if (rsParam == null) {
				// we are not interested in this resultset
				// so we close it, and we add a null to the list of results
				JdbcUtils.closeResultSet((ResultSet) cs.getObject(finalOffset));
				list.add(null);
				continue;
			}
			if (rsParam.isRowMapper()) {
				List<?> listRs = ResultSetUtils.asList((ResultSet) cs.getObject(finalOffset), rsParam.getRowMapper());
				list.add(listRs);
			} else if (rsParam.isRowCallbackHandler()) {
				processResultSet((ResultSet) cs.getObject(finalOffset), rsParam.getRowCallbackHandler());
				list.add(null);
			} else {
				throw new IllegalArgumentException("Invalid ResultSetSqlParameter.");
			}
		}
		return list;
	}

	/**
	 * Creates a row mapper for the clasz parameter. This method can either
	 * create a new row mapper or return an existing thread safe mapper.
	 * @param clasz  the class to create/return a row mapper for. 
	 * @return a RowMapper for the specified class. For String, Integer,
	 * 			Long, Double, Boolean, Date and BLOB this method returns the corresponding
	 * 			mapper defined in {@link ReusableRowMappers}. For byte[] it returns a new 
	 * 			{@link ByteArrayRowMapper} and for InputStream it returns a new
	 * 			{@link InputStreamRowMapper}. For Map it returns a new instance of 
	 * 			{@link RowAsMapRowMapper}.
	 * 			This method returns null if the clasz parameter is null.
	 * 			It is not recommended to pass BLOB.class to this method anymore.
	 * 
	 * @see ReusableRowMappers#ROW_MAPPER_STRING
	 * @see ReusableRowMappers#ROW_MAPPER_INTEGER
	 * @see ReusableRowMappers#ROW_MAPPER_LONG
	 * @see ReusableRowMappers#ROW_MAPPER_DOUBLE
	 * @see ReusableRowMappers#ROW_MAPPER_BOOLEAN
	 * @see ReusableRowMappers#ROW_MAPPER_DATE
	 * @see ReusableRowMappers#ROW_MAPPER_BIG_DECIMAL
	 * @see ReusableRowMappers#ROW_MAPPER_BIG_INTEGER
	 * @see ByteArrayRowMapper
	 * @see InputStreamRowMapper
	 * @see RowAsMapRowMapper
	 * @see RowAsArrayRowMapper
	 */
	@SuppressWarnings("unchecked")
	public static <T> RowMapper<T> rowMapperForClass(Class<T> clasz, LobHandler lobHandler) {
		if (clasz == null)
			return null;
		else if (clasz == String.class)
			return (RowMapper<T>) ReusableRowMappers.ROW_MAPPER_STRING;
		else if (clasz == Integer.class)
			return (RowMapper<T>) ReusableRowMappers.ROW_MAPPER_INTEGER;
		else if (clasz == Long.class)
			return (RowMapper<T>) ReusableRowMappers.ROW_MAPPER_LONG;
		else if (clasz == Double.class)
			return (RowMapper<T>) ReusableRowMappers.ROW_MAPPER_DOUBLE;
		else if (clasz == Boolean.class)			
			return (RowMapper<T>) ReusableRowMappers.ROW_MAPPER_BOOLEAN;
		else if (clasz == Date.class)
			return (RowMapper<T>) ReusableRowMappers.ROW_MAPPER_DATE;
		else if (clasz == BigDecimal.class)
			return (RowMapper<T>) ReusableRowMappers.ROW_MAPPER_BIG_DECIMAL;
		else if (clasz == BigInteger.class)
			return (RowMapper<T>) ReusableRowMappers.ROW_MAPPER_BIG_INTEGER;
		else if (clasz == byte[].class)
			return (RowMapper<T>) new ByteArrayRowMapper(lobHandler);
		else if (clasz == InputStream.class)
			return (RowMapper<T>) new InputStreamRowMapper(lobHandler);
		else if (clasz == Map.class) {
			return (RowMapper<T>) new RowAsMapRowMapper();
		} else if (clasz == Object[].class) {
			return (RowMapper<T>) new RowAsArrayRowMapper();
		} else {
			return new ReflectionRowMapper<T>(clasz);
		}
	}
	
	
	
	// ---------------------------- //
	
	private final static Integer DEFAULT_INTEGER = Integer.valueOf(0);
	private final static Long DEFAULT_LONG = Long.valueOf(0);
	private final static Double DEFAULT_DOUBLE = Double.valueOf(0.0d);
	private final static BigDecimal DEFAULT_BIG_DECIMAL = BigDecimal.ZERO;
	private final static BigInteger DEFAULT_BIG_INTEGER = BigDecimal.ZERO.toBigInteger();
	private final static String DEFAULT_STRING = "";
	private final static Boolean DEFAULT_BOOLEAN = Boolean.FALSE;
	private final static String[] DEFAULT_STRING_ARRAY = {};
	private final static Number[] DEFAULT_NUMBER_ARRAY = {};
	
	
	// Integer methods follow
	
	public static int getIntValue(ResultSet rs, int col) throws SQLException {
		return rs.getInt(col);
	}
	
	public static int getIntValue(ResultSet rs, String col) throws SQLException {
		return rs.getInt(col);
	}
	
	public static Integer getIntObject(ResultSet rs, int col) throws SQLException {
		return getIntObject(rs, col, false);
	}
	
	public static Integer getIntObject(ResultSet rs, String col) throws SQLException {
		return getIntObject(rs, col, false);
	}
	
	public static Integer getIntObject(ResultSet rs, int col, boolean allowNull) throws SQLException {
		return getIntObject(getIntValue(rs, col), rs, allowNull);
	}
	
	public static Integer getIntObject(ResultSet rs, String col, boolean allowNull) throws SQLException {
		return getIntObject(getIntValue(rs, col), rs, allowNull);
	}
	
	private static Integer getIntObject(int value, ResultSet rs, boolean allowNull) throws SQLException {
		if (rs.wasNull()) {
			if (allowNull) {
				return null;
			} else {
				return DEFAULT_INTEGER;
			}
		} else {
			return Integer.valueOf(value);
		}
	}
	
	
	// Long methods follow
	
	public static long getLongValue(ResultSet rs, int col) throws SQLException {
		return rs.getLong(col);
	}
	
	public static long getLongValue(ResultSet rs, String col) throws SQLException {
		return rs.getLong(col);
	}
	
	public static Long getLongObject(ResultSet rs, int col) throws SQLException {
		return getLongObject(rs, col, false);
	}
	
	public static Long getLongObject(ResultSet rs, String col) throws SQLException {
		return getLongObject(rs, col, false);
	}
	
	public static Long getLongObject(ResultSet rs, int col, boolean allowNull) throws SQLException {
		return getLongObject(getLongValue(rs, col), rs, allowNull);
	}
	
	public static Long getLongObject(ResultSet rs, String col, boolean allowNull) throws SQLException {
		return getLongObject(getLongValue(rs, col), rs, allowNull);
	}
	
	private static Long getLongObject(long value, ResultSet rs, boolean allowNull) throws SQLException {
		if (rs.wasNull()) {
			if (allowNull) {
				return null;
			} else {
				return DEFAULT_LONG;
			}
		} else {
			return Long.valueOf(value);
		}
	}
	
	
	
	// Double methods follow
	
	public static double getDoubleValue(ResultSet rs, int col) throws SQLException {
		return rs.getDouble(col);
	}
	
	public static double getDoubleValue(ResultSet rs, String col) throws SQLException {
		return rs.getDouble(col);
	}
	
	public static Double getDoubleObject(ResultSet rs, int col) throws SQLException {
		return getDoubleObject(rs, col, false);
	}
	
	public static Double getDoubleObject(ResultSet rs, String col) throws SQLException {
		return getDoubleObject(rs, col, false);
	}
	
	public static Double getDoubleObject(ResultSet rs, int col, boolean allowNull) throws SQLException {
		return getDoubleObject(getDoubleValue(rs, col), rs, allowNull);
	}
	
	public static Double getDoubleObject(ResultSet rs, String col, boolean allowNull) throws SQLException {
		return getDoubleObject(getDoubleValue(rs, col), rs, allowNull);
	}
	
	private static Double getDoubleObject(double value, ResultSet rs, boolean allowNull) throws SQLException {
		if (rs.wasNull()) {
			if (allowNull) {
				return null;
			} else {
				return DEFAULT_DOUBLE;
			}
		} else {
			return Double.valueOf(value);
		}
	}
	

	// BigInteger methods follows
	
	public static BigInteger getBigIntegerValue(ResultSet rs, int col) throws SQLException {
		return getBigIntegerObject(rs, col);
	}
	
	public static BigInteger getBigIntegerValue(ResultSet rs, String col) throws SQLException {
		return getBigIntegerObject(rs, col);
	}
	
	public static BigInteger getBigIntegerObject(ResultSet rs, int col) throws SQLException {
		return getBigIntegerObject(rs, col, false);
	}
	
	public static BigInteger getBigIntegerObject(ResultSet rs, String col) throws SQLException {
		return getBigIntegerObject(rs, col, false);
	}
	
	public static BigInteger getBigIntegerObject(ResultSet rs, int col, boolean allowNull) throws SQLException {
		return getBigIntegerObject(rs.getBigDecimal(col), allowNull);
	}
	
	public static BigInteger getBigIntegerObject(ResultSet rs, String col, boolean allowNull) throws SQLException {
		return getBigIntegerObject(rs.getBigDecimal(col), allowNull);
	}
	
	private static BigInteger getBigIntegerObject(BigDecimal value, boolean allowNull) throws SQLException {
		if (value == null) {
			if (allowNull) {
				return null;
			} else {
				return DEFAULT_BIG_INTEGER;
			}
		} else {
			return value.toBigInteger();
		}
	}
	
	
	
	
	// BigDecimal methods follow

	public static BigDecimal getBigDecimalValue(ResultSet rs, int col) throws SQLException {
		return getBigDecimalObject(rs, col);
	}
	
	public static BigDecimal getBigDecimalValue(ResultSet rs, String col) throws SQLException {
		return getBigDecimalObject(rs, col);
	}
	
	public static BigDecimal getBigDecimalObject(ResultSet rs, int col) throws SQLException {
		return getBigDecimalObject(rs, col, false);
	}
	
	public static BigDecimal getBigDecimalObject(ResultSet rs, String col) throws SQLException {
		return getBigDecimalObject(rs, col, false);
	}
	
	public static BigDecimal getBigDecimalObject(ResultSet rs, int col, boolean allowNull) throws SQLException {
		return getBigDecimalObject(rs.getBigDecimal(col), allowNull);
	}
	
	public static BigDecimal getBigDecimalObject(ResultSet rs, String col, boolean allowNull) throws SQLException {
		return getBigDecimalObject(rs.getBigDecimal(col), allowNull);
	}
	
	private static BigDecimal getBigDecimalObject(BigDecimal value, boolean allowNull) throws SQLException {
		if (value == null) {
			if (allowNull) {
				return null;
			} else {
				return DEFAULT_BIG_DECIMAL;
			}
		} else {
			return value;
		}
	}
	

	
	// String methods follow
	
	public static String getStringValue(ResultSet rs, int col) throws SQLException {
		return getStringObject(rs, col);
	}
	
	public static String getStringValue(ResultSet rs, String col) throws SQLException {
		return getStringObject(rs, col);
	}
	
	public static String getStringObject(ResultSet rs, int col) throws SQLException {
		return getStringObject(rs, col, false);
	}
	
	public static String getStringObject(ResultSet rs, String col) throws SQLException {
		return getStringObject(rs, col, false);
	}
	
	public static String getStringObject(ResultSet rs, int col, boolean allowNull) throws SQLException {
		return getStringObject(rs.getObject(col), allowNull);
	}
	
	public static String getStringObject(ResultSet rs, String col, boolean allowNull) throws SQLException {
		return getStringObject(rs.getObject(col), allowNull);
	}
	
	
	private static String getStringObject(Object object, boolean allowNull) throws SQLException {
		if (object == null) {
			if (allowNull) {
				return null;
			} else {
				return DEFAULT_STRING;
			}
		} else {
	    	String res;
	    	if (object instanceof Clob){
	    		res = ((Clob) object).getSubString(1,(int)((Clob) object).length());
	    	} else {
	    		res = (String) object;
	    	}
			return res;
		}
	}

	
	// Boolean methods follow
	
	public static boolean getBooleanValue(ResultSet rs, int col) throws SQLException {
		return getBooleanObject(rs, col);
	}
	
	public static boolean getBooleanValue(ResultSet rs, String col) throws SQLException {
		return getBooleanObject(rs, col);
	}
	
	public static Boolean getBooleanObject(ResultSet rs, int col) throws SQLException {
		return getBooleanObject(rs, col, false);
	}
	
	public static Boolean getBooleanObject(ResultSet rs, String col) throws SQLException {
		return getBooleanObject(rs, col, false);
	}
	
	public static Boolean getBooleanObject(ResultSet rs, int col, boolean allowNull) throws SQLException {
		try {
			String s = getStringObject(rs, col, allowNull);
			return getBooleanObject(s, allowNull);
		} catch (ClassCastException e) {
			// workarround to support real boolean values in the DB
			Boolean b = (Boolean) rs.getObject(col);
			return getBooleanObject(b, allowNull);
		}
	}
	
	public static Boolean getBooleanObject(ResultSet rs, String col, boolean allowNull) throws SQLException {
		try {
			String s = getStringObject(rs, col, allowNull);
			return getBooleanObject(s, allowNull);			
		} catch (ClassCastException e) {
			// workarround to support real boolean values in the DB
			Boolean b = (Boolean) rs.getObject(col);
			return getBooleanObject(b, allowNull);
		}
	}
	
	private static Boolean getBooleanObject(String value, boolean allowNull) throws SQLException {
		if (value == null) {
			if (allowNull) {
				return null;
			} else {
				return DEFAULT_BOOLEAN;
			}
		} else {
			return value.trim().equalsIgnoreCase("y")?Boolean.TRUE:Boolean.FALSE;
		}
	}
	
	private static Boolean getBooleanObject(Boolean value, boolean allowNull) throws SQLException {
		if (value == null) {
			if (allowNull) {
				return null;
			} else {
				return DEFAULT_BOOLEAN;
			}
		} else {
			return value;
		}
	}
	
	
	
	// Date methods follow
	
	public static Date getDateValue(ResultSet rs, int col) throws SQLException {
		return getDateObject(rs, col);
	}
	
	public static Date getDateValue(ResultSet rs, String col) throws SQLException {
		return getDateObject(rs, col);
	}

	public static Date getDateObject(ResultSet rs, int col) throws SQLException {
		return getDateObject(rs.getTimestamp(col));
	}
	
	public static Date getDateObject(ResultSet rs, String col) throws SQLException {
		return getDateObject(rs.getTimestamp(col));
	}
	
	private static Date getDateObject(Object o) throws SQLException {
		if (o == null) {
			return null;
		}
		long longTime = ((Timestamp)o).getTime();
		return new Date(longTime);
	}
	
	
	
	public static LocalDate getLocalDate(ResultSet rs, int col) throws SQLException {
		return getLocalDate(rs.getTimestamp(col));
	}

	public static LocalDate getLocalDate(ResultSet rs, String col) throws SQLException {
		return getLocalDate(rs.getTimestamp(col));
	}
	
	private static LocalDate getLocalDate(Timestamp t) throws SQLException {
		return Utils.toLocalDate(t);
	}

	public static LocalTime getLocalTime(ResultSet rs, int col) throws SQLException {
		return getLocalTime(rs.getTimestamp(col));
	}

	public static LocalTime getLocalTime(ResultSet rs, String col) throws SQLException {
		return getLocalTime(rs.getTimestamp(col));
	}
	
	private static LocalTime getLocalTime(Timestamp t) throws SQLException {
		return Utils.toLocalTime(t);
	}

	public static LocalDateTime getLocalDateTime(ResultSet rs, int col) throws SQLException {
		return getLocalDateTime(rs.getTimestamp(col));
	}

	public static LocalDateTime getLocalDateTime(ResultSet rs, String col) throws SQLException {
		return getLocalDateTime(rs.getTimestamp(col));
	}
	
	private static LocalDateTime getLocalDateTime(Timestamp t) throws SQLException {
		return Utils.toLocalDateTime(t);
	}
	

	
	public static byte[] getBlobAsBytes(ResultSet rs, int col, LobHandler lobHandler) throws SQLException {
		return lobHandler.getBlobAsBytes(rs, col);
	}
	
	public static byte[] getBlobAsBytes(ResultSet rs, String col, LobHandler lobHandler) throws SQLException {
		return lobHandler.getBlobAsBytes(rs, col);
	}

	public static InputStream getBlobAsInputStream(ResultSet rs, int col, LobHandler lobHandler) throws SQLException {
		return lobHandler.getBlobAsBinaryStream(rs, col);
	}
	
	public static InputStream getBlobAsInputStream(ResultSet rs, String col, LobHandler lobHandler) throws SQLException {
		return lobHandler.getBlobAsBinaryStream(rs, col);
	}
	
	
	
	// array methods follow

	public static String[] getStringArray(ResultSet rs, int col) throws SQLException {
		return getStringArray(rs, col, false);
	}

	public static String[] getStringArray(ResultSet rs, String col) throws SQLException {
		return getStringArray(rs, col, false);
	}
	
	public static String[] getStringArray(ResultSet rs, int col, boolean allowNull) throws SQLException {
		return getStringArray(rs.getArray(col), allowNull);
	}
	
	public static String[] getStringArray(ResultSet rs, String col, boolean allowNull) throws SQLException {
		return getStringArray(rs.getArray(col), allowNull);
	}
	
	private static String[] getStringArray(Array array, boolean allowNull) throws SQLException {
		if (array == null) {
			if (allowNull) {
				return null;
			} else {
				return DEFAULT_STRING_ARRAY;
			}
		} else {
			try {
				Object object = array.getArray();
				if (!(object instanceof String[])) {
					throw new ClassCastException("Column value can not be converted to String[].");
				}
				return (String[]) object;
			} finally {
				com.asentinel.common.jdbc.JdbcUtils.cleanupArray(array);
			}
		}
	}

	public static Number[] getNumberArray(ResultSet rs, int col) throws SQLException {
		return getNumberArray(rs, col, false);
	}

	public static Number[] getNumberArray(ResultSet rs, String col) throws SQLException {
		return getNumberArray(rs, col, false);
	}
	
	public static Number[] getNumberArray(ResultSet rs, int col, boolean allowNull) throws SQLException {
		return getNumberArray(rs.getArray(col), allowNull);
	}
	
	public static Number[] getNumberArray(ResultSet rs, String col, boolean allowNull) throws SQLException {
		return getNumberArray(rs.getArray(col), allowNull);
	}

	private static Number[] getNumberArray(Array array, boolean allowNull) throws SQLException {
		if (array == null) {
			if (allowNull) {
				return null;
			} else {
				return DEFAULT_NUMBER_ARRAY;
			}
		} else {
			try {
				Object object = array.getArray();
				if (!(object instanceof Number[])) {
					throw new ClassCastException("Column value can not be converted to Number[].");
				}
				return (Number[]) object;
			} finally {
				com.asentinel.common.jdbc.JdbcUtils.cleanupArray(array);
			}
		}
	}
	

	public static int[] getIntArray(ResultSet rs, int col) throws SQLException {
		return getIntArray(rs, col, false);
	}

	public static int[] getIntArray(ResultSet rs, String col) throws SQLException {
		return getIntArray(rs, col, false);
	}
	
	public static int[] getIntArray(ResultSet rs, int col, boolean allowNull) throws SQLException {
		return getIntArray(rs.getArray(col), allowNull);
	}
	
	public static int[] getIntArray(ResultSet rs, String col, boolean allowNull) throws SQLException {
		return getIntArray(rs.getArray(col), allowNull);
	}

	private static int[] getIntArray(Array array, boolean allowNull) throws SQLException {
		Number[] numbers = getNumberArray(array, allowNull);
		if (numbers == null) {
			return null;
		} else {
			int[] ints = new int[numbers.length];
			for (int i=0; i < ints.length; i++) {
				ints[i] = numbers[i].intValue();
			}
			return ints;
		}
	}
	
	public static long[] getLongArray(ResultSet rs, int col) throws SQLException {
		return getLongArray(rs, col, false);
	}

	public static long[] getLongArray(ResultSet rs, String col) throws SQLException {
		return getLongArray(rs, col, false);
	}
	
	public static long[] getLongArray(ResultSet rs, int col, boolean allowNull) throws SQLException {
		return getLongArray(rs.getArray(col), allowNull);
	}
	
	public static long[] getLongArray(ResultSet rs, String col, boolean allowNull) throws SQLException {
		return getLongArray(rs.getArray(col), allowNull);
	}

	private static long[] getLongArray(Array array, boolean allowNull) throws SQLException {
		Number[] numbers = getNumberArray(array, allowNull);
		if (numbers == null) {
			return null;
		} else {
			long[] longs = new long[numbers.length];
			for (int i=0; i < longs.length; i++) {
				longs[i] = numbers[i].longValue();
			}
			return longs;
		}
	}
	

	public static double[] getDoubleArray(ResultSet rs, int col) throws SQLException {
		return getDoubleArray(rs, col, false);
	}

	public static double[] getDoubleArray(ResultSet rs, String col) throws SQLException {
		return getDoubleArray(rs, col, false);
	}
	
	public static double[] getDoubleArray(ResultSet rs, int col, boolean allowNull) throws SQLException {
		return getDoubleArray(rs.getArray(col), allowNull);
	}
	
	public static double[] getDoubleArray(ResultSet rs, String col, boolean allowNull) throws SQLException {
		return getDoubleArray(rs.getArray(col), allowNull);
	}

	private static double[] getDoubleArray(Array array, boolean allowNull) throws SQLException {
		Number[] numbers = getNumberArray(array, allowNull);
		if (numbers == null) {
			return null;
		} else {
			double[] doubles = new double[numbers.length];
			for (int i=0; i < doubles.length; i++) {
				doubles[i] = numbers[i].doubleValue();
			}
			return doubles;
		}
	}
	
	public static BigInteger[] getBigIntegerArray(ResultSet rs, int col) throws SQLException {
		return getBigIntegerArray(rs, col, false);
	}

	public static BigInteger[] getBigIntegerArray(ResultSet rs, String col) throws SQLException {
		return getBigIntegerArray(rs, col, false);
	}
	
	public static BigInteger[] getBigIntegerArray(ResultSet rs, int col, boolean allowNull) throws SQLException {
		return getBigIntegerArray(rs.getArray(col), allowNull);
	}
	
	public static BigInteger[] getBigIntegerArray(ResultSet rs, String col, boolean allowNull) throws SQLException {
		return getBigIntegerArray(rs.getArray(col), allowNull);
	}

	private static BigInteger[] getBigIntegerArray(Array array, boolean allowNull) throws SQLException {
		Number[] numbers = getNumberArray(array, allowNull);
		if (numbers == null) {
			return null;
		} else {
			BigInteger[] bis = new BigInteger[numbers.length];
			for (int i=0; i < bis.length; i++) {
				if (numbers[i] instanceof BigDecimal) {
					bis[i] = ((BigDecimal) numbers[i]).toBigInteger();	
				} else {
					bis[i] = new BigInteger(String.valueOf(numbers[i].longValue()));
				}
			}
			return bis;
		}
	}
	
	/**
	 * Reads and converts a {@code ResultSet} column to an enum. There are 2 ways
	 * the conversion is achieved:
	 * <li>if the enum implements {@link EnumId} than the value extracted from the
	 * column is matched against each of the enum ids. The enum constant that
	 * matches the database id is selected. Note that for string enum ids the match
	 * is case sensitive.
	 * <li>otherwise the value of the column (assumed to be of type string) is case
	 * insensitive matched against each of the names of the enum constants. The
	 * first enum constant that matches the database string value is selected.
	 * 
	 * @param <T>       the actual enum type
	 * @param rs        the resultset
	 * @param column    the name of the column in the resultset
	 * @param enumClass the enum actual class
	 * @return the enum constant extracted from the specified column
	 * @throws SQLException          if the conversion to enum failed or the column
	 *                               can not be read for some other reason
	 * @throws IllegalStateException if the target enum does not correctly implement
	 *                               the {@link EnumId} contract
	 * 
	 * @see EnumId
	 */
	public static <T extends Enum<T>> Enum<T> getEnum(ResultSet rs, String column, Class<T> enumClass) throws SQLException {
		T[] values = enumClass.getEnumConstants();
		if (values == null || values.length == 0) {
			throw new IllegalStateException("Enum " + enumClass.getName() + " does not have any declared constants.");
		}
		if (EnumId.class.isAssignableFrom(enumClass)) {
			// TODO: we get the type of the id by checking the first constant in the enum, we may
			// need to validate that the others have the same type
			Object id = ((EnumId<?>) values[0]).getId();
			if (id == null) {
				throw new IllegalStateException("Enum " + enumClass.getName() + " implements " + EnumId.class.getSimpleName() 
						+ " incorrectly. At least one of the enum instances has a null id.");
			}
			Object dbValue;
			if (id instanceof Integer) {
				dbValue = getIntObject(rs, column, true);
			} else if (id instanceof Long) {
				dbValue = getLongObject(rs, column, true);
			} else if (id instanceof String) {
				dbValue = getStringObject(rs, column, true);
				if (!StringUtils.hasLength((String) dbValue)) {
					dbValue = null;
				}
			// TODO: add other id types as needed
			} else {
				throw new IllegalStateException("Enum " + enumClass.getName() + " implements " + EnumId.class.getSimpleName() 
						+ " incorrectly. At least one of the enum instances returns an unsupported id type: " + id.getClass());
			}
			if (dbValue == null) {
				return null;
			}
			for (T value: values) {
				Object valueId = ((EnumId<?>) value).getId();
				if (valueId == null) {
					throw new IllegalStateException("Enum " + enumClass.getName() + " implements " + EnumId.class.getSimpleName() 
							+ " incorrectly. At least one of the enum instances has a null id.");
				}
				if (valueId.equals(dbValue)) {
					return value;
				}
			}
			throw new SQLException("Failed to convert the value " + dbValue + " to " + enumClass.getName()
					+ ". Valid values are " 
					+ Arrays.asList(values)
							.stream()
							.map(v -> ((EnumId<?>) v).getId())
							.map(String::valueOf).collect(joining(", ")) + ".");
		} else {
			String dbValue = getStringObject(rs, column, true);
			if (!StringUtils.hasLength(dbValue)) {
				return null;
			}
			for (T value: values) {
				if (value.toString().equalsIgnoreCase(dbValue)) {
					return value;
				}
			}
			throw new SQLException("Failed to convert the value '" + dbValue + "' to " + enumClass.getName()
					+ "'. Valid case insensitive values are " 
					+ Arrays.asList(values)
							.stream()
							.map(String::valueOf).collect(joining(", ")) + ".");
		}
	}
	
}



/**
 * Decorator for a {@link RowCallbackHandler}.
 * The purpose of this class is to count the rows in the
 * resultset. Useful for logging.
 */
class RowCallbackHandlerDecorator implements RowCallbackHandler {
	private final RowCallbackHandler rowCallbackHandler;
	private int size = 0;
	
	RowCallbackHandlerDecorator(RowCallbackHandler rowCallbackHandler) {
		Assert.assertNotNull(rowCallbackHandler, "rowCallbackHandler");
		this.rowCallbackHandler = rowCallbackHandler;
	}

	@Override
	public void processRow(ResultSet rs) throws SQLException {
		rowCallbackHandler.processRow(rs);
		size++;
	}
	
	int size() {
		return size;
	}

}

