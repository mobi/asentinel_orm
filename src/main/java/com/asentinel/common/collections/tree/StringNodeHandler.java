package com.asentinel.common.collections.tree;

/**
 * NodeHandler used internally by the SimpleNode class in the method
 * {@link SimpleNode#toStringAsTree()}.
 * 
 * @see SimpleNode#toStringAsTree()
 *
 * @param <T>
 * 
 * @author Razvan Popian
 */
final class StringNodeHandler<T> implements NodeHandler<T> {

	private static final String TAB = "   ";
	
	private final StringBuilder sb = new StringBuilder();
	
	/**
	 * @see NodeHandler#handleNode(Node)
	 */
	@Override
	public void handleNode(Node<T> node) {
		for (int i=0; i<node.getLevel(); i++) {
			sb.append(TAB);
		}
		sb.append(node).append("\n");
	}
	
	public String getTreeAsString() {
		return sb.toString();
	}

}
