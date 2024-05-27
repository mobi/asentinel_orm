package com.asentinel.common.orm.ed.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.orm.AutoEagerLoader;
import com.asentinel.common.orm.CollectionProxyEntityDescriptor;
import com.asentinel.common.orm.Entity;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.EntityDescriptorNodeCallback;
import com.asentinel.common.orm.ProxyEntityDescriptor;
import com.asentinel.common.orm.RelationType;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.orm.SimpleEntityDescriptor.Builder;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class EDTRCircularRefDetectionTestCase {

	private final static Logger log = LoggerFactory.getLogger(EDTRCircularRefDetectionTestCase.class);
	
	DefaultEntityDescriptorTreeRepository edtr = new DefaultEntityDescriptorTreeRepository();
	
	@Mock
	SqlBuilderFactory sqlBuilderFactory;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testCircularReferenceDetection1() {
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
			edtr.getEntityDescriptorTree(Order3.class, new EntityDescriptorNodeCallback() {
				
				@Override
				public boolean customize(Node<EntityDescriptor> node, Builder builder) {
					if (Order0.class.equals(builder.getEntityClass())) {
						builder.entityClass(Order3.class);
					}
					return true;
				}
			});
		});
		log.debug("testCircularReferenceDetection1 - Expected exception message: " + e.getMessage());
		assertTrue(e.getMessage().toUpperCase().indexOf("CIRCULAR") >= 0);
	}

	@Test
	public void testCircularReferenceDetection2() {
		assertNotNull(edtr.getEntityDescriptorTree(Order3.class));
		log.debug("testCircularReferenceDetection2 - Circular reference not detected. This is good !");
	}

	@Test(expected = IllegalStateException.class)
	public void testCircularReferenceDetection3() {
		edtr.getEntityDescriptorTree(A.class);
	}

	
	@Test
	public void testCircularReferenceDetection4() {
		edtr.setSqlBuilderFactory(sqlBuilderFactory);
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(A.class);
		assertNotNull(root);
		log.debug("testCircularReferenceDetection4 - Tree: \n" + root.toStringAsTree());
		log.debug("testCircularReferenceDetection4 - Circular reference not detected. This is good !");
	}

	@Test
	public void testCircularReferenceDetection5() {
		// no circular reference, because  B does not have any children
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(C.class);
		assertNotNull(root);
		log.debug("testCircularReferenceDetection5 - Tree: \n" + root.toStringAsTree());
		log.debug("testCircularReferenceDetection5 - Circular reference not detected. This is good !");
	}

	
	@Test 
	public void circularReferenceDetectedButNodeGetsReplacedOnThe1stLevel() {
		edtr.setSqlBuilderFactory(sqlBuilderFactory);
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(
				A.class,
				AutoEagerLoader.forPath(A.class, A.class)
		);
		log.debug("circularReferenceDetectedButNodeGetsReplacedOnThe1stLevel - Tree: \n" + root.toStringAsTree());
		assertTrue(root.getValue() instanceof SimpleEntityDescriptor);
		assertTrue(root
					.getChildren().get(0).getValue() instanceof SimpleEntityDescriptor);
		assertTrue(root.getChildren().get(0)
						.getChildren().get(0).getValue() instanceof ProxyEntityDescriptor);
		assertTrue(root.getChildren().get(0)
						.getChildren().get(0).isLeaf());
	}
	
	@Test 
	public void circularReferenceDetectedButNodeGetsReplacedUpToThe2ndLevel() {
		edtr.setSqlBuilderFactory(sqlBuilderFactory);
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(
				A.class,
				AutoEagerLoader.forPath(A.class, A.class),
				AutoEagerLoader.forPath(A.class, A.class, A.class)
		);
		log.debug("circularReferenceDetectedButNodeGetsReplacedUpToThe2ndLevel - Tree: \n" + root.toStringAsTree());
		assertTrue(root.getValue() instanceof SimpleEntityDescriptor);
		assertTrue(root
					.getChildren().get(0).getValue() instanceof SimpleEntityDescriptor);
		assertTrue(root.getChildren().get(0)
						.getChildren().get(0).getValue() instanceof SimpleEntityDescriptor);
		assertTrue(root.getChildren().get(0)
						.getChildren().get(0)
						.getChildren().get(0).getValue() instanceof ProxyEntityDescriptor);
		assertTrue(root.getChildren().get(0)
				.getChildren().get(0)
				.getChildren().get(0).isLeaf());
	}	
	
	@Test 
	public void circularReferenceDetectedForCollectionButNodeGetsReplaced() {
		edtr.setSqlBuilderFactory(sqlBuilderFactory);
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(
				CollectionCircularReference.class,
				AutoEagerLoader.forPath(CollectionCircularReference.class, CollectionCircularReference.class)
		);
		log.debug("circularReferenceDetectedForCollectionButNodeGetsReplaced - Tree: \n" + root.toStringAsTree());
		assertTrue(root.getValue() instanceof SimpleEntityDescriptor);
		assertTrue(root
					.getChildren().get(0).getValue() instanceof SimpleEntityDescriptor);
		assertTrue(root.getChildren().get(0)
						.getChildren().get(0).getValue() instanceof CollectionProxyEntityDescriptor);
		assertTrue(root.getChildren().get(0)
						.getChildren().get(0).isLeaf());
	}
	
	/**
	 * This demonstrates the current behavior for a different branch circular
	 * reference. However, maybe the right way would be for the
	 * {@link EntityDescriptorTreeRepository} to detect the circular reference
	 * sooner (node c1 should be a proxy node) instead of later. So maybe the tree
	 * generated in this method should have only the root with one proxy child.
	 */
	@Test
	public void differentBranchCircularReference() {
		edtr.setSqlBuilderFactory(sqlBuilderFactory);
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(C2.class);
		log.debug("differentBranchCircularReference - Tree: \n" + root.toStringAsTree());
		assertTrue(root.getValue() instanceof SimpleEntityDescriptor);
		assertEquals(1, root.getChildren().size());

		Node<EntityDescriptor> c1 = root.getChildren().get(0);
		assertTrue(c1.getValue() instanceof SimpleEntityDescriptor);
		assertEquals(1, c1.getChildren().size());
		
		Node<EntityDescriptor> c12 = c1.getChildren().get(0);
		assertTrue(c12.getValue() instanceof ProxyEntityDescriptor);
	}
	
	
	@Table("r")
	private static class R {
		@PkColumn("id")
		int id;
		
		@Child
		C1 c1;
	}

	@Table("c1")
	private static class C1 extends R {
		
	}
	
	@Table("c2")
	private static class C2 extends R {

	}	
	
}

@Table("A")
class A {
	@PkColumn("id")
	int id;
	
	@Child
	A a;
}

@Table("B")
class B {
	@PkColumn("id")
	int id;
}

class C extends B {
	@Child
	B b;
}


@Table("Orders")
class Order0 implements Entity {

	@PkColumn("OrderId")
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

@Table("Orders")
class Order3 extends Order0 {

	@Child(parentRelationType=RelationType.MANY_TO_ONE)
	void addDetail(Detail3 option) {

	}

	@Child
	void setDelivery(Delivery3 delivery) {

	}



}

@Table("Details")
class Detail3 implements Entity {

	@PkColumn("DetailId")
	int id;



	@Child(parentRelationType=RelationType.MANY_TO_ONE)
	void addOption(Option3 option) {

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
class Option3 implements Entity {

	@PkColumn("OptionId")
	int id;
	
	@Child
	Order0 order;

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
class Delivery3 implements Entity {
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

@Table("CollectionCircularReference")
class CollectionCircularReference {
	@PkColumn("id")
	int id;	
	
	@Child(parentRelationType = RelationType.MANY_TO_ONE)
	List<CollectionCircularReference> children;
}