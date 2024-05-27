package com.asentinel.common.collections.tree;

/**
 * Listener to be implemented by node value objects
 * that want to be aware of their holding node. This allows
 * objects in a tree to be aware of their position in the 
 * tree hierarchy.
 *
 * @param <T>
 * 
 * @author Razvan Popian
 */
public interface NodeAware<T> {
	
	/**
	 * Method called by {@link Node} implementations when a value is 
	 * attached/detached to the <code>Node</code>.
	 * @param node the node to which the implementer was attached. Can be
	 * 			null to indicate that the value was detached from the node.
	 * 
	 * @see Node#setValue(Object)
	 * @see SimpleNode#setValue(Object)
	 */
	public void setNode(Node<T> node);

}
