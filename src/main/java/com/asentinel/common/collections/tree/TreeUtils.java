package com.asentinel.common.collections.tree;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import com.asentinel.common.util.Assert;

/**
 * Collection of utility methods that apply to trees/nodes.
 * 
 * @see Node
 * 
 * @author Razvan Popian
 */
public final class TreeUtils {
	private static final Logger log = LoggerFactory.getLogger(TreeUtils.class);
	
	/**
	 * Copies the tree represented by the root node <code>source</code> into a
	 * new tree. The nodes in the new tree will reference the same values found in the nodes
	 * of the source tree, but a new tree structure will be created.
	 * <br>
	 * Note that all nodes in the resulted tree will be {@link SimpleNode}s regardless of the
	 * {@link Node} implementations in the source tree.
	 * 
	 * @param source the root of the tree to be copied, this must be a root node, otherwise
	 * 			an <code>IllegalArgumentException</code> is thrown.
	 * @return a copy of the source tree.
	 * 
	 * @see Node
	 * @see #transform(Node, NodeTransformer)
	 */
	public static <T> Node<T> copy(Node<T> source) {
		return transform(source, new CopyNodeTransformer<T>());
	}
	
	
	/**
	 * Transforms recursively each descendant of the <code>source</code> node provided
	 * as parameter using a {@link NodeTransformer} implementation provided by the client. Each node in
	 * the source tree will be transformed.
	 * 
	 * @param source the root of the tree to be transformed, this must be a root node, otherwise
	 * 			an <code>IllegalArgumentException</code> is thrown.
	 * @param transformer the transformer to use.
	 * @return the transformed tree, represented by its root node.
	 *
	 * @see Node
	 * @see NodeTransformer
	 */
	public static <S, D> Node<D> transform(Node<S> source, final NodeTransformer<S, D> transformer) {
		Assert.assertNotNull(source, "source");
		Assert.assertTrue(source.isRoot(), "The source node is not the root.");
		Assert.assertNotNull(transformer, "transformer");

		// the Node implementations are not required to implement
		// hashcode and equals, but we are ok with the default Object
		// implementations in this case
		final Map<Node<S>, Node<D>> map = new HashMap<>();
		
		source.traverse(node -> {
				Node<D> dest = transformer.transform(node);
				map.put(node, dest);
				if (!node.isRoot()) {
					Node<D> destParent = map.get(node.getParent());
					destParent.addChild(dest);
				}
			}
		);

		return map.get(source);
	}
	
	/**
	 * @see #getNode(Node, NodeMatcher ...)
	 * @see NodeMatcher
	 */
	@SuppressWarnings("unchecked")
	public static <T> Node<T> getNode(Node<T> root, NodeMatcher<T> singletonPath) {
		return getNode(root, new NodeMatcher[]{singletonPath});
	}
	
	/**
	 * This method locates the node denoted by the <code>path</code> parameter in the tree.
	 * Each {@link NodeMatcher} in the <code>path</code> array is matched against each node on the
	 * corresponding level. If on a level multiple nodes match this method throws <code>IllegalArgumentException</code>.
	 * @param root the root of the tree to be searched
	 * @param path array of {@link NodeMatcher}s
	 * @return the node if it can be found
	 * @throws IllegalArgumentException if the node can not be found or if multiple nodes matched.
	 * 
	 * @see NodeMatcher
	 */
	@SafeVarargs
	public static <T> Node<T> getNode(Node<T> root, NodeMatcher<T> ... path) {
		Assert.assertNotNull(root, "root");
		Assert.assertNotNull(path, "path");
		if (path.length == 0) {
			throw new IllegalArgumentException("Empty path.");
		}
		
		return getNode(0, Collections.singletonList(root), path);
	}
	
	@SafeVarargs
	private static <T> Node<T> getNode(int level, List<Node<T>> children, NodeMatcher<T> ... path) {
		if (level >= path.length) {
			throw new IllegalArgumentException("The level is bigger than the maximum path index.");
		}
		if (children.isEmpty()) {
			log.debug("getNode - Path that is too long: {}", Arrays.toString(path));
			throw new IllegalArgumentException("Invalid path, the path exceeds the depth of the branch.");
		}
		Node<T> target = null;
		for (Node<T> child: children) {
			if (path[level].match(child)) {
				if (target == null) {
					target = child;
				} else {
					log.debug("getNode - Already matched node: {}", target);
					log.debug("getNode - Currently matched node: {}", child);
					log.debug("getNode - Path that matched the 2 nodes: {}", Arrays.toString(path));
					log.debug("getNode - Path index that matched the 2 nodes: {}", level);
					throw new IllegalArgumentException("Multiple nodes matched for path index " + level + ".");
				}
			}
		}
		if (target == null) {
			log.debug("getNode - Path that failed to match: {}", Arrays.toString(path));
			log.debug("getNode - Path index that failed to match: {}", level);
			throw new IllegalArgumentException("Invalid path, no node matched for path index " + level + ".");
		}
		if (level == path.length - 1) {
			return target;
		} else {
			return getNode(++level, target.getChildren(), path);
		}
	}
	
	/**
	 * Counts the nodes in a tree. Note that a full traversal is required.
	 * @param root the root of the tree.
	 * @return the number of nodes in the tree.
	 */
	public static int countNodes(Node<?> root) {
		Assert.assertNotNull(root, "root");
		// the atomic integer is used because of its increment method. The Integer class is
		// immutable and would require a lot of boxing/unboxing to calculate the count.
		List<AtomicInteger> holder = Collections.singletonList(new AtomicInteger(0));
		root.traverse(node -> holder.get(0).incrementAndGet());
		return holder.get(0).get();
	}
	
	/**
	 * Callback interface that transforms a node and its value to another node implementation
	 * and possibly another value. Usually the resulted value is calculated
	 * based on the source value.
	 * 
	 * @param <S> the type of value in the source node.
	 * @param <D> the type of value in the destination node.
	 * 
	 * @see TreeUtils#transform(Node, NodeTransformer)
	 */
	@FunctionalInterface
	public static interface NodeTransformer<S, D> {
		
		/**
		 * Callback method that transforms a node.
		 * @param source the node to transform
		 * @return the node resulted from the transformation 
		 */
		public Node<D> transform(Node<S> source);
	}
	
	private static class CopyNodeTransformer<T> implements NodeTransformer<T, T> {

		@Override
		public Node<T> transform(Node<T> source) {
			return new SimpleNode<>(source.getValue());
		}
	}
	
	/**
	 * @see #getNode(Node, NodeMatcher...)
	 */
	@FunctionalInterface
	public static interface NodeMatcher<T> {
		
		public boolean match(Node<? extends T> node);
	}
	
	public static class StringNodeMatcher implements NodeMatcher<String> {
		
		private final String value;
		
		public StringNodeMatcher(String value) {
			this.value = value;
		}

		@Override
		public boolean match(Node<? extends String> node) {
			return ObjectUtils.nullSafeEquals(value, node.getValue());
		}
		
		@Override
		public String toString() {
			return value;
		}
	}
	
	
	private TreeUtils() {}
	
}
