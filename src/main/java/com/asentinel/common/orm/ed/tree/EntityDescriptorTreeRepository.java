package com.asentinel.common.orm.ed.tree;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.EntityDescriptorNodeCallback;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.orm.mappers.Child;

/**
 * Interface that defines the methods to be used to create {@link EntityDescriptor}
 * trees by recursively looking for the {@link Child} annotation in domain objects.
 * 
 * @author Razvan Popian
 */
public interface EntityDescriptorTreeRepository {
	
	// no callback methods	

	/**
	 * @see #getEntityDescriptorTree(Class, EntityDescriptorNodeCallback...)
	 */
	default Node<EntityDescriptor> getEntityDescriptorTree(Class<?> clazz) {
		return getEntityDescriptorTree(clazz, (EntityDescriptorNodeCallback[]) null);
	}

	/**
	 * @see #getEntityDescriptorTree(Class, String, EntityDescriptorNodeCallback...)
	 */
	default Node<EntityDescriptor> getEntityDescriptorTree(Class<?> clazz, String rootTableAlias) {
		return getEntityDescriptorTree(clazz, rootTableAlias, (EntityDescriptorNodeCallback[]) null);
	}
	
	/**
	 * @see #getEntityDescriptorTree(Node, EntityDescriptorNodeCallback...)
	 */
	default Node<EntityDescriptor> getEntityDescriptorTree(Node<EntityDescriptor> root) {
		return getEntityDescriptorTree(root, (EntityDescriptorNodeCallback[]) null);
	}

	// callback methods
	
	/**
	 * Creates an {@link EntityDescriptor} tree by recursively looking for {@link Child} annotations
	 * in the class provided as parameter. <br>
	 * Note that if the {@code EntityDescriptorNodeCallback} chain alters the value of a node to be a {@code EntityDescriptor}
	 * that is not {@code QueryReady} the children of that class/node will not be explored any further. This is normal since
	 * a query can only be generated from trees where all nodes implement {@code QueryReady}.
	 *  
	 * @param clazz the root class.
	 * @param nodeCallbacks {@link EntityDescriptorNodeCallback} chain to allow the customization
	 * 				of the tree. 
	 * @return the <code>EntityDescriptor</code> tree for the specified class.
	 * 
	 * @see EntityDescriptorNodeCallback
	 * @see SimpleEntityDescriptor
	 * @see SimpleEntityDescriptor.Builder
	 */
	default Node<EntityDescriptor> getEntityDescriptorTree(
			Class<?> clazz, 
			EntityDescriptorNodeCallback ... nodeCallbacks) {
		return getEntityDescriptorTree(clazz, null, nodeCallbacks);
	}
	
	/**
	 * Creates an {@link EntityDescriptor} tree by recursively looking for {@link Child} annotations
	 * in the class provided as parameter. <br>
	 * Note that if the {@code EntityDescriptorNodeCallback} chain alters the value of a node to be a {@code EntityDescriptor}
	 * that is not {@code QueryReady} the children of that class/node will not be explored any further. This is normal since
	 * a query can only be generated from trees where all nodes implement {@code QueryReady}.
	 * 
	 * @param clazz the root class.
	 * @param rootTableAlias a table alias for the root class
	 * @param nodeCallbacks {@link EntityDescriptorNodeCallback} chain to allow the customization
	 * 				of the tree. 
	 * @return the <code>EntityDescriptor</code> tree for the specified class.
	 * 
	 * @see EntityDescriptorNodeCallback
	 * @see SimpleEntityDescriptor
	 * @see SimpleEntityDescriptor.Builder
	 */
	Node<EntityDescriptor> getEntityDescriptorTree(
			Class<?> clazz, 
			String rootTableAlias, 
			EntityDescriptorNodeCallback ... nodeCallbacks);

	/**
	 * Creates an {@link EntityDescriptor} tree by recursively looking for {@link Child} annotations
	 * in the entity class from the root node. The root node MUST hold a {@link SimpleEntityDescriptor} 
	 * instance.<br>
	 * Note that if the {@code EntityDescriptorNodeCallback} chain alters the value of a node to be a {@code EntityDescriptor}
	 * that is not {@code QueryReady} the children of that class/node will not be explored any further. This is normal since
	 * a query can only be generated from trees where all nodes implement {@code QueryReady}.
	 * 
	 * @param root the root of the <code>EntityDescriptor</code> tree. The root value MUST be a {@link SimpleEntityDescriptor}.
	 * 				The entity class from this <code>SimpleEntityDescriptor</code> will be used to recursively 
	 * 				create the other nodes.
	 * @param nodeCallbacks {@link EntityDescriptorNodeCallback} chain to allow the customization
	 * 				of the tree. 
	 * @return the root parameter, just to have a similar method signature with the other methods in this class.
	 * 
	 * @see EntityDescriptorNodeCallback
	 * @see SimpleEntityDescriptor
	 * @see SimpleEntityDescriptor.Builder
	 */
	Node<EntityDescriptor> getEntityDescriptorTree(
			Node<EntityDescriptor> root, 
			EntityDescriptorNodeCallback ... nodeCallbacks);
}
