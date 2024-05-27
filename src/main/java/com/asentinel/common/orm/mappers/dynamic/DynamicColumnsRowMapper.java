package com.asentinel.common.orm.mappers.dynamic;

import static java.util.Collections.emptyList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import com.asentinel.common.jdbc.ColumnMetadata;
import com.asentinel.common.jdbc.ObjectFactory;
import com.asentinel.common.orm.mappers.AnnotationRowMapper;

/**
 * {@code AnnotationRowMapper} extension that is able to map dynamic columns in
 * addition to the static columns for entities that support dynamic columns (ie.
 * implement the {@link DynamicColumnsEntity} interface).
 * 
 * @see AnnotationRowMapper
 * @see DynamicColumnsEntityDescriptor
 * @see DynamicColumnsEntityNodeCallback
 * 
 * @author Razvan.Popian
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
					object, column.getDynamicColumnType(), 
					rs, new ColumnMetadata(getColumnPrefix(), column.getDynamicColumnName(), column.isDynamicColumnAllowNull())
			);
			object.setValue(column, value);
		}
	}
}
