package com.asentinel.common.collections.tree;

import static org.junit.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

/**
 * {@link TreeUtils} test case, tests the {@link TreeUtils#copy(Node)} method
 * that calls {@link TreeUtils#transform(Node, com.asentinel.common.collections.tree.TreeUtils.NodeTransformer)}. 
 */
public class TreeUtilsTestCase {
	private static final Logger log = LoggerFactory.getLogger(TreeUtilsTestCase.class);
	
	@Test
	public void testTreeCopy() {
		Node<Integer> testNode1 = new SimpleNode<>(202);
		Node<Integer> testNode2 = new SimpleNode<>(30);
		Node<Integer> tree = new SimpleNode<>(1);
		tree.addChild(new SimpleNode<>(10)).addChild(new SimpleNode<>(20)).addChild(testNode2);
		tree.getChildren().get(0).addChild(new SimpleNode<>(100))
			.addChild(new SimpleNode<>(101));
		tree.getChildren().get(1).addChild(new SimpleNode<>(200).addChild(new SimpleNode<>(2001)))
			.addChild(testNode1).addChild(new SimpleNode<>(null));
		log.debug("testTreeCopy - Tree:\n{}", tree.toStringAsTree());
		
		Node<Integer> treeCopy = TreeUtils.copy(tree);
		log.debug("testTreeCopy - Tree copy:\n{}", tree.toStringAsTree());

		assertEquals(tree.toStringAsTree(), treeCopy.toStringAsTree());
		assertNotSame(tree, treeCopy);
	}
	
	
	@Test
	public void testNonRootNodeCopy() {
		Node<Integer> root = new SimpleNode<>(1);
		Node<Integer> child = new SimpleNode<>(2);
		root.addChild(child);
		try {
			TreeUtils.copy(child);
			fail("The copy operation should not succeed for a child node.");
		} catch (IllegalArgumentException e) {
			log.debug("testNonRootNodeCopy - Expected exception: {}", e.getMessage());
		}
	}

	@Test
	public void testSingleNodeTreeCopy() {
		Node<Integer> root = new SimpleNode<>(1);
		assertEquals(root.toStringAsTree(), TreeUtils.copy(root).toStringAsTree());
		assertNotSame(root, TreeUtils.copy(root));

	}

	@Test(expected = NullPointerException.class)
	public void testNullNodeCount() {
		TreeUtils.countNodes(null);
	}

	@Test
	public void testSingleNodeCount() {
		Node<Integer> root = new SimpleNode<>(1);
		assertEquals(1, TreeUtils.countNodes(root));
	}

	@Test
	public void testMultipleNodeCount() {
		Node<Integer> root = new SimpleNode<>(1);
		root.addChild(new SimpleNode<>(2)).addChild(new SimpleNode<>(3));
		assertEquals(3, TreeUtils.countNodes(root));
	}
}
