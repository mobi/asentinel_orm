package com.asentinel.common.jdbc;

import static com.asentinel.common.jdbc.ResultSetUtils.*;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
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
	
	private volatile ConversionService conversionService;

	/**
	 * @see #setConversionService(ConversionService)
	 */
	public ConversionService getConversionService() {
		return conversionService;
	}

	/**
	 * Sets a {@code ConversionService} implementation to be used as fallback when
	 * the resultset value can not be converted to the target field type. Allows
	 * mapping custom database types and/or custom domain types.
	 */
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	protected final Object getValueInternal(Object parentObject, TypeDescriptor targetDescriptor, ResultSet rs, ColumnMetadata columnMetadata) throws SQLException {
		try {
			return getValue(parentObject, targetDescriptor, rs, columnMetadata);
		} catch (ClassCastException | SQLException e) {
			throw new SQLException("Can not convert SQL type to argument type for element " + targetDescriptor + " .", e);
		}
	}

	/**
	 * This method reads the value of the specified column from the resultset. It
	 * can be overridden by subclasses to customize the extraction.
	 * 
	 * @param parentObject         the object to which the field that we are reading
	 *                             from the {@code ResultSet} belongs to.
	 * @param targetDescriptor the type to convert to.
	 * @param rs                   the {@code ResultSet}.
	 * @param columnMetadata       information about the {@code ResultSet} column to
	 *                             read.
	 * @return the value extracted from the {@code ResultSet} specified column
	 * 
	 * @throws SQLException
	 */
	@SuppressWarnings({"rawtypes", "unchecked" })
	protected Object getValue(Object parentObject, TypeDescriptor targetDescriptor, ResultSet rs, ColumnMetadata columnMetadata) throws SQLException {
		Class<?> targetType = targetDescriptor.getType();
		String column = columnMetadata.getResultsetName();
		boolean allowNull = columnMetadata.isAllowNull();
		if (targetType == int.class) {
			return getIntObject(rs, column);
		} else if (targetType == String.class) {
			return getStringObject(rs, column, allowNull);
		} else if (targetType == double.class) {
			return getDoubleObject(rs, column);
		} else if (targetType == boolean.class) {
			return getBooleanObject(rs, column);
		} else if (targetType == Date.class) {
			return getDateObject(rs, column);
		} else if (targetType == LocalDate.class) {
			return getLocalDate(rs, column);
		} else if (targetType == LocalTime.class) {
			return getLocalTime(rs, column);
		} else if (targetType == LocalDateTime.class) {
			return getLocalDateTime(rs, column);
		} else if (targetType == Instant.class) {
			return getInstant(rs, column);
		} else if (targetType == long.class) {
			return getLongObject(rs, column);
		} else if (targetType == Integer.class) {
			return getIntObject(rs, column, allowNull);
		} else if (targetType == Double.class) {			
			return getDoubleObject(rs, column, allowNull);
		} else if (targetType == Boolean.class ) {			
			return getBooleanObject(rs, column, allowNull);
		} else if (targetType == Long.class) {			
			return getLongObject(rs, column, allowNull);
		} else if (targetType == BigDecimal.class) {
			return getBigDecimalObject(rs, column, allowNull);
		} else if (targetType == BigInteger.class) {
			return getBigIntegerObject(rs, column, allowNull);
		} else if (targetType == Number.class) {
			return getBigDecimalObject(rs, column, allowNull);
		} else if (Enum.class.isAssignableFrom(targetType)) {
			return getEnum(rs, column, (Class<Enum>) targetType);
		} else if (targetType == byte[].class) {
			return getBlobAsBytes(rs, column, getLobHandler());
		} else if (targetType == INPUT_STREAM_TYPE) {
			// TODO: load eager if the column is present in resultset, lazy otherwise
			if (getQueryExecutor() == null) {
				// eager load the blob
				return getBlobAsInputStream(rs, column, getLobHandler());
			} else {
				// create a lazy loading proxy for the blob, the blob column should not be in the resultset and
				// the parentObject must be a annotated entity (with @Table and @PkColumn)
				return new InputStreamProxy(getQueryExecutor(), getLobHandler(), parentObject, columnMetadata.getMappedName());
			}
		} else if (targetType == String[].class) {
			return getStringArray(rs, column, allowNull);
		} else if (targetType == int[].class) {
			return getIntArray(rs, column, allowNull);
		} else if (targetType == long[].class) {
			return getLongArray(rs, column, allowNull);
		} else if (targetType == double[].class) {
			return getDoubleArray(rs, column, allowNull);
		} else if (targetType == BigDecimal[].class) {
			Number[] numbers = getNumberArray(rs, column, allowNull);
			if (numbers instanceof BigDecimal[]) {
				return (BigDecimal[]) numbers;
			} else {
				throw new SQLException("Unable to convert to BigDecimal[]");
			}
		} else if (targetType == BigInteger[].class) {
			return getBigIntegerArray(rs, column, allowNull);
		} else if (targetType == Number[].class) {
			return getNumberArray(rs, column, allowNull);
		// TODO: add support for Date[], Boolean[]
		} else {
			// we fallback to the conversion service if one is available
			return customConvert(targetDescriptor, rs, column);
		}
	}

	protected final Object customConvert(TypeDescriptor targetDescriptor, ResultSet rs, String column) throws SQLException {
		if (conversionService != null) {
			Object object = rs.getObject(column);
			if (object == null) {
				return null;
			}
			TypeDescriptor sourceDescriptor = TypeDescriptor.valueOf(object.getClass());
			if (conversionService.canConvert(sourceDescriptor, targetDescriptor)) {
				return conversionService.convert(object, sourceDescriptor, targetDescriptor);
			}
		}
		// no luck with the conversion service, we error out
		throw new SQLException("Unsupported property type " +  targetDescriptor.getType().getName() + ".");
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
