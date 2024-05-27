package com.asentinel.common.orm;

import static com.asentinel.common.orm.EntityDescriptorUtils.convertToNodeMatchers;
import static com.asentinel.common.orm.EntityDescriptorUtils.isProxyEntityDescriptor;
import static com.asentinel.common.orm.EntityDescriptorUtils.match;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.TreeUtils.NodeMatcher;
import com.asentinel.common.orm.SimpleEntityDescriptor.Builder;
import com.asentinel.common.orm.ed.tree.DefaultEntityDescriptorTreeRepository;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.util.Assert;

/**
 * {@code EntityDescriptorNodeCallback} implementation that will match a node in an {@code EntityDescriptor}
 * tree for lazy loading. The node value will be set to a {@link ProxyEntityDescriptor} 
 * or a {@link CollectionProxyEntityDescriptor} instance depending on the type of the target. Note that
 * a collection proxy will only work for annotated fields and not for annotated methods.
 * This class is reusable and thread safe once configured.
 * <br><br>
 * The following precautions should be considered for proxy target classes:
 * 
 * <li> The entity proxies created with the help of this class will be loaded when a method other then the id (primary key) access methods or
 * {@code Object#toString()} is called on the proxy. Note that if the id getter or setter method have a call to a non static method of the class the
 * proxy load will still be triggered. The id getter and setter should strictly get and set the id.
 *  
 * <li>The collection proxies  created with the help of this class will be loaded when a method other than {@code Object#toString()} is called
 * on the proxy.
 *  
 * <li> <b>Any final method in the target class will NOT be intercepted and therefore will NOT trigger the load.</b>
 * You should not use final methods in classes that you plan to proxy or at least not rely on them for triggering the load.
 *  
 * <br><br>
 * The generated proxy will synchronize all the method calls on the proxy implicit lock. This ensures the visibility
 * of the lazy loaded state across all threads if the lazy loading occurs on a thread other than the one that
 * created the proxy.
 * 
 * <br><br>
 * The generated proxy is serializable if the proxied class is serializable and defines a {@code serialVersionUid}. It is the responsibility
 * of the proxy client to load the proxy before serialization so that the correct state is serialized. It is also the responsibility of the
 * client to load the proxy class before deserialization (use {@code ProxyFactory#getProxyObjectFactory(Class)} 
 * or {@code CollectionProxyFactory#getProxyObjectFactory(Class)} for this).
 *  
 * @see ProxyEntityDescriptor
 * @see CollectionProxyEntityDescriptor
 * @see EntityDescriptorNodeMatcher
 * @see Child#fetchType()
 * @see AutoEagerLoader
 *  
 * @author Razvan Popian
 */
public class AutoLazyLoader implements EntityDescriptorNodeCallback {
	private final static Logger log = LoggerFactory.getLogger(AutoLazyLoader.class);

	private final SqlBuilderFactory sqlBuilderFactory;
	private final Function<Object, ?> loader;
	private final NodeMatcher<EntityDescriptor>[] path;
	
	/**
	 * Used if the {@link DefaultEntityDescriptorTreeRepository} was configured with
	 * a {@link SqlBuilderFactory}. In this case the configured {@code SqlBuilderFactory} will be used for
	 * creating and executing the query that will load the proxy target object. <br>
	 * Use AutoLazyLoader#forPath(Object...) as an alternative to avoid compiler complaints when
	 * 			using a varargs argument with the first value {@code null}. 
	 * 
	 * @see AutoLazyLoader#forPath(Object...)
	 * @see #AutoLazyLoader(SqlBuilderFactory, Object...)
	 */
	public AutoLazyLoader(Object ... path) {
		this((SqlBuilderFactory) null, path);
	}

	
	/**
	 * @param sqlBuilderFactory the {@code SqlBuilderFactory} to use for loading the target entity. Could be {@code null},
	 * 			in this case the {@code SqlBuilderFactory} implementation configured in the {@link DefaultEntityDescriptorTreeRepository}
	 * 			will be used.
	 * @param path the path to the target {@code Node(s)} , can contain either {@code Class} 
	 * 			or {@code NodeMatcher} instances. If an element in the {@code path} is {@code null}, 
	 * 			a {@code NodeMatcher} that will match anything will be created for the corresponding position 
	 * 			of the path. This is especially useful for the last position in the path, so that we can match
	 * 			all the children of a certain node.
	 * 
	 * @see #forAllRootChildren(SqlBuilderFactory)
	 * @see EntityDescriptorNodeMatcher
	 */
	public AutoLazyLoader(SqlBuilderFactory sqlBuilderFactory, Object ... path) {
		this.sqlBuilderFactory = sqlBuilderFactory;
		this.loader = null;
		this.path = getPath(path);
	}

	/**
	 * @param loader {@code Function} that will be used for loading the target entity or collection. 
	 * 			If the target is an entity the parameter represents the entity id, if the target is a collection
	 * 			the parameter represents the foreign key from the parent.
	 * @param path the path to the target {@code Node}. The path should match exactly one node because the loader
	 * 			function can obviously load only one type of entity or collection. If the path matches multiple nodes
	 * 			it is likely that your code will not work correctly. 
	 * 
	 * @see EntityDescriptorNodeMatcher
	 */
	public AutoLazyLoader(Function<Object, ?> loader, Object ... path) {
		Assert.assertNotNull(loader, "loader");
		this.sqlBuilderFactory = null;
		this.loader = loader;
		this.path = getPath(path);
	}
	
	// @implNote: not called by the DefaultEntityDescriptorTreeRepository, but the interface
	// declaration forces us to  implement it.
	@Override
	public boolean customize(Node<EntityDescriptor> node, Builder builder) {
		return customize(node, builder, this.sqlBuilderFactory);
	}
	
	/**
	 * @throws IllegalStateException if the path is not valid (empty path or has less than 2 elements).
	 * @see #replaceWithProxyEntityDescriptor(Node, Builder, SqlBuilderFactory)
	 */
	@Override
	public boolean customize(Node<EntityDescriptor> node, Builder builder, SqlBuilderFactory sqlBuilderFactory) {
		if (path == null || path.length <= 1) {
			throw new IllegalStateException("The path must have at least 2 elements. The root class can not be lazy loaded.");
		}
		if (!match(node, builder, path)) {
			return true;
		}
		
		if (isProxyEntityDescriptor(node.getValue())) {
			if (log.isTraceEnabled()) {
				log.trace("customize - The EntityDescriptor " + node.getValue() 
					+ " for the path " + Arrays.toString(path) + " is already a proxy EntityDescriptor.");
			}
			return true;
		}
		
		return replaceWithProxyEntityDescriptor(node, builder, sqlBuilderFactory);
	}

	/**
	 * Use this method if you already know the target node for proxying and
	 * you also have the builder for it.
	 * @see #customize(Node, Builder, SqlBuilderFactory)
	 */
	public boolean replaceWithProxyEntityDescriptor(Node<EntityDescriptor> node, Builder builder, SqlBuilderFactory sqlBuilderFactory) {
		if (sqlBuilderFactory == null) {
			log.warn("replaceWithProxyEntityDescriptor - No SqlBuilderFactory passed as parameter. Was one configured in the " 
					+ DefaultEntityDescriptorTreeRepository.class.getSimpleName() + "?");
			if (this.sqlBuilderFactory == null) {
				throw new IllegalStateException("No SqlBuilderFactory passed as parameter and no SqlBuilderFactory passed on construction. Can not"
						+ " create the lazy loading entity descriptor.");
			}
			sqlBuilderFactory = this.sqlBuilderFactory;
		}
		
		if (StringUtils.hasText(builder.getJoinConditionsOverride()) && loader == null) {
			log.warn("customize - The join conditions are overridden in the @Child annotation for class " + builder.getEntityClass() + ". The lazy load"
					+ " may not work correctly unless a custom loader function is provided.");
		}
		
		// we need to run build to prepopulate the values of the fkname, pkname etc
		// we create a clone of the builder so that we don't affect the clients that expect 
		// the builder to be in the same state as it was passed to this method
		builder = builder.clone();
		builder.build();
		
		// FYI: Note that a collection proxy will only work for annotated fields
		// not for annotated methods
		if (builder.getParentRelationType() == RelationType.MANY_TO_ONE
				|| builder.getParentRelationType() == RelationType.MANY_TO_MANY) {
			return customizeForCollectionProxy(node, builder, sqlBuilderFactory);
		} else {
			return customizeForEntityProxy(node, builder, sqlBuilderFactory);
		}
	}
	
	@SuppressWarnings("unchecked")
	private boolean customizeForCollectionProxy(Node<EntityDescriptor> node, Builder builder, SqlBuilderFactory sqlBuilderFactory) {
		CollectionProxyEntityDescriptor ed;
		String fkName;
		String manyToManyRightFkName = null;
		String manyToManyTable = null;
		if (builder.getParentRelationType() == RelationType.MANY_TO_ONE) {
			if (StringUtils.hasText(builder.getFkName())) {
				fkName = builder.getFkName();
			} else {
				fkName = ((QueryReady) node.getParent().getValue()).getPkName();
			}
		} else if (builder.getParentRelationType() == RelationType.MANY_TO_MANY) {
			ManyToManyEntityDescriptor.Builder mtmBuilder = (ManyToManyEntityDescriptor.Builder) builder;
			manyToManyTable = mtmBuilder.getManyToManyTable();
			if (StringUtils.hasText(mtmBuilder.getManyToManyLeftFkName())) {
				fkName = mtmBuilder.getManyToManyLeftFkName();
			} else {
				fkName = ((QueryReady) node.getParent().getValue()).getPkName();
			}
			if (StringUtils.hasText(mtmBuilder.getManyToManyRightFkName())) {
				manyToManyRightFkName = mtmBuilder.getManyToManyRightFkName();
			} else {
				manyToManyRightFkName = builder.getPkName();
			}
		} else {
			throw new IllegalStateException("Null parent relation type.");
		}
		if (loader != null) {
			ed = new CollectionProxyEntityDescriptor(
					(Function<Object, Collection<?>>) loader, 
					builder.getEntityClass(), builder.getTargetMember(), fkName);
		} else {
			ed = new CollectionProxyEntityDescriptor(
					sqlBuilderFactory, 
					builder.getEntityClass(), builder.getTargetMember(), fkName, builder.getTableName(),
					manyToManyRightFkName, manyToManyTable);
		}
		node.setValue(ed);
		return false;
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean customizeForEntityProxy(Node<EntityDescriptor> node, Builder builder, SqlBuilderFactory sqlBuilderFactory) {
		Node<EntityDescriptor> parentNode = node.getParent();
		QueryReady parentQr = (QueryReady) parentNode.getValue();
		String parentTableAlias = parentQr.getTableAlias();
		String parentColAliasSep = parentQr.getColumnAliasSeparator();
		String fkName;
		if (builder.getParentRelationType() == RelationType.ONE_TO_MANY) {
			if (StringUtils.hasText(builder.getFkName())) {
				fkName = builder.getFkName();
			} else {
				fkName = builder.getPkName();
			}
		} else if (builder.getParentRelationType() == RelationType.ONE_TO_ONE) {
			fkName = parentQr.getPkName();
		} else {
			throw new IllegalStateException("Null parent relation type.");
		}
		fkName = parentTableAlias + parentColAliasSep + fkName;
		
		ProxyEntityDescriptor<?> ed;
		if (loader != null) {
			ed = new ProxyEntityDescriptor(loader,
					builder.getEntityClass(), builder.getTargetMember(), fkName);
		} else {
			ed = new ProxyEntityDescriptor<>(sqlBuilderFactory, 
					builder.getEntityClass(), builder.getTargetMember(), fkName, builder.getTableName());
		}
		node.setValue(ed);
		return false;
	}
	
	
	private static NodeMatcher<EntityDescriptor>[] getPath(Object ... path) {
		Assert.assertNotNull(path, "path");
		if (path.length <= 1) {
			log.trace("getPath - The path must have at least 2 elements. The root class can not be lazy loaded. "
					+ "An invalid path is fine only if you do not plan to do any matching (ie. call the #customize method).");
		}
		return convertToNodeMatchers(path);
	}
	
	/**
	 * Static factory method for proxying all the children of the root.
	 * @param sqlBuilderFactory the {@code SqlBuilderFactory} to use for the actual loading.
	 * @return a new {@code AutoLazyLoader}.
	 */
	public static AutoLazyLoader forAllRootChildren(SqlBuilderFactory sqlBuilderFactory) {
		Assert.assertNotNull(sqlBuilderFactory, "sqlBuilderFactory");
		return new AutoLazyLoader(sqlBuilderFactory, null, null);
	}

	/**
	 * Static factory method for proxying all the children of the root if the 
	 * {@link DefaultEntityDescriptorTreeRepository} was configured with
	 * a {@link SqlBuilderFactory}. In this case the configured {@code SqlBuilderFactory} will be used for
	 * creating and executing the query that will load the proxy target object.
	 *  
	 * @return a new {@code AutoLazyLoader}.
	 */
	public static AutoLazyLoader forAllRootChildren() {
		return new AutoLazyLoader(new Object[] {null, null});
	}
	
	/**
	 * Static factory method to avoid any compiler complaints when using {@link #AutoLazyLoader(Object...)}
	 * constructor with a varargs argument whose first value is {@code null}.
	 */
	public static AutoLazyLoader forPath(Object ... path) {
		return new AutoLazyLoader(path);
	}
	
}
