package com.asentinel.common.orm.mappers.dynamic;

import static com.asentinel.common.jdbc.ConversionSupport.isPreparedForProxyingInputStreams;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.asentinel.common.jdbc.ObjectFactory;
import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.SimpleEntityDescriptor;

/**
 * {@code EntityDescriptor} extension for entities that support dynamic columns
 * in addition to static columns (ie. implement {@link DynamicColumnsEntity}).
 * 
 * @see DynamicColumnsEntityNodeCallback
 * 
 * @author Razvan.Popian
 *
 */
public class DynamicColumnsEntityDescriptor<C extends DynamicColumn, T extends DynamicColumnsEntity<C>> 
	extends SimpleEntityDescriptor {
	
	private final Collection<C> dynamicColumns;

	public DynamicColumnsEntityDescriptor(
			Collection<C> dynamicColumns,
			DynamicColumn pkDynamicColumn,
			Builder builder) {
		super(
				builder
				.columnAliasSeparator("_")
				.pkName(pkDynamicColumn != null ? pkDynamicColumn.getDynamicColumnName() : null)
				.entityIdRowMapper(
					getEntityIdRowMapper(pkDynamicColumn, builder)
				)
				.mapper(
					getDynamicColumnsRowMapper(dynamicColumns, builder)
				)
				.preBuild()
		);
		this.dynamicColumns = dynamicColumns == null ? emptyList() : dynamicColumns;
	}

	@Override
	public Collection<String> getColumnNames() {
		Collection<String> staticCols = super.getColumnNames();
		Collection<String> dynamicCols = dynamicColumns
				.stream()
				.filter(c -> !(isPreparedForProxyingInputStreams(c.getDynamicColumnType(), getEntityRowMapper())))
				.map(DynamicColumn::getDynamicColumnName)
				.collect(toList());
		List<String> columns = new ArrayList<>(staticCols.size() + dynamicCols.size());
		columns.addAll(staticCols);
		columns.addAll(dynamicCols);
		return columns;
	}
	
	public Collection<C> getDynamicColumns() {
		return Collections.unmodifiableCollection(dynamicColumns);
	}
	
	private static RowMapper<?> getEntityIdRowMapper(DynamicColumn pkDynamicColumn, Builder builder) {
		if (pkDynamicColumn == null) {
			return null;
		}
		return EntityUtils.getEntityIdRowMapper(pkDynamicColumn.getDynamicColumnType(), 
				builder.getTableAlias() + builder.getColumnAliasSeparator() + pkDynamicColumn.getDynamicColumnName());
	}
	
	private static 
		<C extends DynamicColumn, T extends DynamicColumnsEntity<C>> 
		DynamicColumnsRowMapper<C, T> getDynamicColumnsRowMapper(Collection<C> dynamicColumns, Builder builder) {
		
		@SuppressWarnings("unchecked")
		DynamicColumnsRowMapper<C, T> rm = new DynamicColumnsRowMapper<C, T>(
				dynamicColumns,	
				(ObjectFactory<T>) builder.getEntityFactory(), 
				builder.getTableAlias() + builder.getColumnAliasSeparator()
			);
		rm.setLobHandler(builder.getLobHandler());
		rm.setQueryExecutor(builder.getQueryEx());
		return rm;
	}
	
	@Override
	public String toString() {
		return "DynamicColumnsEntityDescriptor [pkName=" + getPkName() 
				+ ", class=" + getEntityClass().getName() 
				+ ", parentRelationType=" + getParentRelationType()
				+ ", dynamicColumns count=" + dynamicColumns.size()
				+ "]";
	}
	
}
 