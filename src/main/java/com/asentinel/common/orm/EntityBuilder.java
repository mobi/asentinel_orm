package com.asentinel.common.orm;

import static com.asentinel.common.orm.EntityUtils.getEntityId;
import static com.asentinel.common.orm.EntityUtils.isLoadedProxy;
import static com.asentinel.common.orm.EntityUtils.isProxy;
import static com.asentinel.common.orm.EntityUtils.setEntityId;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;
import static java.util.Collections.EMPTY_SET;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.emptySortedMap;
import static java.util.Collections.emptySortedSet;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.collections.tree.TreeUtils;
import com.asentinel.common.orm.collections.OrmArrayList;
import com.asentinel.common.orm.collections.OrmCollection;
import com.asentinel.common.orm.collections.OrmLinkedHashMap;
import com.asentinel.common.orm.collections.OrmLinkedHashSet;
import com.asentinel.common.orm.collections.OrmTreeMap;
import com.asentinel.common.orm.collections.OrmTreeSet;
import com.asentinel.common.orm.proxy.ProxyFactorySupport;
import com.asentinel.common.orm.proxy.collection.CollectionProxyFactory;
import com.asentinel.common.util.Assert;

/**
 * {@code RowCallbackHandler} implementation that is able to create a multilevel one to many entity
 * tree. For example it can create a list of invoices with each invoice containing a list of bills and each
 * bill will contain a list of charges. Each invoice can have a vendor object attached. The client must supply 
 * on construction a tree of {@link EntityDescriptor} instances. The tree is represented by its root {@link Node}. 
 * The tree represents the relationships between the entities. This class supports multiple field entity id 
 * (composite primary key) objects.
 * 
 * <BR><BR>
 * 
 * The child entities are added to the parent using this algorithm:
 * <br>
 * 1) If the {@link EntityDescriptor} corresponding to the child entity has a target member 
 * (see {@link EntityDescriptor#getTargetMember()}) this member will be set/called using
 * reflection.
 * <br>
 * 2) If the target member is <code>null</code> the <code>EntityBuilder</code> will check to see if the
 * parent implements {@link ParentEntity}. If it does it will use this interface to add the child entity to
 * its parent.
 * <br>
 * 3) Otherwise the child entity will simply not be added.
 * 
 * <BR><BR>
 * 
 * This class is not reusable. <BR>
 * 
 * Usage example:<BR>
 * 
 *  Result set:<BR>
 *  <pre>
 *  	InvoiceId 	InvoiceNumber	BillId		BillNumber
 *  	1			I1				1			B1 
 *  	2			I2				2			B2
 *  	2			I2				3			B3   
 *  	1			I1				4			B4   	
 *  	1			I1				5			B5
 *  </pre>
 *  
 * Code that assumes we have the classes Invoice and Bill defined and the Invoice implements {@link ParentEntity}
 * and the Bill implements {@link Entity}:<BR>
 * 
 * <pre>
 *		Node<EntityDescriptor> ed = 
 *			new SimpleNode<EntityDescriptor>(EntityDescriptor.forIntPk(Invoice.class, new InvoiceRowMapper(), "InvoiceId"))
 *				.addChild(new SimpleNode<EntityDescriptor>(EntityDescriptor.forIntPk(Bill.class, new BillRowMapper(), "BillId"));
 *		EntityBuilder<Invoice> handler = new EntityBuilder<Invoice>(ed);
 *		SqlQuery query = ....;
 *		query.query("select InvoiceId, InvoiceNumber, BillId, BillNumber from ....", handler);
 *		List<Invoice> invoices = handler.getEntities();
 *
 *		// The invoices List will contain 2 Invoice objects, and the first Invoice object will have 3 bills and the 
 * 		// second will have 2 bills.
 * </pre>
 * 
 * @see Entity
 * @see ParentEntity
 * @see EntityDescriptor
 * @see EntityDescriptor#getTargetMember()
 * @see RelationType
 * 
 * @see #getEntityList()
 * @see #getEntityMap()
 * @see #getEntity()
 * @see #count()
 * 
 * @author Razvan Popian
 */
public class EntityBuilder<T> implements RowCallbackHandler {
	private static final Logger log = LoggerFactory.getLogger(EntityBuilder.class);

	/** the root node. */
	private final Node<EntityDescriptorAndEntity> nodeDescriptorAndEntity;
	
	/** Maps class names to the object cache of that class. */
	private final Map<String, Map<Object, EntityHolder>> mapClassToCache = new HashMap<>();
	
	/** the collection of entities that will be returned to the client */
	private final Map<Object, T> entities = new LinkedHashMap<>();
	
	// lazily inited if needed
	private List<OrmTreeSet<?>> ormTreeSets;
	
	/**
	 * Constructor for this handler. 
	 * @param nodeDescriptor the root descriptor node.
	 */
	@SuppressWarnings({ "unchecked"})
	public EntityBuilder(Node<? extends EntityDescriptor> nodeDescriptor) {
		Assert.assertNotNull(nodeDescriptor, "nodeDescriptor");
		if (log.isTraceEnabled()) {
			log.trace("<init> - EntityDescriptor tree:\n" + nodeDescriptor.toStringAsTree());
		}
		nodeDescriptorAndEntity = TreeUtils.transform((Node<EntityDescriptor>) nodeDescriptor, node -> {
			Assert.assertNotNull(node, "node");
			Assert.assertNotNull(node.getValue(), "node.getValue()");
			EntityDescriptor ed = node.getValue();
			mapClassToCache.put(ed.getEntityClass().getName(), new HashMap<>());
			return new SimpleNode<>(new EntityDescriptorAndEntity(node.getValue()));
		});
	}
	
	/**
	 * @return the list of root entities created by this handler.
	 */
	public List<T> getEntityList() {
		return new ArrayList<>(getEntityMap().values());
	}
	
	/**
	 * @return the map of root entities. The key is the entity id 
	 * 		and the value is the entity.
	 */
	public Map<Object, T> getEntityMap() {
		// populate the TreeSets if any
		if (ormTreeSets != null) {
			for (OrmTreeSet<?> ormTreeSet: ormTreeSets) {
				ormTreeSet.markAsOrmDone();
			}
		}
		
		// return the final entities map
		return entities;
	}
	
	/**
	 * Method that should be used to retrieve data when we know
	 * for sure that this {@code EntityBuilder} produced just one object.
	 * An exception is thrown if 0 or more than one entities were produced.
	 * @return the single root entity produced.
	 * @throws EmptyResultDataAccessException if no root entities were produced.
	 * @throws IncorrectResultSizeDataAccessException if more than 1 root entities
	 * 			were produced.
	 */
	public T getEntity() {
		List<T> list = getEntityList();
		if (list.size() == 1) {
			return list.get(0);
		}
		if (list.isEmpty()) {
			throw new EmptyResultDataAccessException(1);	
		}
		throw new IncorrectResultSizeDataAccessException(1, list.size());
	}
	
	/**
	 * @return number of root entities created by this handler.
	 */
	public int count() {
		return entities.size();
	}

	/** 
	 * @see RowCallbackHandler#processRow(ResultSet) 
	 */
	@SuppressWarnings({"unchecked" })
	@Override
	public void processRow(final ResultSet rs) throws SQLException {
		final int rowIndex = rs.getRow();
		nodeDescriptorAndEntity.traverse(childNode -> {
			try {
				Node<EntityDescriptorAndEntity> parentNode = childNode.getParent();

				// extract child object either from the cache or
				// from the resultset. 
				EntityDescriptorAndEntity descriptorAndEntity = childNode.getValue();
				descriptorAndEntity.setEntity(null);
				EntityDescriptor descriptor = descriptorAndEntity.getEntityDescriptor();
				if (descriptor instanceof CollectionProxyEntityDescriptor) {
					overrideCollectionWithProxy(parentNode, childNode, 
							(CollectionProxyEntityDescriptor) descriptor);
					return;
				}
				Object entityId = descriptor.getEntityIdRowMapper().mapRow(rs, rowIndex);
				if (entityId == null) {
					return;
				}
				Map<Object, EntityHolder> cache = mapClassToCache.get(descriptor.getEntityClass().getName());
				EntityHolder entityHolder = cache.get(entityId);
				Object entity;
				if (entityHolder == null) {
					entity = getEntity(descriptor, rs, rowIndex);
					setEntityId(entity, entityId);
					entityHolder = new EntityHolder(entity);						
					cache.put(entityId, entityHolder);
				} else if (isProxy(entityHolder.getEntity()) 
							&& !isLoadedProxy(entityHolder.getEntity())
							&& !(descriptor instanceof ProxyEntityDescriptor)) {
					entity = entityHolder.getEntity();
					Object entity2 = getEntity(descriptor, rs, rowIndex);
					if (!isProxy(entity2)) {
						// since the state of the proxy (entity2) is available in this resultset 
						// we populate the proxy (entity1) to avoid another DB call further down
						// the road
						setEntityId(entity2, entityId);	
						Field loaderField = ProxyFactorySupport.findLoaderField(entity.getClass()); // getClass is final, does not trigger the load 
						ReflectionUtils.setField(loaderField, entity, (Function<?, ?>) id -> entity2);
						EntityUtils.loadProxy(entity);
					}
				} else {
					entity = entityHolder.getEntity();
				}
				descriptorAndEntity.setEntity(entity);
				
				if (childNode.isRoot()) {
					// we have just created or found a root entity, so
					// we add it to the root entities map
					entities.put(entityId, (T) entity);
				}
				
				// add to parent if possible and necessary
				if (parentNode == null) {
					return;
				}
				EntityDescriptorAndEntity parentEntityDescriptorAndEntity = parentNode.getValue();
				addChild(parentEntityDescriptorAndEntity, descriptor, entityHolder);
			} catch (SQLException e) {
				throw new InvalidDataAccessApiUsageException("Failed to process EntityDescriptor " 
						+ childNode.getValue().getEntityDescriptor(), e);
			}
		});
	}
	
	private void addChild(
			EntityDescriptorAndEntity parentDescriptorAndEntity, 
			EntityDescriptor descriptor, 
			EntityHolder entityHolder) {
		Object parentEntity = parentDescriptorAndEntity.getEntity();
		Object entity = entityHolder.getEntity();
		if (entityHolder.shouldBeAdded(getEntityId(parentEntity), descriptor)) {
			Member member = descriptor.getTargetMember();			
			if (member != null) {
				if (member instanceof Field) {
					Field field = (Field) member;
					if (Collection.class.isAssignableFrom(field.getType())) {
						// this is a collection
						@SuppressWarnings("unchecked")
						Collection<Object> collection = (Collection<Object>) ReflectionUtils.getField(field, parentEntity);
						if (!isCollectionInitialized(collection)) {
							// FIXME: the following logic will work fine if the collection member type is an interface (Collection, Set, List etc),
							// but if the type is a concrete class this code will fail. For example it will fail for a declaration like this:
							// private LinkedList<Abc> list.
							if (SortedSet.class.isAssignableFrom(field.getType())) {
								// default to a OrmTreeSet
								OrmTreeSet<Object> ormTreeSet = new OrmTreeSet<>();
								collection = ormTreeSet;
								if (ormTreeSets == null) {
									ormTreeSets = new ArrayList<>();
								}
								ormTreeSets.add(ormTreeSet);
							} else if (Set.class.isAssignableFrom(field.getType())) {
								// default to a LinkedHashSet
								collection = new OrmLinkedHashSet<>();
							} else {
								// default to an ArrayList for all other cases
								collection = new OrmArrayList<>();
							}
							ReflectionUtils.setField(field, parentEntity, collection);
						}
						if (EntityUtils.isProxy(collection)) {
							return;
						}
						collection.add(entity);
					} else if (Map.class.isAssignableFrom(field.getType())) {
						// this is a map
						@SuppressWarnings("unchecked")
						Map<Object, Object> map = (Map<Object, Object>) ReflectionUtils.getField(field, parentEntity);
						if (!isMapInitialized(map)) {
							// FIXME: the following logic will work fine if the Map member type is an interface (Map, SortedMap),
							// but if the type is a concrete class this code may fail.
							if (SortedMap.class.isAssignableFrom(field.getType())) {
								// default to TreeMap
								map = new OrmTreeMap<>();
							} else {
								// default to LinkedHashMap
								map = new OrmLinkedHashMap<>();
							}
							ReflectionUtils.setField(field, parentEntity, map);
						}
						if (EntityUtils.isProxy(map)) {
							return;
						}
						map.put(getEntityId(entity), entity);
					} else {
						// this is a field we can set directly
						ReflectionUtils.setField((Field) member, parentEntity, entity);
					}
				} else if (member instanceof Method) {
					// this is a method, we invoke it directly
					ReflectionUtils.invokeMethod((Method) member, parentEntity, entity);
				} else {
					throw new IllegalStateException("Unsupported target member for entity descriptor " + descriptor + ".");
				}
			} else if (parentEntity instanceof Parent) {
				((Parent) parentEntity).addChild(entity, descriptor);
			} else {
				/*
				 * If we get here it may be an error (no target in the parent for this child entity),
				 * but it may be that we simply have some entity/table as part of the query, but we don't
				 * need the resulted entity.
				 */
				if (log.isTraceEnabled()) {
					log.trace("addChild - Unable to add the child resulted for the entity descriptor {} to its parent !", descriptor);
				}
				return;
			}
			
			// mark this entity as processed
			entityHolder.addEntityId(getEntityId(parentEntity), descriptor);
		}
	}
	
	private static void overrideCollectionWithProxy(Node<EntityDescriptorAndEntity> parentNode, 
			Node<EntityDescriptorAndEntity> childNode,
			CollectionProxyEntityDescriptor descriptor) {
		if (childNode.isRoot()) {
			throw new IllegalStateException("Can not proxy the root node entity.");
		}
		
		Object parentEntity = parentNode.getValue().getEntity();
		if (parentEntity == null) {
			return;
		}
		Object parentEntityId = EntityUtils.getEntityId(parentEntity);
		
		Member member = descriptor.getTargetMember();
		if (member == null || member instanceof Method) {
			throw new IllegalStateException("Can not create a collection proxy if the target member is a method or is null.");
		}
		Field field = (Field) member;
		Object collection = ReflectionUtils.getField(field, parentEntity);
		if (EntityUtils.isProxy(collection)) {
			// do nothing, the member is already a proxy. this is
			// happening because the parent was created earlier in the result set processing
			return;
		}
		if (collection instanceof OrmCollection) {
			// do nothing, this collection was initialized earlier in the resultset processing
			// so there is no need to make a proxy for it
			return;
		}
		Class<?> proxyType;
		if (Collection.class.isAssignableFrom(field.getType())) {
			if (isCollectionInitialized((Collection<?>) collection)) {
				proxyType = collection.getClass();
			} else {
				// FIXME: the following logic will work fine if the collection member type is an interface (Collection, Set, List etc),
				// but if the type is a concrete class this code will fail. For example it will fail for a declaration like this:
				// private LinkedList<Abc> list.
				if (SortedSet.class.isAssignableFrom(field.getType())) {
					// default to a TreeSet
					proxyType = TreeSet.class;
				} else if (Set.class.isAssignableFrom(field.getType())) {
					// default to a LinkedHashSet
					proxyType = LinkedHashSet.class;
				} else {
					proxyType = ArrayList.class;
				}
			}
		} else if (Map.class.isAssignableFrom(field.getType())) {
			if (isMapInitialized((Map<?, ?>) collection)) {
				proxyType = collection.getClass();
			} else {
				// FIXME: the following logic will work fine if the Map member type is an interface (Map, SortedMap),
				// but if the type is a concrete class this code may fail.
				if (SortedMap.class.isAssignableFrom(field.getType())) {
					// default to TreeMap
					proxyType = TreeMap.class;
				} else {
					// default to LinkedHashMap
					proxyType = LinkedHashMap.class;
				}
			}
		} else {
			throw new IllegalStateException("Unsupported target member for entity descriptor " + descriptor + ".");			
		}
		ReflectionUtils.setField(field, parentEntity,
			CollectionProxyFactory.getInstance().newProxy(proxyType, 
					 descriptor.getLoader(), parentEntityId)
		);
	}
	
	private static boolean isCollectionInitialized(Collection<?> collection) {
		return collection != null
				&& collection != EMPTY_LIST
				&& collection != EMPTY_SET
				&& collection != emptyList()
				&& collection != emptySet()
				&& collection != emptySortedSet();
	}
	
	private static boolean isMapInitialized(Map<?, ?> map) {
		return map != null
				&& map != EMPTY_MAP
				&& map != emptyMap()
				&& map != emptySortedMap();
	}
	
	private static Object getEntity(EntityDescriptor descriptor, ResultSet rs, int rowIndex) throws SQLException {
		Object entity = descriptor.getEntityRowMapper().mapRow(rs, rowIndex);
		if (entity == null) {
			throw new NullPointerException("Null entity returned for descriptor " + descriptor + ".");
		}
		if (!descriptor.getEntityClass().isAssignableFrom(entity.getClass())) {
			throw new ClassCastException(
					"Expected " + descriptor.getEntityClass().getName() 
					+ ", got " + entity.getClass().getName() + " for descriptor " + descriptor + " ."
					);
		}
		return entity;
	}
	

	@Override
	public String toString() {
		return "EntityBuilder [root=" + nodeDescriptorAndEntity.getValue().getEntityDescriptor() + "]";
	}

	
	/**
	 * Internal holder class for an entity. It keeps track of the parent entity ids
	 * to which the wrapped entity was already added.
	 */
	private static class EntityHolder {
		private final Object entity; 
		private final Set<EntityIdAndDescriptorHolder> entityIds = new HashSet<>();
		
	
		public EntityHolder(Object entity) {
			this.entity = entity;
		}
		
		public Object getEntity() {
			return entity;
		}
		
		public void addEntityId(Object entityId, EntityDescriptor entityDescriptor) {
			entityIds.add(new EntityIdAndDescriptorHolder(entityId, entityDescriptor));
		}
		
		public boolean shouldBeAdded(Object entityId, EntityDescriptor entityDescriptor) {
			return !entityIds.contains(new EntityIdAndDescriptorHolder(entityId, entityDescriptor));
		}

		@Override
		public String toString() {
			return "EntityHolder [entity=" + entity + ", entityIds="
					+ entityIds + "]";
		}
		
		private static class EntityIdAndDescriptorHolder {
			// the entityId object is required by the Entity interface contract
			// to implement hashCode and equals. The EntityDescriptors do 
			// not implement hashCode and equals, but the default Object implementation
			// is what we need in this case
			final Object entityId;
			final EntityDescriptor entityDescriptor;
			
			EntityIdAndDescriptorHolder(Object entityId, EntityDescriptor entityDescriptor) {
				this.entityId = entityId;
				this.entityDescriptor = entityDescriptor;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime
						* result
						+ ((entityDescriptor == null) ? 0 : entityDescriptor
								.hashCode());
				result = prime * result
						+ ((entityId == null) ? 0 : entityId.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				EntityIdAndDescriptorHolder other = (EntityIdAndDescriptorHolder) obj;
				if (entityDescriptor == null) {
					if (other.entityDescriptor != null)
						return false;
				} else if (!entityDescriptor.equals(other.entityDescriptor))
					return false;
				if (entityId == null) {
					if (other.entityId != null)
						return false;
				} else if (!entityId.equals(other.entityId))
					return false;
				return true;
			}
			
		}
	}
	
	private static class EntityDescriptorAndEntity {
		private final EntityDescriptor entityDescriptor;
		private Object entity;
		
		EntityDescriptorAndEntity(EntityDescriptor entityDescriptor) {
			this.entityDescriptor = entityDescriptor;
		}

		public EntityDescriptor getEntityDescriptor() {
			return entityDescriptor;
		}

		public Object getEntity() {
			return entity;
		}
		
		public void setEntity(Object entity) {
			this.entity = entity;
		}

		@Override
		public String toString() {
			return "EntityDescriptorAndEntity [entityDescriptor=" + entityDescriptor + ", entity=" + entity + "]";
		}

	}
	
}
