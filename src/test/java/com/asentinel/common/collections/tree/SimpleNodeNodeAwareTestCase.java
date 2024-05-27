package com.asentinel.common.collections.tree;

import static org.junit.Assert.*;

import org.junit.Test;

public class SimpleNodeNodeAwareTestCase {
	
	@Test
	public void testNodeAware1() {
		NodeAwareObject value1 = new NodeAwareObject();
		Node<NodeAwareObject> node = new SimpleNode<NodeAwareObject>(value1);
		assertSame(node, value1.getNode());		
		
		node.setValue(null);
		assertNull(value1.getNode());
	}

	@Test
	public void testNodeAware2() {
		NodeAwareObject value1 = new NodeAwareObject();
		NodeAwareObject value2 = new NodeAwareObject();
		Node<NodeAwareObject> node = new SimpleNode<NodeAwareObject>(value1);
		assertSame(node, value1.getNode());		
		
		node.setValue(value2);
		assertSame(node, value2.getNode());
		assertNull(value1.getNode());
	}
	
	
	private class NodeAwareObject implements NodeAware<NodeAwareObject> {
		
		private Node<NodeAwareObject> node;

		public Node<NodeAwareObject> getNode() {
			return node;
		}

		@Override
		public void setNode(Node<NodeAwareObject> node) {
			this.node = node;
		}
		
	}

}
