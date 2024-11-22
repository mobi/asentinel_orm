package com.asentinel.common.orm.ed.tree;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.NodeHandler;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.orm.CacheEntityDescriptor;
import com.asentinel.common.orm.Entity;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.RelationType;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class EDTRPropertiesTestCase {

	private static final Logger log = LoggerFactory.getLogger(EDTRPropertiesTestCase.class);
	
	EntityDescriptorTreeRepository edtr = new DefaultEntityDescriptorTreeRepository();
	
	public static boolean isChildInList(String name, Class<? extends Entity> clazz, List<Node<EntityDescriptor>> children) {
		for (Node<EntityDescriptor> node: children) {
			EntityDescriptor ed = node.getValue();
			if (name.equalsIgnoreCase(ed.getName().toString())
					&& clazz.equals(ed.getEntityClass())) {
				return true;
			}
		}
		return false;
	}
	
	@Test
	public void testAnnotatedProperties() {
		Node<EntityDescriptor> node = edtr.getEntityDescriptorTree(Order.class);
		log.debug("testAnnotatedProperties - tree: \n{}", node.toStringAsTree());
		
		node.traverse(new NodeHandler<EntityDescriptor>() {
			
			@Override
			public void handleNode(Node<EntityDescriptor> node) {
				SimpleEntityDescriptor ed = (SimpleEntityDescriptor) node.getValue();
				if (ed.getTableName().equalsIgnoreCase("Orders")) {
					List<Node<EntityDescriptor>> children = node.getChildren();
					assertEquals(2, children.size());
					assertTrue(isChildInList("DetailId", Detail.class, children));
					assertTrue(isChildInList("DeliveryId", Delivery.class, children));
				} else if (ed.getTableName().equalsIgnoreCase("Details")) {
					List<Node<EntityDescriptor>> children = node.getChildren();
					assertEquals(1, children.size());
					assertTrue(isChildInList("OptionId", Option.class, children));
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
	public void testAnnotatedPropertiesWithCallback() {
		Node<EntityDescriptor> node = edtr.getEntityDescriptorTree(Order.class,
                (node1, builder) -> {
                    if (node1.isRoot()) {
                        List<Cached> cachedList = Collections.emptyList();
                        CacheEntityDescriptor cached = CacheEntityDescriptor.forIntPk(Cached.class, "CachedId", cachedList);
                        node1.addChild(new SimpleNode<>(cached));
                    } else if (builder.getName().toString().equalsIgnoreCase("DeliveryId")) {
                        List<Cached> cachedList = Collections.emptyList();
                        CacheEntityDescriptor cached = CacheEntityDescriptor.forIntPk(Cached.class, "CachedId", cachedList);
                        node1.addChild(new SimpleNode<>(cached));
                    }
                    return true;
                }
        );
		log.debug("testAnnotatedProperties - tree: \n{}", node.toStringAsTree());
		
		node.traverse(node2 -> {
            EntityDescriptor ed = node2.getValue();
            if (ed.getName().toString().equalsIgnoreCase("OrderId")) {
                List<Node<EntityDescriptor>> children = node2.getChildren();
                assertEquals(3, children.size());
                assertTrue(isChildInList("DetailId", Detail.class, children));
                assertTrue(isChildInList("DeliveryId", Delivery.class, children));
                assertTrue(isChildInList("CachedId", Cached.class, children));
            } else if (ed.getName().toString().equalsIgnoreCase("DetailId")) {
                List<Node<EntityDescriptor>> children = node2.getChildren();
                assertEquals(1, children.size());
                assertTrue(isChildInList("OptionId", Option.class, children));
            } else if (ed.getName().toString().equalsIgnoreCase("OptionId")) {
                List<Node<EntityDescriptor>> children = node2.getChildren();
                assertEquals(0, children.size());
            } else if (ed.getName().toString().equalsIgnoreCase("DeliveryId")) {
                List<Node<EntityDescriptor>> children = node2.getChildren();
                assertEquals(1, children.size());
                assertTrue(isChildInList("CachedId", Cached.class, children));
            } else if (ed.getName().toString().equalsIgnoreCase("CachedId")) {
                List<Node<EntityDescriptor>> children = node2.getChildren();
                assertEquals(0, children.size());
            } else {
                fail("Unexpected descriptor.");
            }
        });
	}
}

@Table("Orders")
class Order implements Entity {
	
	@PkColumn("OrderId")
	int id;
	
	@Child(type=Detail.class, parentRelationType=RelationType.MANY_TO_ONE)
	List<Detail> details;
	
	@Child(name="DeliveryId")
	Delivery delivery;

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
class Detail implements Entity {
	
	@PkColumn("DetailId")
	int id;
	
	@Child(type=Option.class, parentRelationType=RelationType.MANY_TO_ONE)
	List<Option> options;
	
	
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
class Option implements Entity {
	
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
class Delivery implements Entity {
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
class Cached implements Entity {
	
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
