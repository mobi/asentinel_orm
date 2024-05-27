package com.asentinel.common.jdbc;

import static com.asentinel.common.jdbc.ResultSetUtils.getBigDecimalObject;
import static com.asentinel.common.jdbc.ResultSetUtils.getBigIntegerArray;
import static com.asentinel.common.jdbc.ResultSetUtils.getBigIntegerObject;
import static com.asentinel.common.jdbc.ResultSetUtils.getBlobAsBytes;
import static com.asentinel.common.jdbc.ResultSetUtils.getBlobAsInputStream;
import static com.asentinel.common.jdbc.ResultSetUtils.getBooleanObject;
import static com.asentinel.common.jdbc.ResultSetUtils.getDateObject;
import static com.asentinel.common.jdbc.ResultSetUtils.getDoubleArray;
import static com.asentinel.common.jdbc.ResultSetUtils.getDoubleObject;
import static com.asentinel.common.jdbc.ResultSetUtils.getEnum;
import static com.asentinel.common.jdbc.ResultSetUtils.getIntArray;
import static com.asentinel.common.jdbc.ResultSetUtils.getIntObject;
import static com.asentinel.common.jdbc.ResultSetUtils.getLocalDate;
import static com.asentinel.common.jdbc.ResultSetUtils.getLocalDateTime;
import static com.asentinel.common.jdbc.ResultSetUtils.getLocalTime;
import static com.asentinel.common.jdbc.ResultSetUtils.getLongArray;
import static com.asentinel.common.jdbc.ResultSetUtils.getLongObject;
import static com.asentinel.common.jdbc.ResultSetUtils.getNumberArray;
import static com.asentinel.common.jdbc.ResultSetUtils.getStringArray;
import static com.asentinel.common.jdbc.ResultSetUtils.getStringObject;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import com.asentinel.common.orm.proxy.InputStreamProxy;

/**
 * Class that handles conversion of the JDBC types to java types. This is
 * intended to be a base class for {@link RowMapper} implementations. <br><br>
 * Instances of this class should be used as effectively immutable objects.
 * 
 * @author Razvan Popian
 */
public abstract class ConversionSupport extends LobHandlerSupport {
	
	public static final Class<InputStream> INPUT_STREAM_TYPE = InputStream.class;
	
	/**
	 * This method reads the value of the specified column from the resultset. It
	 * can be overridden by subclasses to customize the extraction.
	 * 
	 * @param parentObject   the object to which the field that we are reading from
	 *                       the {@code ResultSet} belongs to.
	 * @param cls            the type to convert to.
	 * @param rs             the {@code ResultSet}.
	 * @param columnMetadata information about the {@code ResultSet} column to read.
	 * @return the value extracted from the {@code ResultSet} specified column
	 * 
	 * @throws SQLException
	 */
	@SuppressWarnings({"rawtypes", "unchecked" })
	protected Object getValue(Object parentObject, Class<?> cls, ResultSet rs, ColumnMetadata columnMetadata) throws SQLException {
		// TODO: integrate the Spring Converter Framework to make the type conversions more flexible
		// This may only work for AnnotationRowMapper. The @PkColumn/@Column annotations should
		// allow the user to specify what converter to use for reading data from the DB and
		// what converter to user for writing data
		String column = columnMetadata.getResultsetName();
		boolean allowNull = columnMetadata.isAllowNull();
		if (cls == int.class) {
			return getIntObject(rs, column);
		} else if (cls == String.class) {
			return getStringObject(rs, column, allowNull);
		} else if (cls == double.class) {
			return getDoubleObject(rs, column);
		} else if (cls == boolean.class) {
			return getBooleanObject(rs, column);
		} else if (cls == Date.class) {
			return getDateObject(rs, column);
		} else if (cls == LocalDate.class) {
			return getLocalDate(rs, column);
		} else if (cls == LocalTime.class) {
			return getLocalTime(rs, column);
		} else if (cls == LocalDateTime.class) {
			return getLocalDateTime(rs, column);
		} else if (cls == long.class) {
			return getLongObject(rs, column);
		} else if (cls == Integer.class) {
			return getIntObject(rs, column, allowNull);
		} else if (cls == Double.class) {			
			return getDoubleObject(rs, column, allowNull);
		} else if (cls == Boolean.class ) {			
			return getBooleanObject(rs, column, allowNull);
		} else if (cls == Long.class) {			
			return getLongObject(rs, column, allowNull);
		} else if (cls == BigDecimal.class) {
			return getBigDecimalObject(rs, column, allowNull);
		} else if (cls == BigInteger.class) {
			return getBigIntegerObject(rs, column, allowNull);
		} else if (cls == Number.class) {
			return getBigDecimalObject(rs, column, allowNull);
		} else if (Enum.class.isAssignableFrom(cls)) {
			return getEnum(rs, column, (Class<Enum>) cls);
		} else if (cls == byte[].class) {
			return getBlobAsBytes(rs, column, getLobHandler());
		} else if (cls == INPUT_STREAM_TYPE) {
			// TODO: load eager if the column is present in resultset, lazy otherwise
			if (getQueryExecutor() == null) {
				// eager load the blob
				return getBlobAsInputStream(rs, column, getLobHandler());
			} else {
				// create a lazy loading proxy for the blob, the blob column should not be in the resultset and
				// the parentObject must be a annotated entity (with @Table and @PkColumn)
				return new InputStreamProxy(getQueryExecutor(), getLobHandler(), parentObject, columnMetadata.getMappedName());
			}
		} else if (cls == String[].class) {
			return getStringArray(rs, column, allowNull);
		} else if (cls == int[].class) {
			return getIntArray(rs, column, allowNull);
		} else if (cls == long[].class) {
			return getLongArray(rs, column, allowNull);
		} else if (cls == double[].class) {
			return getDoubleArray(rs, column, allowNull);
		} else if (cls == BigDecimal[].class) {
			Number[] numbers = getNumberArray(rs, column, allowNull);
			if (numbers instanceof BigDecimal[]) {
				return (BigDecimal[]) numbers;
			} else {
				throw new SQLException("Unable to convert to BigDecimal[]");
			}
		} else if (cls == BigInteger[].class) {
			return getBigIntegerArray(rs, column, allowNull);
		} else if (cls == Number[].class) {
			return getNumberArray(rs, column, allowNull);
		// TODO: add support for Date[], Boolean[]
		} else {
			throw new SQLException("Unsupported property type " + cls.getName() + ".");
		}
	}
	
	
	/**
	 * @return {@code true} if the {@code targetMemberType} parameter is an
	 *         {@code InputStream} and the {@code rowMapper} parameter is prepared
	 *         for proxying input streams, {@code false} otherwise
	 */
	public static boolean isPreparedForProxyingInputStreams(Class<?> targetMemberType, RowMapper<?> rowMapper) {
		return targetMemberType == ConversionSupport.INPUT_STREAM_TYPE
				&& rowMapper instanceof ConversionSupport
				&& ((ConversionSupport) rowMapper).getQueryExecutor() != null;
	}
	
}
