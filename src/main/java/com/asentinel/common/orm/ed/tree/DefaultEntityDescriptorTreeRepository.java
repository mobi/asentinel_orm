package com.asentinel.common.orm.ed.tree;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.StringUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.collections.tree.TreeUtils;
import com.asentinel.common.collections.tree.TreeUtils.NodeMatcher;
import com.asentinel.common.jdbc.ConversionSupport;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.orm.AutoLazyLoader;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.EntityDescriptorNodeCallback;
import com.asentinel.common.orm.EntityDescriptorNodeMatcher;
import com.asentinel.common.orm.FetchType;
import com.asentinel.common.orm.ManyToManyEntityDescriptor;
import com.asentinel.common.orm.QueryReady;
import com.asentinel.common.orm.QueryUtils;
import com.asentinel.common.orm.RelationType;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.orm.TargetChildMember;
import com.asentinel.common.orm.TargetMembers;
import com.asentinel.common.orm.TargetMembersHolder;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.util.Assert;
import com.asentinel.common.util.ConcurrentCache;
import com.asentinel.common.util.ListUtils;
import com.asentinel.common.util.Utils;

/**
 * Default implementation of the {@link EntityDescriptorTreeRepository} interface. To support circular
 * references a {@link SqlBuilderFactory} must be injected. If this is not injected circular references are
 * not supported and an exception is thrown if a circular reference is detected.
 * 
 * @see EntityDescriptorTreeRepository 
 * @see #setSqlBuilderFactory(SqlBuilderFactory)
 * 
 * @author Razvan Popian
 */
public class DefaultEntityDescriptorTreeRepository implements EntityDescriptorTreeRepository {
	
	private static final Logger log = LoggerFactory.getLogger(DefaultEntityDescriptorTreeRepository.class);
	
	/**
	 * This prefix MUST be different from the one {@link QueryUtils#nextTableAlias()} is
	 * using.
	 */
	static final String TABLE_ALIAS_PREFIX = "t";
	
	private final ConcurrentCache<CacheKey, Node<EntityDescriptor>> cache = new ConcurrentCache<>();
	
	private final LobHandler lobHandler;
	
	// not final to allow the setter to work. We need to support
	// circular references for EntityDescriptorTreeRepository and SqlBuilderFactory
	// FYI: if this is configured from a spring application context the volatile is not necessary,
	// but if this dependency is set later the volatile is critical to ensure the visibility of this reference 
	private volatile SqlBuilderFactory sqlBuilderFactory;
	
	// helper for automatic lazy loading circular references
	private final AutoLazyLoader autoLazyLoader = new AutoLazyLoader();

	
	private ConversionService conversionService;

	/**
	 * Default constructor. No {@code LobHandler} is set so
	 * there will be no support for LOBs in the resulting trees.
	 */
	public DefaultEntityDescriptorTreeRepository() {
		this((LobHandler) null);
	}

	/**
	 * Constructor that allows setting a {@code LobHandler}. The {@code LobHandler}
	 * can be {@code null}, in this case there will be no support for LOBs in the
	 * resulting trees.
	 */
	public DefaultEntityDescriptorTreeRepository(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}
	
	/**
	 * @see #DefaultEntityDescriptorTreeRepository(LobHandler)
	 */
	public DefaultEntityDescriptorTreeRepository(JdbcFlavor jdbcFlavor) {
		this(jdbcFlavor.getLobHandler());
	}
	
	public SqlBuilderFactory getSqlBuilderFactory() {
		return sqlBuilderFactory;
	}

	/**
	 * Sets the {@link SqlBuilderFactory} to use for lazy loading any circular reference member.
	 * If {@code null} the lazy loading of circular referenced members is not supported.
	 */
	public void setSqlBuilderFactory(SqlBuilderFactory sqlBuilderFactory) {
		this.sqlBuilderFactory = sqlBuilderFactory;
	}

	
	public ConversionService getConversionService() {
		return conversionService;
	}

	/**
	 * Sets the {@code ConversionService} to be used for special database types
	 * when reading result sets.
	 * 
	 * @see ConversionSupport
	 */
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	@Override
	public Node<EntityDescriptor> getEntityDescriptorTree(
			Class<?> clazz, 
			String rootTableAlias, 
			EntityDescriptorNodeCallback ... nodeCallbacks
			) {
		Assert.assertNotNull(clazz, "clazz");
		if (nodeCallbacks != null && nodeCallbacks.length != 0) {
			if (log.isDebugEnabled()) {
				log.debug("getEntityDescriptorTree - The tree for class " +
						clazz.getName() + " is NOT cached.");
			}
			// build the tree, we don't yet support caching for trees built with
			// node callbacks
			// TODO: a possible enhancement is to add support for caching even if NodeCallbacks are used 
			return getEntityDescriptorTreeInternal(clazz, rootTableAlias, nodeCallbacks);
		} else {
			Node<EntityDescriptor> tree = cache.get(new CacheKey(clazz, rootTableAlias), 
				() -> getEntityDescriptorTreeInternal(clazz, rootTableAlias, nodeCallbacks)
			);
			
			if (log.isDebugEnabled()) {
				log.debug("getEntityDescriptorTree - The tree for class " + 
						clazz.getName() + " is cached.");
			}
			// make a defensive copy of the cached tree, we don't want the client code to alter the structure
			// of the cached tree
			return TreeUtils.copy(tree);
		}
	}
	
	private Node<EntityDescriptor> getEntityDescriptorTreeInternal(
			Class<?> clazz, 
			String rootTableAlias, 
			EntityDescriptorNodeCallback ... nodeCallbacks
			) {
		Assert.assertNotNull(clazz, "clazz");
		long t0 = 0;
		if (log.isTraceEnabled()) {
			t0 = System.nanoTime(); 
		}
		SimpleEntityDescriptor.Builder builder = new SimpleEntityDescriptor.Builder(clazz)
			.tableAlias(getTableAlias(rootTableAlias, new IndexHolder(0)))
			.queryExecutor(Optional.ofNullable(sqlBuilderFactory).map(SqlBuilderFactory::getSqlQuery).orElse(null))
			.lobHandler(lobHandler)
			.conversionService(conversionService);
		Node<EntityDescriptor> root = new SimpleNode<>();
		if (!processNodeCallbackChain(root, builder, nodeCallbacks)) {
			throw new IllegalArgumentException("The root node value was transformed to a non QueryReady implementation "
					+ "by the EntityDescriptorNodeCallback chain. This is illegal.");
		}
		Node<EntityDescriptor> ret = getEntityDescriptorTree(root, nodeCallbacks);
		if (log.isTraceEnabled()) {
			long t1 = System.nanoTime();
			log.trace("getEntityDescriptorTreeInternal - Tree built in " + Utils.nanosToMillis(t1 - t0) + " ms.");
		}
		return ret;
	}
	
	

	@Override
	public Node<EntityDescriptor> getEntityDescriptorTree(
			Node<EntityDescriptor> root, 
			EntityDescriptorNodeCallback ... nodeCallbacks
			) {
		Assert.assertNotNull(root, "root");
		Assert.assertNotNull(root.getValue(), "root.getValue()");
		getEntityDescriptorTreeInternal(root, nodeCallbacks, null, null);
		return root;
	}
	
	
	private void getEntityDescriptorTreeInternal(
			Node<EntityDescriptor> parent,
			EntityDescriptorNodeCallback[] nodeCallbacks,
			Set<String> tableAliases, IndexHolder indexHolder
			) {
		
		Class<?> clazz = parent.getValue().getEntityClass();
		
		if (indexHolder == null) {
			indexHolder = new IndexHolder(1);
		}

		// detect duplicated table aliases
		if (tableAliases == null) {
			tableAliases = new HashSet<>();
		}
		if (parent.getValue() instanceof QueryReady) {
			String testTableAlias = ((QueryReady) parent.getValue()).getTableAlias(); 
			if (tableAliases.contains(testTableAlias.toUpperCase())) {
				if (parent.isRoot()) {
					// we can not have a duplicated table alias if we are just starting processing
					throw new RuntimeException("Unexpected error.");
				}
				throw new IllegalArgumentException("Duplicated table alias '" + testTableAlias + "' for member of type " + clazz.getName() 
						+ " in class " + parent.getParent().getValue().getEntityClass() + ".");
			}
			tableAliases.add(testTableAlias.toUpperCase());
		}

		// process the @Child annotations in the parent class
		TargetMembers targetMembers = TargetMembersHolder.getInstance().getTargetMembers(clazz);
		List<TargetChildMember> holders = targetMembers.getChildMembers();
		for (TargetChildMember holder: holders) {
			Child childAnn = holder.getAnnotation();
			AnnotatedElement element = holder.getAnnotatedElement();
			
			// determine the class of the child
			Class<?> finalClazz = holder.getChildType();
			Member member = (Member) element;
			
			// node and descriptor creation
			SimpleNode<EntityDescriptor> child = new SimpleNode<EntityDescriptor>();
			parent.addChild(child);
			SimpleEntityDescriptor.Builder builder = childAnn.parentRelationType() == RelationType.MANY_TO_MANY
					? new ManyToManyEntityDescriptor.Builder(finalClazz, childAnn.manyToManyTable()) 
					: new SimpleEntityDescriptor.Builder(finalClazz); 
			
			builder.name(childAnn.name())
				.pkName(childAnn.pkName())
				.fkName(childAnn.fkName())
				.tableName(childAnn.tableName())
				.tableAlias(getTableAlias(childAnn.tableAlias(), indexHolder))
				.columnAliasSeparator(childAnn.columnAliasSeparator())
				.parentRelationType(childAnn.parentRelationType())
				.parentJoinType(childAnn.parentJoinType())
				.joinConditionsOverride(childAnn.joinConditionsOverride())
				.forceManyAsOneInPaginatedQueries(childAnn.forceManyAsOneInPaginatedQueries())
				.queryExecutor(Optional.ofNullable(sqlBuilderFactory).map(SqlBuilderFactory::getSqlQuery).orElse(null))
				.lobHandler(lobHandler)
				.conversionService(conversionService)
				.targetMember(member)
				;
			if (builder instanceof ManyToManyEntityDescriptor.Builder) {
				((ManyToManyEntityDescriptor.Builder) builder)
					.manyToManyTable(childAnn.manyToManyTable())
					.manyToManyTableAlias(getTableAlias(childAnn.manyToManyTableAlias(), indexHolder))
					.manyToManyLeftFkName(childAnn.manyToManyLeftFkName())
					.manyToManyRightFkName(childAnn.manyToManyRightFkName())
					.manyToManyLeftJoinConditionsOverride(childAnn.manyToManyLeftJoinConditionsOverride())
					.manyToManyRightJoinConditionsOverride(childAnn.manyToManyRightJoinConditionsOverride());
			}
			
			// circular reference detection
			boolean potentialCircularReference = false;
			List<Node<EntityDescriptor>> ancestors = child.getAncestors();
			ancestors.remove(ancestors.size() - 1);
			for (Node<EntityDescriptor> node: ancestors) {
				Class<?> type = node.getValue().getEntityClass();
				if (type == null) {
					throw new NullPointerException("Null entity class detected.");
				}
				if (type.isAssignableFrom(finalClazz)
						|| finalClazz.isAssignableFrom(type)) {
					potentialCircularReference = true;
				}
			}
			TargetMembers childTargetMembers = TargetMembersHolder.getInstance().getTargetMembers(finalClazz);
			List<TargetChildMember> childHolders = childTargetMembers.getChildMembers();
			boolean proxyEd = false;
			if (potentialCircularReference && !childHolders.isEmpty()) {
				if (sqlBuilderFactory == null) {
					// no SqlBuilderFactory injected, so we can not lazy load this node
					// we have to throw exception
					throw new IllegalStateException("Potential circular reference detected, "
							+ "but no SqlBuilderFactory was injected so we can not use lazy loading. "
							+ "See the path below:\n" 
							+ getCircularRefPath(child, builder)
							);
				}
				if (log.isTraceEnabled()) {
					log.trace("getEntityDescriptorTreeInternal - Potential circular reference detected. "
							+ "Automatic lazy loading will be setup for the following path:\n" 
							+ getCircularRefPath(child, builder));
				}
				autoLazyLoader.replaceWithProxyEntityDescriptor(child, builder, sqlBuilderFactory);
				proxyEd = true;
			}			
			
			// deal with the lazy fetch type, if this is not already a proxy ed because of a circular reference
			if (!proxyEd && childAnn.fetchType() == FetchType.LAZY) {
				if (sqlBuilderFactory == null) {
					throw new IllegalStateException("No SqlBuilderFactory was injected in the " + 
							this.getClass().getSimpleName() + ", so we can not use lazy loading.");
				}
				// we replace the standard SimpleEntityDescriptor with a lazy loading EntityDescriptor
				autoLazyLoader.replaceWithProxyEntityDescriptor(child, builder, sqlBuilderFactory);
			}
			
			if (processNodeCallbackChain(child, builder, nodeCallbacks)) {
				getEntityDescriptorTreeInternal(child, nodeCallbacks, tableAliases, indexHolder);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static final String getCircularRefPath(Node<EntityDescriptor> node, SimpleEntityDescriptor.Builder builder) {
		@SuppressWarnings("rawtypes")
		List ancestors = node.getAncestors();
		if (ancestors.isEmpty()) {
			// something is wrong, this should never be empty
			// per the getAncestors contract
			return "";
		}
		ancestors.remove(ancestors.size() - 1);
		ancestors.add(builder);
		return ListUtils.toString(ancestors);
	}
	
	boolean processNodeCallbackChain(
			Node<EntityDescriptor> node,
			SimpleEntityDescriptor.Builder builder, 
			EntityDescriptorNodeCallback ... nodeCallbacks
			) {
		
		if (nodeCallbacks != null) {
			for (EntityDescriptorNodeCallback nodeCallback: nodeCallbacks) {
				if (nodeCallback == null) {
					throw new IllegalArgumentException("Null node callback detected in chain.");
				}
				boolean continueChain = nodeCallback.customize(node, builder, sqlBuilderFactory);
				if (!continueChain) {
					break;
				}
			}
		}
		
		if (node.getValue() == null) {
			// sets a QueryReady in the node, returns true to explore the children
			node.setValue(builder.build());
			return true;
		} else if (node.getValue() instanceof QueryReady) {
			// QueryReady already set in the node by one of the callbacks in the chain, returns true to explore the children
			return true;
		} else {
			// Not a query ready (maybe a CacheEntityDescriptor or a ProxyEntityDescriptor), will not look at the children of this node
			return false;
		}
	}
		
	/**
	 * Locates the {@link EntityDescriptor} node with the specified <code>path</code>. The <code>path</code>
	 * array can contain {@link Class} or {@link NodeMatcher} instances.
	 * @return the {@link EntityDescriptor} for the specified <code>path</code>. If the <code>path</code> 
	 * 			is <code>null</code> or 0 length the root <code>EntityDescriptor</code> is returned.
	 * @throws IllegalArgumentException if one of the members of the <code>path</code> array is not 
	 * 			instance of {@link Class} or {@link NodeMatcher} or if the <code>path</code> is not valid.
	 * @see TreeUtils#getNode(Node, com.asentinel.common.collections.tree.TreeUtils.NodeMatcher...)
	 */
	@SafeVarargs
	public static EntityDescriptor getEntityDescriptor(Node<EntityDescriptor> root, Object ... path) throws IllegalArgumentException {
		Assert.assertNotNull(root, "root");
		if (path == null || path.length == 0) {
			return root.getValue();
		}
		NodeMatcher<EntityDescriptor>[] finalMatchers = convertToNodeMatchers(path);
		return TreeUtils.getNode(root, finalMatchers).getValue();
	}

	/**
	 * Converts the <code>path</code> array that can contain either {@link Class} or {@link NodeMatcher} instances
	 * to an array of {@code NodeMatchers} ONLY.
	 * @return the {@code NodeMatchers} array for the specified <code>path</code>. If the <code>path</code> 
	 * 			is <code>null</code> or 0 length an empty array is returned.
	 * @throws IllegalArgumentException if one of the members of the <code>path</code> array is not 
	 * 			instance of {@link Class} or {@link NodeMatcher}.
	 */
	@SuppressWarnings("unchecked")
	static NodeMatcher<EntityDescriptor>[] convertToNodeMatchers(Object ... path) throws IllegalArgumentException {
		if (path == null || path.length == 0) {
			return new NodeMatcher[0];
		}
		NodeMatcher<EntityDescriptor>[] finalMatchers = new NodeMatcher[path.length];
		for (int i = 0; i < path.length; i++) {
			if (path[i] instanceof NodeMatcher) {
				finalMatchers[i] = (NodeMatcher<EntityDescriptor>) path[i];
			} else if (path[i] instanceof Class) {
				finalMatchers[i] = new EntityDescriptorNodeMatcher((Class<?>) path[i]);
			} else {
				throw new IllegalArgumentException("Illegal path element: " + path[i] + " .");
			}
		}
		return finalMatchers;
	}
	
	
	private static String getTableAlias(String tableAlias, IndexHolder indexHolder) {
		if (StringUtils.hasText(tableAlias)) {
			return tableAlias;
		} else {
			String ret = TABLE_ALIAS_PREFIX + Integer.toHexString(indexHolder.getIndex());
			indexHolder.inc();
			return ret;
		}	
	}
	
	private static class IndexHolder {
		private int index = 0;
		
		public IndexHolder(int index) {
			this.index = index;
		}
		
		public void inc() {
			index ++;
		}

		public int getIndex() {
			return index;
		}
	}
	
	private static class CacheKey {
		private final Class<?> clasz;
		private final String alias;
		
		public CacheKey(Class<?> clasz, String alias) {
			this.clasz = clasz;
			this.alias = alias;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((alias == null) ? 0 : alias.hashCode());
			result = prime * result + ((clasz == null) ? 0 : clasz.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			CacheKey other = (CacheKey) obj;
			if (alias == null) {
				if (other.alias != null)
					return false;
			} else if (!alias.equals(other.alias))
				return false;
			if (clasz == null) {
				if (other.clasz != null)
					return false;
			} else if (!clasz.equals(other.clasz))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return "CacheKey [clasz=" + clasz + ", alias=" + alias + "]";
		}
		
	}
	
}
