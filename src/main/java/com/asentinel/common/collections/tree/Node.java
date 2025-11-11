package com.asentinel.common.collections.tree;

import java.util.List;

/**
 * This interface should be implemented by objects that represent a node in a tree. A node
 * should define a parent and a list of children so that the tree can be traversed starting from any
 * node up to the root and also starting from the root down to the leaves. How the children are stored, 
 * mainly how they are ordered is an implementation detail. The {@link #getChildren()} method can return
 * the list of children in a different order than the order of addition.
 * <br><br>
 * 
 * Be aware that implementations may or may not have circular reference detection. The clients of this
 * interface should avoid at any cost the creation of circular references.
 * <br><br>
 * 
 * @param <T> - the type of the value being stored in the Node.
 * 
 * @see NodeHandler
 * 
 * @author Razvan Popian
 */
public interface Node<T> {
	
	/**
	 * Set the parent of this node.
	 */
	void setParent(Node<T> node);
	
	/**
	 * @return the parent of this Node. It can return null if this is
	 * 			a root node.
	 * @see #isRoot()
	 */
	Node<T> getParent();
	
	/**
	 * Method that adds a child to this node. If the node parameter is already child of
	 * some other node this method should set its parent to this node and remove it from
	 * the list of children of the other parent node.
	 * @param node the node to be added as child.
	 * @throws NullPointerException if the node parameter is null.
	 * @return this node so that multiple {@link #addChild(Node)} methods can be
	 * 			chained like this: node.addChild(node1).addChild(node2). 
	 */
	Node<T> addChild(Node<T> node);
	
	/**
	 * Remove the specified child node from this node. Only the first occurrence should be removed. 
	 * The way that the node is identified in the list of children of this node 
	 * is an implementation detail (each implementation can do whatever logic it needs).
	 * @param node the node to be removed
	 * @return true if the node was found and removed from the list of children for this node,
	 * 			false otherwise.
	 */
	boolean removeChild(Node<T> node);
	
	/**
	 * Set the value of this node. 
	 */
	void setValue(T value);
	
	/**
	 * @return the value of this node.
	 */
	T getValue();
	
	/**
	 * @return true if this is a root node (the parent is null),
	 * 			false if this node has a parent.
	 */
	boolean isRoot();
	
	/**
	 * @return true if this node has no children,
	 * 			false otherwise.
	 */
	boolean isLeaf();
	
	/**
	 * @return the level of this node. The root node is on level 0,
	 * 			the root children are on level 1, the children of 
	 * 			the root children are on level 2 and so on.
	 */
	int getLevel();

	/**
	 * Method that traverses the tree represented by this node (this node is considered the root). 
	 * The implementation is free to choose whatever traversal order it needs. The implementation should
	 * use the handler parameter to process each node.
	 * @see NodeHandler
	 */
	void traverse(NodeHandler<T> handler);
	
	/**
	 * @return the children of this node. The order of the children
	 * 			may not be the order in which they were added (it is an implementation detail).
	 * @see #getDescendants()
	 */
	List<Node<T>> getChildren();
	
	/**
	 * @return a list of all the nodes that are descendants of this node (children, nephews, etc).The
	 * 			first node in the list must be this node. The order is which the actual descendants 
	 * 			are returned is an implementation detail.
	 * @see #getChildren()
	 */
	List<Node<T>> getDescendants();

	/**
	 * @return list of the ancestors of this node up to the root,
	 * 			including this node. The first node in the list
	 * 			must be the root and the last must be this node. If
	 * 			this node is the root the list will contain just this node.
	 */
	List<Node<T>> getAncestors();
	
	/**
	 * @return the siblings of this node if any. If this node
	 * 			is the root node an empty list is returned.
	 */
	List<Node<T>> getSiblings();
	
	/**
	 * @return String representation of this node including its subtree.
	 * 			How this string is created is an implementation detail.
	 */
	String toStringAsTree();
}
