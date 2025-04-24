package com.asentinel.common.orm.mappers.dynamic;

import static com.asentinel.common.orm.mappers.SqlParameterTypeDescriptor.isCustomConversion;
import static java.util.Collections.emptyList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.springframework.core.convert.TypeDescriptor;

import com.asentinel.common.jdbc.ColumnMetadata;
import com.asentinel.common.jdbc.ObjectFactory;
import com.asentinel.common.orm.mappers.AnnotationRowMapper;
import com.asentinel.common.text.FieldIdTypeDescriptor;

/**
 * {@code AnnotationRowMapper} extension that is able to map dynamic columns in
 * addition to the static columns for entities that support dynamic columns (ie.
 * implement the {@link DynamicColumnsEntity} interface).
 * 
 * @see AnnotationRowMapper
 * @see DynamicColumnsEntityDescriptor
 * @see DynamicColumnsEntityNodeCallback
 * 
 * @author Razvan Popian
 */
public class DynamicColumnsRowMapper<C extends DynamicColumn, T extends DynamicColumnsEntity<C>> extends AnnotationRowMapper<T> {
	
	private final Collection<C> dynamicColumns;

	public DynamicColumnsRowMapper(
			Collection<C> dynamicColumns,
			ObjectFactory<T> objectFactory, String columnPrefix) {
		this(dynamicColumns, objectFactory, true, columnPrefix);
	}

	
	public DynamicColumnsRowMapper(
			Collection<C> dynamicColumns,
			ObjectFactory<T> objectFactory, boolean ignorePkColumn, String columnPrefix) {
		super(objectFactory, ignorePkColumn, columnPrefix);
		if (dynamicColumns == null) {
			dynamicColumns = emptyList();
		}
		this.dynamicColumns = dynamicColumns;
	}
	
	@Override
	protected void populateTarget(T object, ResultSet rs) throws SQLException {
		// process static columns
		super.populateTarget(object, rs);
		
		// process dynamic columns
		for (C column: dynamicColumns) {
			Object value = getValue(
					object, column.getTypeDescriptor(), 
					rs, new ColumnMetadata(getColumnPrefix(), column.getDynamicColumnName(), column.isDynamicColumnAllowNull())
			);
			object.setValue(column, value);
		}
	}
	
	@Override
	protected Object getValueInternal(Object parentObject, TypeDescriptor targetDescriptor, ResultSet rs, ColumnMetadata columnMetadata) throws SQLException {
		if (!(targetDescriptor instanceof FieldIdTypeDescriptor)) {
			return super.getValueInternal(parentObject, targetDescriptor, rs, columnMetadata);
		}
		
		FieldIdTypeDescriptor fieldIdDescriptor = (FieldIdTypeDescriptor) targetDescriptor;
		if (!(fieldIdDescriptor.getFieldId() instanceof DynamicColumn)) {
			return super.getValueInternal(parentObject, targetDescriptor, rs, columnMetadata);
		}
		
		DynamicColumn column = (DynamicColumn) fieldIdDescriptor.getFieldId();
		if (getConversionService() != null
				&& isCustomConversion(column)) {
			// we are dealing with a custom type, we call the conversion service
			return customConvert(targetDescriptor, rs, columnMetadata.getResultsetName());
		}
		// let the super class code attempt to convert, but it will likely fail
		return super.getValueInternal(parentObject, targetDescriptor, rs, columnMetadata);
	}

}
