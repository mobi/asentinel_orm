package com.asentinel.common.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import com.asentinel.common.util.Assert;

/**
 * Base class for row mappers that convert rows to maps, arrays etc. <br><br>
 *
 * Subclasses must override the abstract method {@link #mapRow(ResultSet, int)} and they can also override
 * {@link #valueForColumn(ResultSet, int)} to provide their own conversion from
 * SQL types to java types. The default implementation of this method uses {@link Flag} to drive its behaviour.
 * 
 * The default behavior uses ResultSetUtils#getXXXObject(ResultSet, int)
 * methods for known types and {@link ResultSet#getObject(int)} for other types. This means that
 * for known types default values will be returned for NULL columns. For example 0 is returned for
 * a NULL numeric column.
 * <br><br>  
 * 
 * Instances of this class should be used as effectively immutable objects.
 * 
 * @see Flag
 * @see EnumSet
 *  
 * @param <T> the type of the object returned by this mapper.
 * 
 * @author Razvan Popian
 */
public abstract class AbstractRowMapper<T> extends LobHandlerSupport implements RowMapper<T> {
	private final static Logger log = LoggerFactory.getLogger(AbstractRowMapper.class);
	
	private final EnumSet<Flag> flags;
	
	/**
	 * Default constructor, sets the <code>allNumbersAsBigDecimal</code> to false.
	 */
	public AbstractRowMapper() {
		this(EnumSet.noneOf(Flag.class));
	}
	
	/**
	 * Constructor that allows for the initialization of the <code>flags</code>.
	 * 
	 * @see Flag
	 * @see EnumSet
	 * 
	 * @param flags
	 */
	public AbstractRowMapper(EnumSet<Flag> flags) {
		Assert.assertNotNull(flags, "flags");
		this.flags = flags;
	}
	
	
	/**
	 * Convert the specified column to an object.
	 *  
	 * @param rs the ResultSet
	 * @param col the column index
	 * 
	 * @return Object resulted from converting the specified column using the following 
	 * 			conversion rules (assuming <code>allNumbersAsBigDecimal</code> is false):<br>
	 * 			- VARCHAR, CLOB, CHAR --&gt; String, if null defaults to "" or null depending on {@link #flags}<br>
	 * 			- TIMESTAMP, DATE --&gt; Date, if null defaults to null<br>
	 * 			- NUMERIC (int) --&gt; BigInteger, if null defaults to 0 or null depending on {@link #flags}<br>
	 * 			- NUMERIC (real) --&gt; BigDecimal, if null defaults to 0.0 or null depending on {@link #flags}<br>
	 * 			- BLOB --&gt; byte[] array, if null defaults to null<br>
	 * 			- all other types are converted using {@link ResultSet#getObject(int)}.<br>
	 * 			If {@link Flag#ALL_NUMBERS_AS_BIG_DECIMAL}is set this method returns BigDecimal for
	 * 			int types.
	 * 
	 * @throws SQLException
	 * 
	 * @see {@link ResultSetUtils}
	 */
	protected Object valueForColumn(ResultSet rs, int col) throws SQLException {
		Object o;
		ResultSetMetaData metaData = rs.getMetaData(); 
		int columnType = metaData.getColumnType(col); 
		boolean defaultToNull = flags.contains(Flag.DEFAULT_TO_NULL);
		switch (columnType) {
			case Types.VARCHAR:
			case Types.CLOB:
			case Types.CHAR:
				o = ResultSetUtils.getStringObject(rs, col, defaultToNull);
				break;
			case Types.TIMESTAMP:
			case Types.DATE:
				o = ResultSetUtils.getDateObject(rs, col);
				break;
			case Types.NUMERIC:
				if (flags.contains(Flag.ALL_NUMBERS_AS_BIG_DECIMAL)) {
					o = ResultSetUtils.getBigDecimalObject(rs, col, defaultToNull);
				} else {
					int scale = metaData.getScale(col);
					if ((scale != 0) 
						&& (scale != -127)) {
						o = ResultSetUtils.getBigDecimalObject(rs, col, defaultToNull);
					} else {
						o = ResultSetUtils.getBigIntegerObject(rs, col, defaultToNull);
					}
				}
				break;
			case Types.BLOB:
				o = ResultSetUtils.getBlobAsBytes(rs, col, getLobHandler());
				break;
			default:
				if (log.isDebugEnabled()) {
					log.debug("valueForColumn - WARNING: Sql Type " + columnType + " is converted using getObject().");
				}
				o = rs.getObject(col);
		}
		return o;
	}
	
	public boolean isAllNumbersAsBigDecimal() {
		return flags.contains(Flag.ALL_NUMBERS_AS_BIG_DECIMAL);
	}
	
	public boolean isDefaultToNull() {
		return flags.contains(Flag.DEFAULT_TO_NULL);
	}
	
	/**
	 * Enum containing all available flags that drive the behavior of the {@link AbstractRowMapper}
	 * <br /><br />
	 * Supported flags:
	 * 	<ul>
	 * 		<li>
	 * 			{@link Flag#ALL_NUMBERS_AS_BIG_DECIMAL} - if set all numbers pulled from the database will be converted 
	 * 				to BigDecimal regardless of their scale. If this flag is not set integers will be converted
	 * 				to BigInteger and decimal values will be converted to BigDecimal. <br /> 
	 * 				This flag exist to get around an Oracle driver bug. Oracle does not seem 
	 * 				to set the scale when the SQL sum command is used in the query.
	 * 		</li>
	 * 		<li>
	 * 			{@link Flag#DEFAULT_TO_NULL} - if set the conversion methods in this class will return null values 
	 * 				for database fields that are null. Otherwise default values will be returned (for null database
	 * 				string a java empty string will be returned, for a null database numeric field a value of 0 will be returned, etc). 
	 * 		</li>
	 * 	</ul>
	 * <br />
	 * 	All these flags are passed as an {@link EnumSet} to the constructor.
	 * <br />
	 * 	
	 * @see EnumSet
	 * 
	 * @author bogdan.popescu
	 *
	 */
	public static enum Flag {
		ALL_NUMBERS_AS_BIG_DECIMAL, DEFAULT_TO_NULL;
	}
	
}
