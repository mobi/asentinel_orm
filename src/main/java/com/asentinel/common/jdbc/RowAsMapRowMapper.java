package com.asentinel.common.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

import com.asentinel.common.util.Assert;

/**
 * RowMapper that maps a ResultSet row to a {@link Map}. The map returned
 * is created by an {@link ObjectFactory}. By default (if the no-args or the boolean arg constructor  is used) 
 * a LinkedHashMap will be returned by the {@link #mapRow(ResultSet, int)} method. This map
 * preserves the order of the columns. The returned LinkedHashMap is slightly altered to 
 * preprocess the key passed to its get method using the {@link #keyForColumn(String)} method.
 * This means that access to the default map is case-insensitive.
 * <br><br>
 * 
 * If the resultset contains multiple columns with the same name just one of the columns will be found in the resulting
 * map for each row.
 * <br><br>
 * 
 * Subclasses can override the method {@link #keyForColumn(String)} to provide their own key generation and also
 * {@link #valueForColumn(ResultSet, int)} to customize type conversion.
 * <br><br>  
 * 
 * Instances of this class should be used as effectively immutable objects.
 * <br><br>
 * 
 * @see AbstractRowMapper
 * @see AbstractRowMapper#valueForColumn(ResultSet, int) for details about conversion.
 * @see AbstractRowMapper.Flag for details about the available flags
 * 
 * @author Razvan Popian
 */
public class RowAsMapRowMapper extends AbstractRowMapper<Map<String, Object>> {
	
	private ObjectFactory<Map<String, Object>> mapFactory = new ObjectFactory<Map<String,Object>>() {
		@SuppressWarnings("serial")
		@Override
		public Map<String, Object> newObject() throws IllegalStateException {
			return new LinkedHashMap<String, Object>() {
				@Override
				public Object get(Object key) {
					return super.get(keyForColumn((String) key));
				}
			};
		}
	};
	
	/** 
	 * Simple constructor used to set the conversion flags.
	 * @param flags that will drive the behavior of converting SQL types to Java.
	 * 
	 * @see AbstractRowMapper.Flag
	 */
	public RowAsMapRowMapper(EnumSet<Flag> flags) {
		super(flags);
	}
	
	/**
	 * Constructor used when there is a need to customize the
	 * map creation.
	 * @param mapFactory factory that creates a Map.
	 * @param flags that will drive the behavior of converting SQL types to Java.
	 * 
	 * @see AbstractRowMapper.Flag
	 */
	public RowAsMapRowMapper(ObjectFactory<Map<String, Object>> mapFactory, EnumSet<Flag> flags) {
		super(flags);
		Assert.assertNotNull(mapFactory, "mapFactory");
		this.mapFactory = mapFactory;
	}
	
	
	/** No args constructor for default initialization */
	public RowAsMapRowMapper() {
		
	}
	
	/**
	 * Constructor used when there is a need to customize the
	 * map creation.
	 */
	public RowAsMapRowMapper(ObjectFactory<Map<String, Object>> mapFactory) {
		this(mapFactory, EnumSet.noneOf(Flag.class));
	}
	
	
	/**
	 * @param name the column name as found in ResultSetMetaData.
	 * @return the key for the column.
	 */
	protected String keyForColumn(String name) {
		return name.toUpperCase();
	}

	
	@Override
	public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
		Map<String, Object> map = mapFactory.newObject();
		int colCount = rs.getMetaData().getColumnCount();
		for (int col = 1; col <= colCount; col++) {
			map.put(keyForColumn(rs.getMetaData().getColumnName(col)), valueForColumn(rs, col));
		}
		return map;
	}
	
	
	@Override
	public String toString() {
		return "RowAsMapRowMapper [mapFactory=" + mapFactory + "]";
	}

}
