package com.asentinel.common.orm.ed.tree;

import static com.asentinel.common.orm.ed.tree.EDTRPropertiesTestCase.isChildInList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.NodeHandler;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.jdbc.ConversionSupport;
import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.orm.CacheEntityDescriptor;
import com.asentinel.common.orm.Entity;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.EntityDescriptorNodeCallback;
import com.asentinel.common.orm.RelationType;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.orm.SimpleEntityDescriptor.Builder;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class EDTRMethodsTestCase {

	private final static Logger log = LoggerFactory.getLogger(EDTRMethodsTestCase.class);
	
	DefaultEntityDescriptorTreeRepository edtr = new DefaultEntityDescriptorTreeRepository();	

	@Test
	public void testAnnotatedMethods() {
		SqlBuilderFactory sbf = mock(SqlBuilderFactory.class);
		SqlQuery qEx = mock(SqlQuery.class);
		when(sbf.getSqlQuery()).thenReturn(qEx);
		edtr.setSqlBuilderFactory(sbf);
		
		Node<EntityDescriptor> node = edtr.getEntityDescriptorTree(Order2.class);
		log.debug("testAnnotatedMethods - tree: \n" + node.toStringAsTree());

		node.traverse(new NodeHandler<EntityDescriptor>() {
			
			@Override
			public void handleNode(Node<EntityDescriptor> node) {
				SimpleEntityDescriptor ed = (SimpleEntityDescriptor) node.getValue();
				
				// validate we set the SqlQuery in the row mapper so that blobs
				// cab be lazily loaded
				assertNotNull(((ConversionSupport) ed.getEntityRowMapper()).getQueryExecutor());
				
				if (ed.getTableName().equalsIgnoreCase("Orders")) {
					List<Node<EntityDescriptor>> children = node.getChildren();
					assertEquals(2, children.size());
					assertTrue(isChildInList("DetailId", Detail2.class, children));
					assertTrue(isChildInList("DeliveryId", Delivery2.class, children));
				} else if (ed.getTableName().equalsIgnoreCase("Details")) {
					List<Node<EntityDescriptor>> children = node.getChildren();
					assertEquals(1, children.size());
					assertTrue(isChildInList("OptionId", Option2.class, children));
				} else if (ed.getTableName().equalsIgnoreCase("Options")) {
					List<Node<EntityDescriptor>> children = node.getChildren();
					assertEquals(0, children.size());
				} else if (ed.getTableName().equalsIgnoreCase("Deliveries")) {
					List<Node<EntityDescriptor>> children = node.getChildren();
					assertEquals(0, children.size());
				} else {
					fail("Unexpected descriptor.");
				}
			}
		});
	}


	@Test
	public void testAnnotatedMethodsWithCallback() {
		Node<EntityDescriptor> node = edtr.getEntityDescriptorTree(Order2.class,
				new EntityDescriptorNodeCallback() {
			@Override
			public boolean customize(Node<EntityDescriptor> node, Builder builder) {
				if (node.isRoot()) {
					List<Cached> cachedList = Collections.emptyList();
					CacheEntityDescriptor cached = CacheEntityDescriptor.forIntPk(Cached2.class, "CachedId", cachedList);
					node.addChild(new SimpleNode<EntityDescriptor>(cached));
				} else if (builder.getName().toString().equalsIgnoreCase("DeliveryId")) {
					List<Cached> cachedList = Collections.emptyList();
					CacheEntityDescriptor cached = CacheEntityDescriptor.forIntPk(Cached2.class, "CachedId", cachedList);
					node.addChild(new SimpleNode<EntityDescriptor>(cached));
				}
				return true;
			}
		}
				);
		log.debug("testAnnotatedMethods - tree: \n" + node.toStringAsTree());

		node.traverse(new NodeHandler<EntityDescriptor>() {
			
			@Override
			public void handleNode(Node<EntityDescriptor> node) {
				EntityDescriptor ed = node.getValue();
				if (ed.getName().toString().equalsIgnoreCase("OrderId")) {
					List<Node<EntityDescriptor>> children = node.getChildren();
					assertEquals(3, children.size());
					assertTrue(isChildInList("DetailId", Detail2.class, children));
					assertTrue(isChildInList("DeliveryId", Delivery2.class, children));
					assertTrue(isChildInList("CachedId", Cached2.class, children));
				} else if (ed.getName().toString().equalsIgnoreCase("DetailId")) {
					List<Node<EntityDescriptor>> children = node.getChildren();
					assertEquals(1, children.size());
					assertTrue(isChildInList("OptionId", Option2.class, children));
				} else if (ed.getName().toString().equalsIgnoreCase("OptionId")) {
					List<Node<EntityDescriptor>> children = node.getChildren();
					assertEquals(0, children.size());
				} else if (ed.getName().toString().equalsIgnoreCase("DeliveryId")) {
					List<Node<EntityDescriptor>> children = node.getChildren();
					assertEquals(1, children.size());
					assertTrue(isChildInList("CachedId", Cached2.class, children));
				} else if (ed.getName().toString().equalsIgnoreCase("CachedId")) {
					List<Node<EntityDescriptor>> children = node.getChildren();
					assertEquals(0, children.size());
				} else {
					fail("Unexpected descriptor.");
				}
			}
		});
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void testNonQueryReadyRoot() {
		edtr.getEntityDescriptorTree(Order2.class,
				new EntityDescriptorNodeCallback() {
					@Override
					public boolean customize(Node<EntityDescriptor> node, Builder builder) {
						if (node.isRoot()) {
							CacheEntityDescriptor cached = CacheEntityDescriptor.forIntPk(Cached2.class, 
									"CachedId", Collections.emptyList());
							node.setValue(cached);
						}
						return true;
					}
				}
		);
	}
	
	
	@Test
	public void testCachedBranchIsPruned() {
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(Order2.class,
				new EntityDescriptorNodeCallback() {
					@Override
					public boolean customize(Node<EntityDescriptor> node, Builder builder) {
						if (builder.getEntityClass() == Detail2.class) {
							CacheEntityDescriptor cached = CacheEntityDescriptor.forIntPk(Detail2.class, 
									"DetailId", Collections.emptyList());
							node.setValue(cached);
						}
						return true;
					}
				}
		);
		log.debug("testCachedBranchIsPruned - tree: \n" + root.toStringAsTree());
		// count nodes
		List<Integer> counter = new ArrayList<Integer>(1);
		counter.add(0);
		root.traverse(new NodeHandler<EntityDescriptor>() {

			@Override
			public void handleNode(Node<EntityDescriptor> node) {
				Integer c = counter.get(0) + 1;
				counter.set(0, c);
			}
		});
		assertEquals(3, counter.get(0).intValue());
		
	}
	
	
}



@Table("Orders")
class Order2 implements Entity {

	@PkColumn("OrderId")
	int id;


	@Child(parentRelationType=RelationType.MANY_TO_ONE)
	void addDetail(Detail2 option) {

	}

	@Child(name="DeliveryId")
	void setDelivery(Delivery2 delivery) {

	}


	@Override
	public Object getEntityId() {
		return id;
	}

	@Override
	public void setEntityId(Object entityId) {
		id = (Integer) entityId;
	}

}

@Table("Details")
class Detail2 implements Entity {

	@PkColumn("DetailId")
	int id;


	@Child(parentRelationType=RelationType.MANY_TO_ONE)
	void addOption(Option2 option) {

	}


	@Override
	public Object getEntityId() {
		return id;
	}

	@Override
	public void setEntityId(Object entityId) {
		id = (Integer) entityId;	
	}

}

@Table("Options")
class Option2 implements Entity {

	@PkColumn("OptionId")
	int id;

	@Override
	public Object getEntityId() {
		return id;
	}

	@Override
	public void setEntityId(Object entityId) {
		id = (Integer) entityId;
	}
}

@Table("Deliveries")
class Delivery2 implements Entity {
	@PkColumn("DeliveryId")
	int id;

	@Override
	public Object getEntityId() {
		return id;
	}

	@Override
	public void setEntityId(Object entityId) {
		id = (Integer) entityId;
	}
}

@Table("Cached")
class Cached2 implements Entity {

	@PkColumn("CachedId")
	int id;

	@Override
	public Object getEntityId() {
		return id;
	}

	@Override
	public void setEntityId(Object entityId) {
		id = (Integer) entityId;
	}
}
