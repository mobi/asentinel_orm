package com.asentinel.common.collections.tree;

import java.util.ArrayList;
import java.util.List;


/**
 * NodeHandler implementation that adds all the nodes received by
 * the {@link #handleNode(Node)} method to an internal list.
 * 
 * @see NodeHandler
 * @see #getList()
 *
 * @param <T>
 * 
 * @author Razvan Popian
 */
public final class ListNodeHandler<T> implements NodeHandler<T> {
	
	private final List<Node<T>> list = new ArrayList<>();

	/**
	 * @see NodeHandler#handleNode(Node)
	 */
	@Override
	public void handleNode(Node<T> node) {
		list.add(node);
	}

	/**
	 * @return the list of nodes
	 */
	public List<Node<T>> getList() {
		return list;
	}

}
