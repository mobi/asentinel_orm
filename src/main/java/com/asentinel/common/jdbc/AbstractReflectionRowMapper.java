package com.asentinel.common.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.util.Assert;

/**
 * Skeleton {@code RowMapper} implementation. It defines methods for creating the object that will be read from a row
 * and for populating that object with the row data. Instances of this class should be reusable and thread safe.
 * <br>
 * 
 * There is one abstract method to be overridden in subclasses {@link #populateTarget(Object, ResultSet)}. 
 * Optionally subclasses can also override the following methods to customize the column data extraction:<br>
 * 		<li> {@link #createTarget(ResultSet)}
 * 		<li> {@link #getValue(Object, Method, ResultSet, ColumnMetadata)}
 * 		<li> {@link #getValue(Object, Field, ResultSet, ColumnMetadata)}
 * 		<li> {@link #setValue(Object, Method, Object)}
 * 		<li> {@link #setValue(Object, Field, Object)}
 *   
 * @author Razvan Popian
 */
public abstract class AbstractReflectionRowMapper<T> extends ConversionSupport implements RowMapper<T> {
	
	protected final ObjectFactory<T> objectFactory;
	
	protected AbstractReflectionRowMapper(ObjectFactory<T> objectFactory) {
		Assert.assertNotNull(objectFactory, "objectFactory");
		this.objectFactory = objectFactory;
	}
	
	
	protected AbstractReflectionRowMapper(Class<T> clasz) {
		this(new DefaultObjectFactory<T>(clasz));
	}
	
	
	// Row mapping logic methods follow: 
	
	/**
	 * Create the target entity. Can be overridden in subclasses to provide
	 * access to the {@code ResultSet} in the object creation phase. 
	 * @param rs the resultset.
	 * @return a new entity.
	 * @throws SQLException
	 */
	protected T createTarget(ResultSet rs) throws SQLException {
		return objectFactory.newObject();
	}

	
	/**
	 * Sets the fields of the target object. 
	 * @param object target object.
	 * @param rs the resultset.
	 * @throws SQLException
	 */
	protected abstract void populateTarget(T object, ResultSet rs) throws SQLException;
	

	@Override
	public final T mapRow(ResultSet rs, int rowNum) throws SQLException {
		T object = createTarget(rs);
		populateTarget(object, rs);
		return object;
	}
	
	
	// Helper methods for subclasses:
	
	private String createSQLExceptionMessage(Member member) {
		return "Can not convert SQL type to argument type for element '" + member + "' ."; 
	}
	
	/**
	 * This method reads the value of the specified column from the resultset. It
	 * can be overridden by subclasses to customize the extraction.
	 *
	 * @param object         the object that is being mapped for the current row.
	 * @param method         the method that will be called to set the value of the
	 *                       column
	 * @param rs             the resultset
	 * @param columnMetadata information about the {@code ResultSet} column to read.
	 * @return the value extracted from the resultset specified column
	 * @throws SQLException
	 */
	protected Object getValue(T object, Method method, ResultSet rs, ColumnMetadata columnMetadata) throws SQLException {
		if (method.getParameterTypes().length != 1) {
			throw new IllegalArgumentException("The method " + method + " should have exactly one parameter.");
		}
		Class<?> cls = method.getParameterTypes()[0]; 
		try {
			return getValue(object, cls, rs, columnMetadata);
		} catch (ClassCastException | SQLException e) {
			throw new SQLException(createSQLExceptionMessage(method), e);
		}
	}
	
	/**
	 * Sets the value in the target object. Subclasses can override this method
	 * to customize the call.
	 * @param object the target object
	 * @param method the method to invoke on the target object
	 * @param value the parameter of the method.
	 * @throws SQLException if the method call fails.
	 */
	protected void setValue(T object, Method method, Object value) throws SQLException {
		try {
			ReflectionUtils.makeAccessible(method);
			method.invoke(object, value);
		} catch (Exception e) {
			String valueClass = value==null ? "null" : value.getClass().getName();
			throw new SQLException("Failed to call method '" + method + "'" 
					+ " for value " + value + " with type " + valueClass
					+ ".", e);
		}
	}

	/**
	 * This method reads the value of the specified column from the resultset. It
	 * can be overridden by subclasses to customize the extraction.
	 * 
	 * @param object         the object that is being mapped for the current row.
	 * @param field          the field on which the value of the column will be set.
	 * @param rs             the resultset.
	 * @param columnMetadata information about the {@code ResultSet} column to read.
	 * @return the value extracted from the resultset specified column.
	 * @throws SQLException
	 */
	protected Object getValue(T object, Field field, ResultSet rs, ColumnMetadata columnMetadata) throws SQLException {
		Class<?> cls = field.getType(); 
		try {
			return getValue(object, cls, rs, columnMetadata);
		} catch (ClassCastException | SQLException e) {
			throw new SQLException(createSQLExceptionMessage(field), e);
		}
	}
	

	/**
	 * Sets the value in the target object. Subclasses can override this method
	 * to customize the call.
	 * @param object the target object
	 * @param field the field on which the value will be set.
	 * @param value the parameter of the method.
	 * @throws SQLException if the method call fails.
	 */
	protected void setValue(T object, Field field, Object value) throws SQLException {
		try {
			ReflectionUtils.makeAccessible(field);
			ReflectionUtils.setField(field, object, value);
		} catch (Exception e) {
			String valueClass = value==null ? "null" : value.getClass().getName();
			throw new SQLException("Failed to set field '" + field + "'" 
					+ " for value " + value + " with type " + valueClass
					+ ".", e);
		}
	}

}
