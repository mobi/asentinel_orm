package com.asentinel.common.collections.tree;

import static com.asentinel.common.collections.tree.TreeUtils.StringNodeMatcher;
import static org.junit.Assert.assertSame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.Test;


public class TreeUtilsFindTestCase {
	private static final Logger log = LoggerFactory.getLogger(TreeUtilsFindTestCase.class);
	
	Node<String> tree = new SimpleNode<>("child");
	Node<String> m = new SimpleNode<>("mother");
	Node<String> f = new SimpleNode<>("father");
	Node<String> gm = new SimpleNode<>("grandmother");
	Node<String> gf = new SimpleNode<>("grandfather");

	@Before
	public void setup() {
		tree.addChild(m).addChild(f);
		f.addChild(gm).addChild(gf);
	}
	
	@Test
	public void testFindNodeSingleNodeTree_Ok() {
		Node<String> root = new SimpleNode<>("root");
		
		Node<String> node = TreeUtils.getNode(root, new StringNodeMatcher("root"));
		log.debug("testFindNodeSingleNodeTree - node: {}", node);
		assertSame(root, node);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testFindNodeSingleNodeTree_Fail() {
		Node<String> root = new SimpleNode<>("root");
		
		TreeUtils.getNode(root, new StringNodeMatcher("root2"));
	}

	@Test
	public void testFindNode0() {
		log.debug("test - tree:\n{}", tree.toStringAsTree());
		
		Node<String> node = TreeUtils.getNode(tree, 
				new StringNodeMatcher("child"), new StringNodeMatcher("father"));
		log.debug("test - node: {}", node);
		assertSame(f, node);
	}
	
	@Test
	public void testFindNode1() {
		log.debug("test - tree:\n{}", tree.toStringAsTree());
		
		Node<String> node = TreeUtils.getNode(tree, 
				new StringNodeMatcher("child"), new StringNodeMatcher("mother"));
		log.debug("test - node: {}", node);
		assertSame(m, node);
	}

	@Test
	public void testFindNode2() {
		log.debug("test - tree:\n{}", tree.toStringAsTree());
		
		Node<String> node = TreeUtils.getNode(tree, 
				new StringNodeMatcher("child"), new StringNodeMatcher("father"), new StringNodeMatcher("grandfather"));
		log.debug("test - node: {}", node);
		assertSame(gf, node);
	}
	
	@Test
	public void testFindNode3() {
		log.debug("test - tree:\n{}", tree.toStringAsTree());
		
		Node<String> node = TreeUtils.getNode(tree, 
				new StringNodeMatcher("child"), new StringNodeMatcher("father"), new StringNodeMatcher("grandmother"));
		log.debug("test - node: {}", node);
		assertSame(gm, node);
	}
	
	@Test
	public void testFindNode4() {
		tree.addChild(new SimpleNode<>("grandfather"));
		log.debug("test - tree:\n{}", tree.toStringAsTree());
		
		Node<String> node = TreeUtils.getNode(tree, 
				new StringNodeMatcher("child"), new StringNodeMatcher("father"), new StringNodeMatcher("grandfather"));
		log.debug("test - node: {}", node);
		assertSame(gf, node);
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void testFindNodeFail0() {
		log.debug("test - tree:\n{}", tree.toStringAsTree());
		
		TreeUtils.getNode(tree, 
				new StringNodeMatcher("child"), new StringNodeMatcher("father1"), new StringNodeMatcher("grandmother"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testFindNodeFail1() {
		log.debug("test - tree:\n{}", tree.toStringAsTree());
		
		TreeUtils.getNode(tree, 
				new StringNodeMatcher("child"), new StringNodeMatcher("father"), new StringNodeMatcher("grandmother1"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testFindNodeFail2() {
		log.debug("test - tree:\n{}", tree.toStringAsTree());
		
		TreeUtils.getNode(tree, 
				new StringNodeMatcher("child1"), new StringNodeMatcher("father"), new StringNodeMatcher("grandmother"));
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void testFindNodeFail3_MultipleSameNodesOnTheSameLevel() {
		f.addChild(new SimpleNode<>("grandfather"));
		log.debug("test - tree:\n{}", tree.toStringAsTree());
		
		TreeUtils.getNode(tree, 
				new StringNodeMatcher("child"), new StringNodeMatcher("father"), new StringNodeMatcher("grandfather"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testFindNodeFail4_MultipleSameNodesOnTheSameLevel() {
		tree.addChild(new SimpleNode<>("father"));
		log.debug("test - tree:\n{}", tree.toStringAsTree());
		
		TreeUtils.getNode(tree, 
				new StringNodeMatcher("child"), new StringNodeMatcher("father"), new StringNodeMatcher("grandfather"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testFindNodeFail5_PathLongerThanTreeDepth() {
		log.debug("test - tree:\n{}", tree.toStringAsTree());
		
		TreeUtils.getNode(tree, 
				new StringNodeMatcher("child"), new StringNodeMatcher("father"), new StringNodeMatcher("grandfather"), new StringNodeMatcher("grand-grandfather"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyPath() {
		tree.addChild(new SimpleNode<>("father"));
		log.debug("test - tree:\n{}", tree.toStringAsTree());
		
		TreeUtils.getNode(tree);
	}
	
	// This failed for versions 1.55.13 and lower
	@Test
	public void testSameTargetNodeOnDifferentBranch() {
		f.removeChild(gf);
		m.addChild(new SimpleNode<>("grandmother"));
		log.debug("test - tree:\n{}", tree.toStringAsTree());
		
		TreeUtils.getNode(tree, 
				new StringNodeMatcher("child"), new StringNodeMatcher("father"), new StringNodeMatcher("grandmother"));
	}
}
