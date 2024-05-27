package com.asentinel.common.jdbc;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <code>RowMapper</code> implementation that attempts to extract each field from
 * a {@link ResultSet} row by matching the name of the field with the names of the SET methods
 * of the objects created by the encapsulated {@link ObjectFactory} instance. The method matching
 * is not case sensitive, so if 2 or more set methods with the same case insensitive signature exist, 
 * this class will pick the first one.<br><br>
 * 
 * This class has 2 operation modes:<br>
 * 		<li> "exception for missing setter mode", exceptions are thrown if methods are missing in the target object. This
 * 			is the DEFAULT.
 * 		<li> "best effort" mode, no exception is thrown for missing methods, this class will attempt
 * 			to set as many fields as possible in the target object.
 * There are constructors for initializing instances of this class in both modes.<br><br>
 *  
 * Exceptions are thrown by the {@link #mapRow(ResultSet, int)} method if:<br>
 * 		<li> the setter method parameter class does not match the type of the value coming from the DB;<br>
 * 		<li> the setter method parameter is not supported;<br>
 * 		<li> one column from the resultset does not have a matching setter method in the objects
 * 			created by the ObjectFactory and the class does not operate in "best effort" mode.
 * <br><br>
 * 
 * Instances of this class should be used as effectively immutable objects.
 * <br><br>
 * 
 * @see #mapRow(ResultSet, int)
 * @see ObjectFactory
 * @see DefaultObjectFactory
 * 
 * @author Razvan Popian
 */
public class ReflectionRowMapper<T> extends AbstractReflectionRowMapper<T> {
	private final static Logger log = LoggerFactory.getLogger(ReflectionRowMapper.class);

	private final Map<String, Method> mapColumnToMethod = new HashMap<>();
	private final boolean bestEffort;
	

	/** 
	 * Constructor, sets the operation mode to "exception for missing setter" mode.
	 * Use {@link #ReflectionRowMapper(ObjectFactory)} with your own {@link ObjectFactory}
	 * implementation to improve performance. 
	 * @param clasz class for which to create objects for each row (must have a no-args constructor).
	 */
	public ReflectionRowMapper(Class<T> clasz) {
		this(clasz, false);
	}
	
	/** Constructor, sets the operation mode to "exception for missing setter" mode. */
	public ReflectionRowMapper(ObjectFactory<T> objectFactory) {
		this(objectFactory, false);
	}
	
	
	/** 
	 * Constructor, the operation mode can be customized.
	 * @see ReflectionRowMapper#ReflectionRowMapper(Class)
	 */
	public ReflectionRowMapper(Class<T> clasz, boolean bestEffort) {
		super(clasz);
		this.bestEffort = bestEffort;
		initialize(clasz);
	}
	
	/** Constructor, the operation mode can be customized. */
	@SuppressWarnings("unchecked")
	public ReflectionRowMapper(ObjectFactory<T> objectFactory, boolean bestEffort) {
		super(objectFactory);
		this.bestEffort = bestEffort;
		// TODO: an instance is needed here to get the object type. The instance is discarded.
		// We may need to find an alternative, we are creating an instance just to discard it.
		initialize((Class<T>) objectFactory.newObject().getClass());
	}
	
	
	private void initialize(Class<T> clasz) {
		Method[] methods = clasz.getMethods();
		for (Method method: methods) {
			if (method.getParameterTypes().length == 1
					&& method.getName().toLowerCase().startsWith("set")) {
				String columnName = method.getName().substring(3).toLowerCase();
				mapColumnToMethod.putIfAbsent(columnName, method);
			}
		}
	}

	/**
	 * Calculates the {@link ColumnMetadata}. This may be overridden
	 * by subclasses in case the column name in the resultset does not exactly match the
	 * name of the setter method.
	 * @param rsColumnName the actual name of the column in the resultset.
	 * @return the {@code ColumnMetadata}
	 */
	protected ColumnMetadata getColumnMetadata(String rsColumnName) {
		return new ColumnMetadata(rsColumnName);
	}
	
	@Override
	protected void populateTarget(T object, ResultSet rs) throws SQLException {
		for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
			String rsColumnName = rs.getMetaData().getColumnName(i);
			ColumnMetadata columnMetadata = getColumnMetadata(rsColumnName);
			if (columnMetadata == null) {
				// ignore the column
				continue;
			}
			String columnKey = columnMetadata.getMappedName().toLowerCase();
			Method method = mapColumnToMethod.get(columnKey);
			if (method == null) {
				String errorText = "Can not find 'set' method for resultset column '" 
						+ columnMetadata.getMappedName() + "' in class " 
						+ object.getClass().getName()
						+ ".";
				if (bestEffort) {
					if (log.isTraceEnabled()) {
						log.trace("populateTarget - Best effort mode: " + errorText);
					}
				} else {
					throw new SQLException(errorText);
				}
			} else {
				Object value = getValue(object, method, rs, columnMetadata);
				setValue(object, method, value);
			}
		}
	}
	
	
	public ObjectFactory<T> getObjectFactory() {
		return objectFactory;
	}
	
	public boolean isBestEffort() {
		return bestEffort;
	}
	
	@Override
	public String toString() {
		return "ReflectionRowMapper [objectFactory=" + objectFactory + "]";
	}

}
