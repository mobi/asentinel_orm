package com.asentinel.common.jdbc.arrays;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

import org.springframework.jdbc.core.DisposableSqlTypeValue;
import org.springframework.jdbc.core.SqlTypeValue;

import com.asentinel.common.jdbc.InOutCall;
import com.asentinel.common.jdbc.JdbcUtils;
import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.util.Assert;

/**
 * Adapter class for a java array/collection to {@link SqlTypeValue}. 
 * It is useful for  setting an array/collection as argument to a {@link PreparedStatement}.
 * <br>
 * The implementation is not reusable. Instances must be created, passed to the {@link PreparedStatement}
 * and then discarded.
 * 
 * @see InOutCall
 * @see SqlQuery
 * 
 * @see NumberArray
 * @see StringArray
 * @see DateArray
 * 
 * @see ArrayFactory
 * 
 * @author Razvan Popian
 */
public class Array implements DisposableSqlTypeValue {
	
	private final ArrayFactory arrayFactory;
	private final String sqlTypeName;
	private final Object[] objects;
	
	private java.sql.Array arrayToPass;
	
	public Array(String sqlTypeName) {
		this(sqlTypeName, (Object[]) null);
	}
	
	public Array(String sqlTypeName, ArrayFactory arrayFactory) {
		this(sqlTypeName, null, arrayFactory);
	}
	
	public Array(String sqlTypeName, Object[] objects) {
		this(sqlTypeName, objects, new ArrayFactory() {});
	}
	
	public Array(String sqlTypeName, Object[] objects, ArrayFactory arrayFactory) {
		Assert.assertNotNull(arrayFactory, "arrayFactory");
		Assert.assertNotNull(sqlTypeName, "sqlTypeName");
		this.arrayFactory = arrayFactory;
		this.sqlTypeName = sqlTypeName;
		this.objects = objects;
	}

	public String getSqlTypeName() {
		return sqlTypeName;
	}
	
	public Object[] getObjects() {
		return objects;
	}

	@Override
	public final void setTypeValue(PreparedStatement ps, int paramIndex, 
			int sqlType, String typeName) throws SQLException {
		setTypeValue(ps, paramIndex);
	}
	
	/**
	 * @see #cleanup()
	 */
	protected void setTypeValue(PreparedStatement ps, int paramIndex) throws SQLException {
		if (objects == null) {
			ps.setNull(paramIndex, java.sql.Types.ARRAY, sqlTypeName);
			return;
		}
		// the sql Array is stored in a member field because 
		// we need to call #free() on it when we no longer need it.
		arrayToPass = arrayFactory.newArray(ps, sqlTypeName, objects);
		ps.setArray(paramIndex, arrayToPass);
	}
	
	/**
	 * @see #setTypeValue(PreparedStatement, int)
	 */
	@Override
	public void cleanup() {
		JdbcUtils.cleanupArray(arrayToPass);
	}
	
	// method used for testing only
	java.sql.Array getArrayToPass() {
		return arrayToPass;
	}
	
	
	static final int NO_ITEMS_IN_LOG = 5;
	
	@Override
	public String toString() {
		String name = getClass().getSimpleName();
		if (objects == null) {
			return String.format("%s [null]", name);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(name).append(" [size=").append(objects.length)
				.append(", items");
			if (objects.length <= NO_ITEMS_IN_LOG) {
				sb.append("=");
				sb.append(arrayToString(objects));
			} else {
				sb.append("(first ")
				.append(NO_ITEMS_IN_LOG)
				.append(" only)")
				.append("=");
				Object[] shown = Arrays.copyOf(objects, NO_ITEMS_IN_LOG);
				sb.append(arrayToString(shown));
			}
			sb.append("]");
			return sb.toString();
		}
	}
	
	private static String arrayToString(Object[] array) {
		if (array == null) {
			return null;
		}
		if (!(array instanceof CharSequence[])) {
			return Arrays.toString(array);
		}
		CharSequence[] strings = (CharSequence[]) array;
		StringBuilder sb = new StringBuilder(strings.length * 10);
		for (CharSequence string: strings) {
			sb.append(", ").append(JdbcUtils.prepareStringForLogging(string));
		}
		if (sb.length() > 0) {
			sb.delete(0, 2);
		}
		sb.insert(0, "[");
		sb.append("]");
		return sb.toString();
	}
	
}
