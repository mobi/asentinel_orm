package com.asentinel.common.orm;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.collections.tree.TreeUtils;
import com.asentinel.common.collections.tree.TreeUtils.NodeMatcher;
import com.asentinel.common.jdbc.JdbcUtils;
import com.asentinel.common.orm.ed.tree.AnnotationNodeMatcher;
import com.asentinel.common.orm.ed.tree.DefaultEntityDescriptorTreeRepository;
import com.asentinel.common.orm.ed.tree.EntityDescriptorTreeRepository;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.util.Assert;

/**
 * Class containing a set of static methods that can be used to create {@link EntityDescriptor}
 * trees by recursively looking for the {@link Child} annotation in domain objects.
 * 
 * @author Razvan Popian
 */
public final class EntityDescriptorUtils {
	private final static Logger log = LoggerFactory.getLogger(EntityDescriptorUtils.class);
	
	/**
	 * Matcher that matches any node.
	 */
	private final static NodeMatcher<EntityDescriptor> ANY = new NodeMatcher<>() {
		@Override
		public boolean match(Node<? extends EntityDescriptor> node) {
			return true;
		}
		
		@Override
		public String toString() {
			return "ANY";
		}
	};
	
	@Deprecated
	private final static EntityDescriptorTreeRepository edtr 
		= new DefaultEntityDescriptorTreeRepository(JdbcUtils.buildLobHandler());
	
	/**
	 * @return the static {@code edtr} that this class encapsulates. This method
	 * 		allows the {@code EntityDescriptorTreeRepository} instance to be used inside an application context.
	 * 
	 * @deprecated in favor of constructing the {@link EntityDescriptorTreeRepository} with its constructor.
	 */
	@Deprecated
	public static EntityDescriptorTreeRepository getEntityDescriptorTreeRepository() {
		return edtr;
	}

	
	
	private EntityDescriptorUtils() {}
	
	// no callback methods

	/**
	 * @see #getEntityDescriptorTree(Class, EntityDescriptorNodeCallback...)
	 * 
	 * @deprecated in favor of the corresponding method in {@link EntityDescriptorTreeRepository}.
	 */
	@Deprecated
	public static Node<EntityDescriptor> getEntityDescriptorTree(Class<?> clazz) {
		return edtr.getEntityDescriptorTree(clazz);
	}

	/**
	 * @see #getEntityDescriptorTree(Class, String, EntityDescriptorNodeCallback...)
	 * 
	 * @deprecated in favor of the corresponding method in {@link EntityDescriptorTreeRepository}.
	 */
	@Deprecated
	public static Node<EntityDescriptor> getEntityDescriptorTree(Class<?> clazz, String rootTableAlias) {
		return edtr.getEntityDescriptorTree(clazz, rootTableAlias);
	}
	
	/**
	 * @see #getEntityDescriptorTree(Node, EntityDescriptorNodeCallback...)
	 * 
	 * @deprecated in favor of the corresponding method in {@link EntityDescriptorTreeRepository}.
	 */
	@Deprecated
	public static Node<EntityDescriptor> getEntityDescriptorTree(Node<EntityDescriptor> root) {
		return edtr.getEntityDescriptorTree(root);
	}

	
	// callback methods

	/**
	 * @see EntityDescriptorTreeRepository#getEntityDescriptorTree(Class, EntityDescriptorNodeCallback...)
	 * 
	 * @deprecated in favor of the corresponding method in {@link EntityDescriptorTreeRepository}.
	 */
	@Deprecated
	public static Node<EntityDescriptor> getEntityDescriptorTree(
			Class<?> clazz, 
			EntityDescriptorNodeCallback ... nodeCallbacks
			) {
		return edtr.getEntityDescriptorTree(clazz, nodeCallbacks);
	}

	/**
	 * @see EntityDescriptorTreeRepository#getEntityDescriptorTree(Class, String, EntityDescriptorNodeCallback...)
	 * 
	 * @deprecated in favor of the corresponding method in {@link EntityDescriptorTreeRepository}.
	 */
	@Deprecated
	public static Node<EntityDescriptor> getEntityDescriptorTree(
			Class<?> clazz, 
			String rootTableAlias, 
			EntityDescriptorNodeCallback ... nodeCallbacks
			) {
		return edtr.getEntityDescriptorTree(clazz, rootTableAlias, nodeCallbacks);
	}
	

	/**
	 * @see EntityDescriptorTreeRepository#getEntityDescriptorTree(Node, EntityDescriptorNodeCallback...)
	 * 
	 * @deprecated in favor of the corresponding method in {@link EntityDescriptorTreeRepository}.
	 */
	@Deprecated
	public static Node<EntityDescriptor> getEntityDescriptorTree(
			Node<EntityDescriptor> root, 
			EntityDescriptorNodeCallback ... nodeCallbacks
			) {
		return edtr.getEntityDescriptorTree(root, nodeCallbacks);
	}

	
	

	// static helper methods follow
	
	/**
	 * @see #getEntityDescriptorNode(Node, Object...)
	 */
	public static EntityDescriptor getEntityDescriptor(Node<EntityDescriptor> root, Object ... path) throws IllegalArgumentException {
		return getEntityDescriptorNode(root, path).getValue();
	}
	

	/**
	 * Locates the {@link EntityDescriptor} node with the specified <code>path</code>. The <code>path</code>
	 * array can contain {@link Class}, {@link Annotation} or {@link NodeMatcher} instances. Any element in the {@code path}
	 * can be {@code null}, in this case this method will match all the elements on that level and consequently
	 * will return a value only if there is exactly one child at that level in the path (an exception is thrown otherwise).
	 * <br> 
	 * A {@code null} value in the path is useful on the first position of the path so that you don't need to specify the name 
	 * of the root class or the matcher for it.
	 * 
	 * @param root the root node of the tree in which we try to find the specified path.
	 * @param path array of {@code Class}, {@link Annotation} or {@code NodeMatcher} instances representing the path to 
	 * 			the target {@code EntityDescriptor} node.
	 * 
	 * @return the node for the specified <code>path</code>. If the <code>path</code> 
	 * 			is <code>null</code> or 0 length the root node is returned.
	 * 
	 * @throws IllegalArgumentException if one of the members of the <code>path</code> array is not 
	 * 			instance of {@link Class}, {@link Annotation} or {@link NodeMatcher} or if the <code>path</code> is not valid.
	 * 
	 * @see TreeUtils#getNode(Node, com.asentinel.common.collections.tree.TreeUtils.NodeMatcher...)
	 * @see EntityDescriptorNodeMatcher
	 */
	public static Node<EntityDescriptor> getEntityDescriptorNode(Node<EntityDescriptor> root, Object ... path) throws IllegalArgumentException {
		Assert.assertNotNull(root, "root");
		if (path == null || path.length == 0) {
			return root;
		}
		NodeMatcher<EntityDescriptor>[] finalMatchers = convertToNodeMatchers(path);
		return TreeUtils.getNode(root, finalMatchers);
	}

	/**
	 * Converts the <code>path</code> array that can contain either {@link Class},
	 * {@link Annotation} or {@link NodeMatcher} instances to an array of
	 * {@code NodeMatchers} ONLY.<br>
	 * If an element in the {@code path} is {@code null}, a {@code NodeMatcher} that
	 * will match anything will be created for the corresponding position of the
	 * path returned by this method.
	 * 
	 * @param path array of {@code Class} or {@code NodeMatcher} instances that will
	 *             be converted to a {@code NodeMatchers} array.
	 * 
	 * @return the {@code NodeMatchers} array for the specified <code>path</code>.
	 *         If the <code>path</code> is <code>null</code> or 0 length an empty
	 *         array is returned.
	 * 
	 * @throws IllegalArgumentException if one of the members of the
	 *                                  <code>path</code> array is not instance of
	 *                                  {@link Class}, {@link Annotation} or
	 *                                  {@link NodeMatcher}.
	 * 
	 * @see EntityDescriptorNodeMatcher
	 * @see AnnotationNodeMatcher
	 */
	@SuppressWarnings("unchecked")
	public static NodeMatcher<EntityDescriptor>[] convertToNodeMatchers(Object ... path) throws IllegalArgumentException {
		if (path == null || path.length == 0) {
			return new NodeMatcher[0];
		}
		NodeMatcher<EntityDescriptor>[] finalMatchers = new NodeMatcher[path.length];
		for (int i = 0; i < path.length; i++) {
			if (path[i] == null) {
				finalMatchers[i] = ANY;
			} else if (path[i] instanceof NodeMatcher) {
				finalMatchers[i] = (NodeMatcher<EntityDescriptor>) path[i];
			} else if (path[i] instanceof Class) {
				if (Annotation.class.isAssignableFrom((Class<?>) path[i])) {
					finalMatchers[i] = new AnnotationNodeMatcher((Class<? extends Annotation>) path[i]);
				} else {
					finalMatchers[i] = new EntityDescriptorNodeMatcher((Class<?>) path[i]);
				}
			} else {
				throw new IllegalArgumentException("Illegal path element: " + path[i] + " .");
			}
		}
		return finalMatchers;
	}
	
	/**
	 * Tests if the {@code entityDescriptor} parameter is either an entity or
	 * collection proxy.
	 */
	static boolean isProxyEntityDescriptor(EntityDescriptor entityDescriptor) {
		return entityDescriptor instanceof ProxyEntityDescriptor
				|| entityDescriptor instanceof CollectionProxyEntityDescriptor;
	}
	
	/**
	 * Checks if the {@code node} and the {@code builder} that will be used to
	 * create the value for the {@code node} match the given {@code path}. This
	 * method is designed to be used inside
	 * {@link EntityDescriptorNodeCallback#customize(Node, com.asentinel.common.orm.SimpleEntityDescriptor.Builder, SqlBuilderFactory)}
	 * to match nodes for customization using {@code NodeMatcher}s paths.
	 * 
	 * @param node
	 *            the current node, its value may be set or not, this is why we also
	 *            have the {@code builder} parameter. The {@code builder} parameter
	 *            is used to generate the actual value of the node so that the
	 *            matching can be performed.
	 * @param builder
	 *            the builder that will create the value for the node.
	 * @param path
	 *            the path to be matched.
	 * @return {@code true} if the path matches, {@code false} otherwise.
	 */
	@SafeVarargs
	public static boolean match(Node<EntityDescriptor> node, SimpleEntityDescriptor.Builder builder, 
			NodeMatcher<EntityDescriptor> ... path) {
		if (builder == null) {
			return false;
		}
		EntityDescriptor ed;
		try {
			// we don't want to change the state of the builder parameter because
			// we don't want to affect the clients that expect the builder state to stay the same,
			// so we clone it
			ed = builder.clone().build();
		} catch (Exception e) {
			log.error("match - Exception caught, failed to create an EntityDescriptor for class " + builder.getEntityClass() + ", so we will consider that the path " + Arrays.toString(path) + " does not match."
					+ " If the class is not @Table annotated, please use some other EntityDescriptorNodeCallback to process it, make sure it does not allow further callbacks processing"
					+ " and place it before the callback that triggered this message. Note that future versions of the asentinel-common ORM may not catch this error.", e);
			return false;
		}
		return match(node, ed, path);
	}	

	/**
	 * Checks if the {@code node} and the {@code entityDescriptor} match the given
	 * {@code path}. This method is designed to be used inside
	 * {@link EntityDescriptorNodeCallback#customize(Node, com.asentinel.common.orm.SimpleEntityDescriptor.Builder, SqlBuilderFactory)}
	 * to match nodes for customization using {@code NodeMatcher}s paths.
	 * 
	 * @param node
	 *            the current node, its value may be set or not, this is why we also
	 *            have the {@code entityDescriptor} parameter. The
	 *            {@code entityDescriptor} parameter is used as the actual value for
	 *            {@code node} parameter so that the matching can be performed.
	 * @param entityDescriptor
	 *            the actual value for the {@code node} parameter.
	 * @param path
	 *            the path to be matched.
	 * @return {@code true} if the path matches, {@code false} otherwise.
	 */
	@SafeVarargs
	public static boolean match(Node<EntityDescriptor> node, EntityDescriptor entityDescriptor, NodeMatcher<EntityDescriptor> ... path) {
		if (node == null || entityDescriptor == null) {
			return false;
		}
		if (path == null || path.length == 0) {
			return node.isRoot();
		}
			
		List<Node<EntityDescriptor>> ancestors = node.getAncestors();
		if (path.length != ancestors.size()) {
			return false;
		}
		
		// replace the current node with a new instance so that we don't affect the
		// value of the node we received as parameter which usually will be a node from the actual tree
		ancestors.set(ancestors.size() - 1, new SimpleNode<>(entityDescriptor));
		for (int i = ancestors.size() - 1; i >= 0; i--) {
			Node<EntityDescriptor> ancestor = ancestors.get(i);
			NodeMatcher<EntityDescriptor> matcher = path[i];
			if (!matcher.match(ancestor)) {
				return false;
			}
		}
		return true;
	}	
	
}
