package com.asentinel.common.orm.mappers.dynamic;

import static java.util.Collections.unmodifiableSet;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.support.JdbcUtils;

import com.asentinel.common.text.FieldIdTypeDescriptor;
import com.asentinel.common.util.Assert;

/**
 * Reference implementation for the {@link DynamicColumn} interface. Users of
 * the library are encouraged to implement the {@link DynamicColumn} interface
 * for the classes that represent the metadata for their dynamic (runtime
 * defined) columns.
 * 
 * @author Razvan Popian
 */
public class DefaultDynamicColumn implements DynamicColumn {
	private final String name;
	private final Class<?> type;
	private final SqlParameter sqlParameter;
	private final Set<DynamicColumnFlags> flags;
	private final TypeDescriptor typeDescriptor;
	
	public DefaultDynamicColumn(String name, Class<?> type) {
		this(name, type, EnumSet.noneOf(DynamicColumnFlags.class));
	}

	public DefaultDynamicColumn(String name, Class<?> type, String sqlParameterTypeName) {
		this(name, type, 
			new SqlParameter(JdbcUtils.TYPE_UNKNOWN, sqlParameterTypeName), 
			EnumSet.noneOf(DynamicColumnFlags.class));
	}
	
	public DefaultDynamicColumn(String name, Class<?> type, SqlParameter sqlParameter) {
		this(name, type, sqlParameter, EnumSet.noneOf(DynamicColumnFlags.class));
	}

	public DefaultDynamicColumn(String name, Class<?> type, Set<DynamicColumnFlags> flags) {
		this(name, type, null, flags);
	}

	public DefaultDynamicColumn(String name, Class<?> type, SqlParameter sqlParameter, Set<DynamicColumnFlags> flags) {
		Assert.assertNotEmpty(name, "name");
		Assert.assertNotNull(type, "type");
		this.name = name;
		this.type = type;
		this.sqlParameter = sqlParameter;
		if (flags == null) {
			flags = EnumSet.noneOf(DynamicColumnFlags.class);
		}
		this.flags = unmodifiableSet(flags);
		this.typeDescriptor = new FieldIdTypeDescriptor(this, getDynamicColumnType());
	}
	
	@Override
	public String getDynamicColumnName() {
		return name;
	}

	@Override
	public Class<?> getDynamicColumnType() {
		return type;
	}
	
	@Override
	public Set<DynamicColumnFlags> getDynamicColumnFlags() {
		return flags;
	}
	
	@Override
	public TypeDescriptor getTypeDescriptor() {
		return typeDescriptor;
	}
	
	@Override
	public SqlParameter getSqlParameter() {
		return sqlParameter;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultDynamicColumn other = (DefaultDynamicColumn) obj;
		return Objects.equals(name, other.name) && Objects.equals(type, other.type);
	}

	@Override
	public String toString() {
		return "DefaultDynamicColumn [name=" + name + ", type=" + type + "]";
	}
}
