package com.asentinel.common.collections.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.asentinel.common.util.Assert;

/**
 * Node implementation that stores the children in a List<T>.
 * This implementation does not have circular reference detection. It is
 * the responsibility of the client to use this class so that no circular references
 * are created.<BR><BR>
 * 
 * {@link NodeAware} values attached to a <code>SimpleNode</code> are notified
 * by the {@link #setValue(Object)} method.
 *
 * @param <T> - type of the object stored in this node.
 * 
 * @author Razvan Popian
 */
public final class SimpleNode<T> implements Node<T> {
	
	private Node<T> parent;
	private final List<Node<T>> children = new ArrayList<>();
	private T value;
	
	/**
	 * No-args constructor. Sets the value to null.
	 */
	public SimpleNode() {
		this(null);
	}
	
	/**
	 * Constructor that initializes the value.
	 * @param value - the value, can be null.
	 */
	public SimpleNode(T value) {
		setParent(null);
		setValue(value);
	}

	/**
	 * Sets the parent of this node. If this node already has a parent
	 * this method removes this node from the parent children list. This method DOES NOT
	 * add this node as child to the node parameter. The client has to do this manually.
	 * This is why the {@link Node#addChild(Node)} method is preferred.
	 * <br>
	 * If the client tries to set the parent to this node this method does nothing.
	 * @see Node#setParent(Node)
	 */
	@Override
	public void setParent(Node<T> node) {
		if (this == node) {
			return;
		}
		if (parent != null) {
			parent.removeChild(this);
		}		
		this.parent = node;
	}
	
	/**
	 * @see Node#getParent() 
	 */
	@Override
	public Node<T> getParent() {
		return parent;
	}
	
	/**
	 * @see Node#getLevel() 
	 */
	@Override
	public int getLevel() {
		List<Node<T>> list = getAncestors();
		return list.size() - 1;
	}
	

	/**
	 * @see Node#setValue(Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setValue(T value) {
		if (this.value instanceof NodeAware) {
			// notify the old value that it is no longer assigned
			// to this node
			((NodeAware<T>) this.value).setNode(null);
		}
		this.value = value;
		if (value instanceof NodeAware) {
			// notify the new value that it was assigned to a node
			((NodeAware<T>) value).setNode(this);
		}
	}

	/**
	 * @see Node#getValue()
	 */
	@Override
	public T getValue() {
		return value;
	}

	/**
	 * @see Node#getChildren()
	 * @return the list of children in the order they were added. 
	 */
	@Override
	public List<Node<T>> getChildren() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * Adds a child to this node. If the node parameter already has a parent
	 * this method removes the node parameter from the parent and then adds it
	 * to this node children list. If the client tries to add this node as child this
	 * method does nothing.
	 * @param node
	 * @throws NullPointerException if the node parameter is null.
	 * @see Node#addChild(Node)
	 */
	@Override
	public Node<T> addChild(Node<T> node) {
		Assert.assertNotNull(node, "node");
		if (this == node) {
			return this;
		}
		if (node.getParent() != null) {
			node.getParent().removeChild(node);
		}		
		node.setParent(this);
		children.add(node);
		return this;
	}

	/**
	 * @see Node#removeChild(Node)
	 * @param node - the node to be removed. This method will set the parent of the removed node
	 * 					to null if it successfully removes the node.
	 * @return true if the removed method found the node and removed it
	 * 			false otherwise.
	 */
	@Override
	public boolean removeChild(Node<T> node) {
		if (node == null) {
			return false;
		}
		if (children.remove(node)) {
			node.setParent(null);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @see Node#isRoot()
	 */
	@Override
	public boolean isRoot() {
		return parent == null;
	}

	/**
	 * @see Node#isLeaf()
	 */
	@Override
	public boolean isLeaf() {
		return children.isEmpty();
	}

	/**
	 * @see Node#getAncestors()
	 */
	@Override
	public List<Node<T>> getAncestors() {
		List<Node<T>> ancestors = new ArrayList<>();
		ancestors.add(this);		
		Node<T> parentIt = parent;
		while (parentIt != null) {
			ancestors.add(parentIt);
			parentIt = parentIt.getParent();
		}
		Collections.reverse(ancestors);
		return ancestors;
	}

	/**
	 * @see Node#getDescendants()
	 */
	@Override
	public List<Node<T>> getDescendants() {
		ListNodeHandler<T> handler = new ListNodeHandler<>();
		traverse(handler);
		return handler.getList();
	}

	/**
	 * @see Node#getSiblings()
	 */
	@Override
	public List<Node<T>> getSiblings() {
		if (parent == null) {
			return Collections.emptyList();
		}
		List<Node<T>> nodes = parent.getChildren();
		List<Node<T>> siblings = new ArrayList<>(nodes.size());
		for (Node<T> node:nodes) {
			if (this != node) {
				siblings.add(node);
			}
		}
		return siblings;
	}
	
	
	private void traverse(Node<T> node, NodeHandler<T> handler) {
		List<Node<T>> children = node.getChildren();
		for (Node<T> child: children) {
			handler.handleNode(child);			
			if (!child.isLeaf()) {
				traverse(child, handler);
			}
		}
	}
	
	/**
	 * @see Node#traverse(NodeHandler)
	 * @see NodeHandler
	 */
	@Override
	public final void traverse(NodeHandler<T> handler) {
		Assert.assertNotNull(handler, "handler");
		handler.handleNode(this);
		traverse(this, handler);
	}

	/**
	 * @see Node#toStringAsTree()
	 */
	@Override
	public String toStringAsTree() {
		StringNodeHandler<T> handler = new StringNodeHandler<>();
		traverse(handler);
		return handler.getTreeAsString();
	}
	
	@Override
	public String toString() {
		return String.valueOf(value);
	}


}
