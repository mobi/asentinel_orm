package com.asentinel.common.orm;

import static com.asentinel.common.orm.EntityDescriptorUtils.convertToNodeMatchers;
import static com.asentinel.common.orm.EntityDescriptorUtils.isProxyEntityDescriptor;
import static com.asentinel.common.orm.EntityDescriptorUtils.match;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.TreeUtils.NodeMatcher;
import com.asentinel.common.orm.SimpleEntityDescriptor.Builder;
import com.asentinel.common.orm.mappers.Child;

/**
 * {@code EntityDescriptorNodeCallback} implementation that will match a node in an {@code EntityDescriptor}
 * tree for eager loading. By default all nodes are eager loaded, but this can be overridden by setting the {@link Child#fetchType()}
 * property to {@code FetchType#LAZY}. If this is the case this class can be used to load eagerly a member that was configured to be 
 * loaded lazily in its {@code Child} annotation.
 * This class is reusable and thread safe once configured.
 * 
 * @see ProxyEntityDescriptor
 * @see CollectionProxyEntityDescriptor
 * @see EntityDescriptorNodeMatcher
 * @see AutoLazyLoader
 * @see Child#fetchType()
 * 
 * @author Razvan Popian
 */
public class AutoEagerLoader implements EntityDescriptorNodeCallback {
	
	private final NodeMatcher<EntityDescriptor>[] path;
	
	public AutoEagerLoader(Object ... path) {
		if (path == null || path.length <= 1) {
			throw new IllegalArgumentException("The path must have at least 2 elements. The root class is always eager loaded.");
		}
		this.path = convertToNodeMatchers(path);
	}

	@Override
	public boolean customize(Node<EntityDescriptor> node, Builder builder) {
		if (isProxyEntityDescriptor(node.getValue())
				&& match(node, builder, path)) {
			node.setValue(null);
		}
		return true;
	}
	
	/**
	 * Static factory method for eagerly loading all the children of the root.
	 *  
	 * @return a new {@code AutoEagerLoader}.
	 */
	public static AutoEagerLoader forAllRootChildren() {
		return new AutoEagerLoader(null, null);
	}

	/**
	 * Static factory method - alternative to the constructor.
	 */
	public static AutoEagerLoader forPath(Object ... path) {
		return new AutoEagerLoader(path);
	}
	
}
