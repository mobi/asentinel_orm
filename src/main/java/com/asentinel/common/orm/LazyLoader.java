package com.asentinel.common.orm;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.collections.tree.TreeUtils.NodeMatcher;
import com.asentinel.common.jdbc.ObjectFactory;
import com.asentinel.common.jdbc.ReusableRowMappers;
import com.asentinel.common.util.Assert;

/**
 * {@link EntityDescriptorNodeCallback} implementation that can be used for lazy loading 
 * child entities. Use the {@link Builder} class to build instances. Note that the instances are not
 * reusable.
 * <br><br>
 * 
 * Assuming we have an Invoice entity that has a Vendor child in it, here is how a {@code LazyLoader} instance
 * is created in order to lazily load the vendor:
 * 
 * <pre>
 * 		EntityDescriptorNodeCallback ll = new LazyLoader.Builder()
 *			.parentEntityFactory(() -&gt; {
 *				Invoice i = new Invoice() {
 *					// overrides super class method
 *					public Vendor getVendor() {
 *						return LazyLoader.load(
 *								() -&gt; new SqlBuilder&lt;&gt;(Vendor.class).select()
 *										.where().id().eq(getVendorId())
 *										.execForEntity(query),
 *								super::getVendor,
 *								super::setVendor);
 *					}
 *				};
 *				return i;
 *			})
 *			.parentPath(Invoice.class) // not necessary in this case, it's here for clarity only
 *			.removeChild(Vendor.class)
 *			.build();
 * </pre>
 *  
 * The example above does not ensure the visibility of the lazy loaded member in a multi threaded context. You
 * are responsible for ensuring this by synchonizing the getter/setter in the anonymous inner class for example.
 * 
 * @deprecated This is a poor man's lazy loading tool that is now deprecated in favor 
 * 				of the much more flexible, dynamic proxy based {@link AutoLazyLoader}
 *   
 * @author Razvan Popian
 */
@Deprecated
public class LazyLoader implements EntityDescriptorNodeCallback {
	
	private final static EntityDescriptor NO_FURTHER_CLASS_TREE_TRAVERSAL = new EntityDescriptor(
			Object.class, ReusableRowMappers.ROW_MAPPER_INTEGER, ReusableRowMappers.ROW_MAPPER_INTEGER, "MARKER");

	private final ObjectFactory<?> parentEntityFactory;
	private final NodeMatcher<EntityDescriptor>[] parentPath;
	private final NodeMatcher<EntityDescriptor>[] lazyLoadChildren;
	
	private Node<EntityDescriptor> parent;
	
	private LazyLoader(Builder builder) {
		Assert.assertNotNull(builder.parentEntityFactory, "parentEntityFactory");
		if (builder.lazyLoadChildren.isEmpty()) {
			throw new IllegalArgumentException("No children were selected for removal, you should remove the children"
					+ " that you want to lazy load.");
		}
		this.parentEntityFactory = builder.parentEntityFactory;
		this.parentPath = EntityDescriptorUtils.convertToNodeMatchers(builder.parentPath);
		this.lazyLoadChildren = EntityDescriptorUtils.convertToNodeMatchers(
				builder.lazyLoadChildren.toArray(new Object[builder.lazyLoadChildren.size()]));
	}

	@Override
	public boolean customize(Node<EntityDescriptor> node, SimpleEntityDescriptor.Builder builder) {
		if (parentMatch(node, builder, (NodeMatcher<EntityDescriptor>[]) parentPath)) {
			builder.entityFactory(parentEntityFactory);
			this.parent = node;
			return true;			
		} else if (childMatch(node, builder)) {
			if (this.parent == node.getParent()) {
				node.getParent().removeChild(node);
				node.setValue(NO_FURTHER_CLASS_TREE_TRAVERSAL);
				return false;
			}
		}
		return true;
	}
	
	
	private boolean parentMatch(Node<EntityDescriptor> node, SimpleEntityDescriptor.Builder builder, 
			@SuppressWarnings("unchecked") NodeMatcher<EntityDescriptor> ... path) {
		if (node == null || builder == null) {
			return false;
		}
		if (path == null || path.length == 0) {
			return node.isRoot();
		}
			
		// dummy build, just for matching, we set the node value back to null
		// in the finally block. Not ideal, but it will work for now.
		node.setValue(builder.build());
		try {
			List<Node<EntityDescriptor>> ancestors = node.getAncestors();
			if (path.length != ancestors.size()) {
				return false;
			}
			for (int i = ancestors.size() - 1; i >= 0; i--) {
				Node<EntityDescriptor> ancestor = ancestors.get(i);
				NodeMatcher<EntityDescriptor> matcher = path[i];
				if (!matcher.match(ancestor)) {
					return false;
				}
			}
			return true;
		} finally {
			node.setValue(null);
		}
	}
	
	private boolean childMatch(Node<EntityDescriptor> node, SimpleEntityDescriptor.Builder builder) {
		if (node == null || builder == null) {
			return false;
		}
		for (NodeMatcher<EntityDescriptor> childMatcher: lazyLoadChildren) {
			if (childMatcher.match(new SimpleNode<>(builder.build()))) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * {@link LazyLoader} builder class. 
	 */
	public static class Builder {
		private ObjectFactory<?> parentEntityFactory;
		private Object[] parentPath;
		private List<Object> lazyLoadChildren = new ArrayList<>(2);
	
		/**
		 * This sets the {@link ObjectFactory} for the instances that hold the child entities that 
		 * will be lazy loaded. The factory should create an entity that is able to lazy load 
		 * one or more child entities.
		 * <br>
		 * This has to be set to a non {@code null} value.
		 *  
		 * @param parentEntityFactory the factory.
		 * @return this builder.
		 */
		public Builder parentEntityFactory(ObjectFactory<?> parentEntityFactory) {
			this.parentEntityFactory = parentEntityFactory;
			return this;
		}
		
		/**
		 * This sets the path to the node that has entities 
		 * that we want lazy loaded. If not called or set to {@code null} or empty array the root entity is assumed as parent.
		 * @param parentPath the path as either {@code Class} or {@code EntityDescriptorNodeMatcher}
		 *			elements.
		 * @return this builder.
		 * 
		 * @see EntityDescriptorUtils#getEntityDescriptor(Node, Object...)
		 */
		public Builder parentPath(Object ... parentPath) {
			this.parentPath = parentPath;
			return this;
		}

		/**
		 * Sets one of the child nodes that will be removed from the tree because
		 * lazy load was setup for them in the parent entity factory.
		 * @param child the child as either {@code Class} or {@code EntityDescriptorNodeMatcher}.
		 * @return this builder.
		 */
		public Builder removeChild(Object child) {
			this.lazyLoadChildren.add(child);
			return this;
		}
		
		
		public LazyLoader build() {
			return new LazyLoader(this);
		}
	}

	/**
	 * Method to be called by the the getter method of the member (entity or entities) that will be loaded
	 * lazily.
	 * @param entitySupplier a {@link Supplier} that pulls the entity/entities from the database. If {@code null}
	 * 			the actual value of the target member will be returned regardless if it is {@code null} or not.
	 * @param entityGetter a {@link Supplier} that returns the current real value of the target member. You should 
	 * 			use a lambda expression that points to a getter method.
	 * @param entitySetter a {@link Consumer} that sets the value of the target member. You should 
	 * 			use a lambda expression that points to the setter method.
	 * @return the target member.
	 */
	public static <T> T load(Supplier<T> entitySupplier, Supplier<T> entityGetter, Consumer<T> entitySetter) {
		Assert.assertNotNull(entityGetter, "entityGetter");
		Assert.assertNotNull(entitySetter, "entitySetter");
		
		T entity = entityGetter.get();
		if (entity == null && entitySupplier != null) {
			entity = entitySupplier.get();
			entitySetter.accept(entity);
		}
		return entity;
	}
	
}
