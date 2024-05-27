package com.asentinel.common.collections.tree;

import static org.junit.Assert.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

public class SimpleNodeTestCase {
	private static final Logger log = LoggerFactory.getLogger(SimpleNodeTestCase.class);

	@Test
	public void testSingleNode() {
		log.info("testSingleNode start");
		final Integer value = 1;
		Node<Integer> node = new SimpleNode<Integer>(value);
		assertEquals(true, node.isRoot());
		assertEquals(true, node.isLeaf());
		assertNull(node.getParent());
		assertEquals(0, node.getLevel());
		assertEquals(value, node.getValue());
		
		List<Node<Integer>> siblings = node.getSiblings();
		assertEquals(0, siblings.size());
		List<Node<Integer>> ancestors = node.getAncestors();
		assertEquals(1, ancestors.size());
		assertEquals(node, ancestors.get(0));
		List<Node<Integer>> descendants = node.getDescendants();
		assertEquals(1, descendants.size());
		assertEquals(node, descendants.get(0));
		log.info("testSingleNode stop");
	}

	
	@Test
	public void testTree() {
		log.info("testTree start");
		
		// build the tree
		Node<Integer> testNode1 = new SimpleNode<Integer>(202);
		Node<Integer> testNode2 = new SimpleNode<Integer>(30);
		Node<Integer> tree = new SimpleNode<Integer>(1);
		tree.addChild(new SimpleNode<Integer>(10)).addChild(new SimpleNode<Integer>(20)).addChild(testNode2);
		tree.getChildren().get(0).addChild(new SimpleNode<Integer>(100))
			.addChild(new SimpleNode<Integer>(101));
		tree.getChildren().get(1).addChild(new SimpleNode<Integer>(200).addChild(new SimpleNode<Integer>(2001)))
			.addChild(testNode1).addChild(new SimpleNode<Integer>(null));
		log.info("testTree - Tree:\n" + tree.toStringAsTree());
		log.info("testTree - Tree construction done.");
		
		
		// test traversal
		assertEquals(true, tree.isRoot());
		assertEquals(false, tree.isLeaf());
		assertEquals(false, testNode1.isRoot());
		assertEquals(true, testNode1.isLeaf());
		assertEquals(2, testNode1.getLevel());
		assertEquals(false, testNode2.isRoot());
		assertEquals(true, testNode2.isLeaf());
		assertEquals(1, testNode2.getLevel());
		
		List<Node<Integer>> level1Children = tree.getChildren();
		assertEquals(3, level1Children.size());
		assertEquals(testNode2, level1Children.get(2));
		
		List<Node<Integer>> ancestors = testNode2.getAncestors();
		assertEquals(2, ancestors.size());
		assertEquals(tree, ancestors.get(0));

		List<Node<Integer>> descendants = testNode2.getDescendants();
		assertEquals(1, descendants.size());
		assertEquals(testNode2, descendants.get(0));
		
		List<Node<Integer>> siblings = testNode2.getSiblings();
		assertEquals(2, siblings.size());
		
		
		ancestors = testNode1.getAncestors();
		assertEquals(3, ancestors.size());
		assertEquals(tree, ancestors.get(0));

		descendants = testNode1.getDescendants();
		assertEquals(1, descendants.size());
		assertEquals(testNode1, descendants.get(0));
		
		siblings = testNode1.getSiblings();
		assertEquals(2, siblings.size());
		assertNull(siblings.get(1).getValue());
		
		
		// test add/remove
		Node<Integer> testNode3 = new SimpleNode<Integer>(2020);
		Node<Integer> testNode4 = new SimpleNode<Integer>(2021);
		testNode1.addChild(testNode3);
		testNode1.addChild(testNode3);
		log.info("testTree - Tree after add 1 node:\n" + tree.toStringAsTree());
		assertEquals(false, testNode1.isLeaf());
		assertEquals(true, testNode3.isLeaf());
		assertEquals(1, testNode1.getChildren().size());
		
		assertEquals(true, testNode1.removeChild(testNode3));
		assertEquals(false, testNode1.removeChild(testNode3));
		log.info("testTree - Tree after remove 1 node:\n" + tree.toStringAsTree());
		assertEquals(true, testNode1.isLeaf());
		assertEquals(true, testNode3.isLeaf());
		assertNull(testNode3.getParent());
		
		testNode1.addChild(testNode3);
		testNode1.addChild(testNode4);
		assertEquals(false, testNode1.isLeaf());
		assertEquals(true, testNode3.isLeaf());
		assertEquals(true, testNode4.isLeaf());
		List<Node<Integer>> children = testNode1.getChildren();
		assertEquals(testNode3, children.get(0));
		assertEquals(testNode4, children.get(1));

		log.info("testTree stop");
	}

	@Test
	public void testAddRemoveNode() {
		log.info("testAddRemoveNode start");
		Node<Integer> node = new SimpleNode<Integer>(1);		
		Node<Integer> node1 = new SimpleNode<Integer>(2);
		Node<Integer> node2 = new SimpleNode<Integer>(2);
		node.addChild(node1).addChild(node2);
		log.info("testTree - Tree:\n" + node.toStringAsTree());
		
		assertEquals(true, node.removeChild(node1));
		List<Node<Integer>> children = node.getChildren();
		assertEquals(1, children.size());
		assertSame(node2, children.get(0));
		
		assertEquals(true, node.removeChild(node2));
		assertEquals(false, node.removeChild(node1));
		assertEquals(false, node.removeChild(node2));
		
		log.info("testAddRemoveNode stop");
	}
	
	
	@Test
	public void testSetParentNull() {
		Node<Integer> node = new SimpleNode<Integer>(1);		
		Node<Integer> node1 = new SimpleNode<Integer>(2);
		node.addChild(node1);
		node1.setParent(null);
		assertNull(node1.getParent());
		assertEquals(0, node.getChildren().size());
	}
	
	
	@Test
	public void testChangeParent() {
		Node<Integer> root1 = new SimpleNode<Integer>(1);		
		Node<Integer> root2 = new SimpleNode<Integer>(2);
		Node<Integer> child = new SimpleNode<Integer>(0);
		root1.addChild(child);
		assertEquals(1, root1.getChildren().size());
		assertSame(root1, child.getParent());
		
		//child.setParent(root2);
		root2.addChild(child);
		assertEquals(0, root1.getChildren().size());
		assertEquals(1, root2.getChildren().size());
		assertSame(root2, child.getParent());
	}

	

}
