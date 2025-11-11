package com.asentinel.common.collections.tree;

/**
 * Visitor interface used to decouple the traversal of a tree from the processing 
 * of each node in the tree.
 * @see Node#traverse(NodeHandler)
 * 
 * @param <T> - the type of data stored in the nodes that this handler can process.
 * 
 * @author Razvan Popian
 */
@FunctionalInterface
public interface NodeHandler<T> {
	
	/**
	 * Method that processes the node parameter.
	 */
	void handleNode(Node<T> node);
}
