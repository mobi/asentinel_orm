package com.asentinel.common.orm;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.orm.ed.tree.DefaultEntityDescriptorTreeRepository;
import com.asentinel.common.orm.ed.tree.EntityDescriptorTreeRepository;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.mappers.Child;

/**
 * Callback interface that is used by the methods in the {@link DefaultEntityDescriptorTreeRepository}
 * class to customize the automatically generated {@link EntityDescriptor} trees.
 * 
 * @see EntityDescriptorTreeRepository
 * @see DefaultEntityDescriptorTreeRepository
 * 
 * @author Razvan Popian
 */
@FunctionalInterface
public interface EntityDescriptorNodeCallback {
	
	/**
	 * This method is called by the tree creation methods in the {@link EntityDescriptorTreeRepository}
	 * class after the node and the {@link SimpleEntityDescriptor.Builder} for the current 
	 * {@link Child} annotation are created. The node is now in its final position in the tree, but
	 * its value (which by default will be a {@link SimpleEntityDescriptor} or a subclass) may not be yet set.
	 * The value may be set if a previous callback from the chain set it.
	 * <br><br> 
	 * The implementations can set a value in the node, it is allowed for the value to have any type
	 * that extends {@link EntityDescriptor}. For example the implementer may set in the node
	 * a {@link CacheEntityDescriptor} instance.
	 * If the node value is left/set <code>null</code>, the methods in the
	 * {@link EntityDescriptorTreeRepository} class will set the value resulted from calling 
	 * {@link SimpleEntityDescriptor.Builder#build()}. 
	 *  
	 * 
	 * @param node the node for the current {@link Child} annotation as part of the tree. The
	 * 			node value may be <code>null</code> when this method is called. 
	 * @param builder the builder used to create the {@link SimpleEntityDescriptor} for the
	 * 			current {@link Child} annotation, this builder can be used to override the properties
	 * 			found in the {@link Child} annotation.
	 * @param sqlBuilderFactory {@link SqlBuilderFactory} implementation. It can be {@code null} if the {@link EntityDescriptorTreeRepository}
	 * 			implementation can not provide an implementation. The {@link DefaultEntityDescriptorTreeRepository} can be configured with
	 * 			a {@code SqlBuilderFactory} and it can pass that implementation to this method.
	 * @return <code>true</code> if the execution chain should proceed with the
	 * 		next node callback. <code>false</code> to stop at this node callback.  
	 */
	public default boolean customize(Node<EntityDescriptor> node, SimpleEntityDescriptor.Builder builder,
			SqlBuilderFactory sqlBuilderFactory) {
		return this.customize(node, builder);
	}
	
	/**
	 * @see #customize(Node, com.asentinel.common.orm.SimpleEntityDescriptor.Builder, SqlBuilderFactory)
	 */
	public boolean customize(Node<EntityDescriptor> node, SimpleEntityDescriptor.Builder builder);
	
	
	/**
	 * @return a {@code EntityDescriptorNodeCallback} that will cause the query generator
	 * 			to only create the SELECT query for the root node. It is useful for building
	 * 			simple SQL queries, usually queries that return only one or two columns from the
	 * 			root table.  
	 */
	public static EntityDescriptorNodeCallback rootOnlyQuery() {
		return EntityDescriptorNodeCallbackConstants.NO_QUERY_BELOW_ROOT;
	}
}

class EntityDescriptorNodeCallbackConstants {
	
	private EntityDescriptorNodeCallbackConstants() {}
	
	final static EntityDescriptorNodeCallback NO_QUERY_BELOW_ROOT  = (n, b) -> {
		if (!n.isRoot()) {
			n.setValue(new EntityDescriptor(Object.class, 
					(r, rn) -> null, 
					(r, rn) -> null, 
					"NO_QUERY_BELOW_ROOT"));
		}
		return false;
	};
}
