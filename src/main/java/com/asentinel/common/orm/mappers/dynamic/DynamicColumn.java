package com.asentinel.common.orm.mappers.dynamic;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.jdbc.core.SqlParameter;

import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.text.FieldIdTypeDescriptor;

/**
 * Equivalent for the {@link Column} annotation in the dynamic columns world.
 * This interface should be implemented by classes that describe dynamic field
 * properties like name, type etc.
 * 
 * @see DynamicColumnsEntity
 * @see PkColumn
 * @see Column
 * 
 * @author Razvan Popian
 */
public interface DynamicColumn {

	/**
	 * @return the name of the database table column for this dynamic column. 
	 */
	String getDynamicColumnName();

	/**
	 * @return the type of the field in java.
	 */
	Class<?> getDynamicColumnType();
	
	/**
	 * @return boolean determining if this dynamic column supports {@code null}.
	 */
	default boolean isDynamicColumnAllowNull() {
		return getDynamicColumnFlags().contains(DynamicColumnFlags.ALLOW_NULL);
	}
	
	/**
	 * @return boolean determining if this dynamic column is a foreign key.
	 */
	default boolean isDynamicColumnEntityFk() {
		return getDynamicColumnFlags().contains(DynamicColumnFlags.ENTITY_FK);
	}
	
	/**
	 * @return the flags associated with this dynamic column.
	 */
	default Set<DynamicColumnFlags> getDynamicColumnFlags() {
		return EnumSet.noneOf(DynamicColumnFlags.class);
	}
	
	/**
	 * @return a {@link TypeDescriptor} for this column that can be used for
	 *         converting the resultset value (database value) to a java domain
	 *         type.
	 */
	default TypeDescriptor getTypeDescriptor() {
		return new FieldIdTypeDescriptor(this, getDynamicColumnType());
	}
	
	/**
	 * @return information about the mapped database column type of the dynamic
	 *         column. It should be used only if the mapped database column is of
	 *         some special type or a user defined type. Returns {@code null} by
	 *         default to indicate that the default conversion should be used (i.e.
	 *         no {@code ConversionService} conversion should be triggered).
	 */
	default SqlParameter getSqlParameter() {
		return null;
	}
	
	public enum DynamicColumnFlags {
		ALLOW_NULL, ENTITY_FK
	}
	
	// TODO: add isInsertable() and isUpdatable() methods similarly to what we have in
	// the @Column annotation
}
