package com.asentinel.common.orm.mappers.dynamic;

import static java.util.Collections.unmodifiableSet;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;

import com.asentinel.common.text.FieldIdTypeDescriptor;
import com.asentinel.common.util.Assert;

/**
 * Reference implementation for the {@link DynamicColumn} interface.
 * 
 * @author Razvan.Popian
 */
public class DefaultDynamicColumn implements DynamicColumn {
	private final String name;
	private final Class<?> type;
	private final Set<DynamicColumnFlags> flags;
	private final TypeDescriptor typeDescriptor;
	
	public DefaultDynamicColumn(String name, Class<?> type) {
		this(name, type, null);
	}

	public DefaultDynamicColumn(String name, Class<?> type, Set<DynamicColumnFlags> flags) {
		Assert.assertNotEmpty(name, "name");
		Assert.assertNotNull(type, "type");
		this.name = name;
		this.type = type;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}	

	@Override
	public String toString() {
		return "DefaultDynamicColumn [name=" + name + ", type=" + type + "]";
	}
}
