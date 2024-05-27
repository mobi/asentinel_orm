package com.asentinel.common.orm.mappers.dynamic;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.partitioningBy;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.jdbc.TypedObjectFactory;
import com.asentinel.common.orm.AutoLazyLoader;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.EntityDescriptorNodeCallback;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.orm.SimpleEntityDescriptor.Builder;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.util.Assert;

/**
 * {@code EntityDescriptorNodeCallback} implementation for creating
 * {@link DynamicColumnsEntityDescriptor} for entities that have dynamic columns
 * in addition to static columns or even only dynamic columns.
 * 
 * @see DynamicColumnsEntityDescriptor
 * 
 * @author Razvan Popian
 */
public class DynamicColumnsEntityNodeCallback<C extends DynamicColumn, T extends DynamicColumnsEntity<C>> 
	implements EntityDescriptorNodeCallback {
	
	private final TypedObjectFactory<?> entityFactory;
	private final List<C> dynamicStandardColumns;
	private final List<C> dynamicFkColumns;
	private final DynamicColumn pkDynamicColumn;
	
	private final String tableName;
	
	private Map<C, DynamicColumnEntityOverride> entityOverrides;
	
	public DynamicColumnsEntityNodeCallback( 
			TypedObjectFactory<T> entityFactory,  
			Collection<? extends C> dynamicColumns) {
		this(entityFactory, dynamicColumns, null, null);
	}
	
	public DynamicColumnsEntityNodeCallback( 
			TypedObjectFactory<T> entityFactory,  
			Collection<? extends C> dynamicColumns,
			DynamicColumn pkDynamicColumn,
			String tableName) {
		Assert.assertNotNull(entityFactory, "entityFactory");
		this.entityFactory = entityFactory;
		dynamicColumns = dynamicColumns == null ? emptyList() : dynamicColumns;
		Map<Boolean, List<C>> partitions = dynamicColumns.stream()
				.collect(partitioningBy(DynamicColumn::isDynamicColumnEntityFk));
		this.dynamicStandardColumns = partitions.get(Boolean.FALSE);
		this.dynamicFkColumns = partitions.get(Boolean.TRUE);
		this.pkDynamicColumn = pkDynamicColumn;
		this.tableName = tableName;
	}
	
	/**
	 * Associates an optional {@code DynamicColumnEntityOverride} to a
	 * foreign key/entity column.
	 */
	public void addDynamicColumnEntityOverride(C column, DynamicColumnEntityOverride entityOverrideData) {
		Assert.assertNotNull(column, "column");
		Assert.assertNotNull(entityOverrideData, "entityOverrideData");
		if (!column.isDynamicColumnEntityFk()) {
			throw new IllegalArgumentException("Expected a foreign key column. Got " + column);
		}
		if (entityOverrides == null) {
			entityOverrides = new HashMap<>();
		}
		this.entityOverrides.put(column, entityOverrideData);
	}

	
	@Override
	public final boolean customize(Node<EntityDescriptor> node, Builder builder, SqlBuilderFactory sqlBuilderFactory) {
		if (builder.getEntityClass() == entityFactory.getType()) {
			return customizeInternal(node, builder, sqlBuilderFactory);
		}
		return true;
	}
	
	protected boolean customizeInternal(Node<EntityDescriptor> node, Builder builder, SqlBuilderFactory sqlBuilderFactory) {
		if (StringUtils.hasText(tableName)) {
			builder.tableName(tableName);
		}
		node.setValue(new DynamicColumnsEntityDescriptor<C, T>(dynamicStandardColumns,
						pkDynamicColumn,
						builder.entityFactory(entityFactory)));
		
		// add children to this node for any child dynamic entities 
		for (int i = 0; i < dynamicFkColumns.size(); i++) {
			C col = dynamicFkColumns.get(i);
			
			DynamicColumnEntityOverride entityOverride = entityOverrides != null 
														? entityOverrides.get(col)
														: null;
			
			Builder fkEntityBuilder = new SimpleEntityDescriptor.Builder(col.getDynamicColumnType())
					.name(col)
					.fkName(col.getDynamicColumnName())
					.tableAlias(builder.getTableAlias() + "_" + Integer.toHexString(i));
			
			Node<EntityDescriptor> n;			
			if (entityOverride == null) {
				// no overrides for this fk column, all the entity children will be lazily loaded
				n = sqlBuilderFactory.getEntityDescriptorTreeRepository()
						.getEntityDescriptorTree(new SimpleNode<>(fkEntityBuilder.build()), AutoLazyLoader.forAllRootChildren());
			} else {
				// we have override information for this fk column, we will use it
				Node<EntityDescriptor> subTreeRoot = new SimpleNode<>();
				if (entityOverride.getRootCallback() != null) {
					entityOverride.getRootCallback().customize(subTreeRoot, fkEntityBuilder, sqlBuilderFactory);
				}
				if (subTreeRoot.getValue() == null) {
					subTreeRoot.setValue(fkEntityBuilder.build());
				}
				
				EntityDescriptorNodeCallback[] callbacks = entityOverride.getCallbacks();
				EntityDescriptorNodeCallback[] finalCallbacks = new EntityDescriptorNodeCallback[callbacks.length + 1];
				// we are appending the _c to the alias so that it does not conflict
				// with any of the aliases in the if branch
				finalCallbacks[0] = new OverrideTableAliasNodeCallback(fkEntityBuilder.getTableAlias() + "_o");
				System.arraycopy(callbacks, 0, finalCallbacks, 1, callbacks.length);

				n = sqlBuilderFactory.getEntityDescriptorTreeRepository()
						.getEntityDescriptorTree(subTreeRoot, finalCallbacks);				
			}
			node.addChild(n);
		}
		return false;
	}
	
	@Override
	public boolean customize(Node<EntityDescriptor> node, Builder builder) {
		throw new UnsupportedOperationException("This method should not get called.");
	}
	
	/*
	 * This is needed to avoid duplicated table aliases in the main tree.
	 */
	static class OverrideTableAliasNodeCallback implements EntityDescriptorNodeCallback {

		private final String prefix;
		private int index = 1;
		
		public OverrideTableAliasNodeCallback(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public boolean customize(Node<EntityDescriptor> node, Builder builder) {
			builder.tableAlias(prefix + "_" + Integer.toHexString(index++));
			return true;
		}
		
	}
}
