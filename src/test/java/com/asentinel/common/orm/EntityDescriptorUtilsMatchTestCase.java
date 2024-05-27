package com.asentinel.common.orm;

import static com.asentinel.common.orm.EntityDescriptorUtils.match;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.orm.SimpleEntityDescriptor.Builder;
import com.asentinel.common.orm.ed.tree.DefaultEntityDescriptorTreeRepository;

public class EntityDescriptorUtilsMatchTestCase {
	
	// Invoice
	//		/Bill
	//			/Charge
	private final Node<EntityDescriptor> root = new DefaultEntityDescriptorTreeRepository().getEntityDescriptorTree(Invoice.class);

	
	@Test
	public void noNode() {
		assertFalse(match((Node<EntityDescriptor>) null, new Builder(Invoice.class)));
	}
	
	@Test
	public void noBuilder() {
		assertFalse(match(new SimpleNode<>(null), (Builder) null));
	}

	// root node
	
	@Test
	public void rootNode_emptyPath() {
		assertTrue(match(root, new Builder(Invoice.class)));
	}
	
	@Test
	public void rootNode_rightPath() {
		Builder b = new Builder(Invoice.class);
		assertTrue(
			match(root, b,
				new EntityDescriptorNodeMatcher(Invoice.class))
		);
		// ensure build() was not called
		assertTrue(b.getTableName() == null);
	}
	
	@Test
	public void rootNode_wrongPath() {
		assertFalse(
			match(root, new Builder(Invoice.class),
				new EntityDescriptorNodeMatcher(Bill.class))
		);
	}

	@Test
	public void rootNode_pathTooLong() {
		assertFalse(
			match(root, new Builder(Invoice.class),
				new EntityDescriptorNodeMatcher(Invoice.class),
				new EntityDescriptorNodeMatcher(Bill.class))
		);
	}


	// intermediate nodes

	@Test
	public void intermediateNode_emptyPath() {
		assertFalse(match(root.getChildren().get(0), new Builder(Bill.class)));
	}
	
	@Test
	public void intermediateNode_rightPath() {
		Builder b = new Builder(Bill.class);
		assertTrue(
			match(root.getChildren().get(0), b,
				new EntityDescriptorNodeMatcher(Invoice.class), 
				new EntityDescriptorNodeMatcher(Bill.class))
		);
		// ensure build() was not called
		assertTrue(b.getTableName() == null);
	}

	@Test
	public void intermediateNode_wrongPath() {
		assertFalse(
			match(root.getChildren().get(0), new Builder(Bill.class),
				new EntityDescriptorNodeMatcher(Invoice.class), 
				new EntityDescriptorNodeMatcher(Charge.class))
		);
	}

	@Test
	public void intermediateNode_wrongPath_tooShort() {
		assertFalse(
			match(root.getChildren().get(0), new Builder(Bill.class),
				new EntityDescriptorNodeMatcher(Invoice.class))
		);
	}

	@Test
	public void intermediateNode_wrongPath_tooLong() {
		assertFalse(
			match(root.getChildren().get(0), new Builder(Bill.class),
					new EntityDescriptorNodeMatcher(Invoice.class), 
					new EntityDescriptorNodeMatcher(Bill.class),
					new EntityDescriptorNodeMatcher(Charge.class))
		);
	}
	
	
	// leaf nodes

	@Test
	public void leafNode_emptyPath() {
		assertFalse(match(root.getChildren().get(0).getChildren().get(0), new Builder(Charge.class)));
	}

	@Test
	public void leafNode_rightPath() {
		Builder b = new Builder(Charge.class);
		assertTrue(
			match(root.getChildren().get(0).getChildren().get(0), b,
				new EntityDescriptorNodeMatcher(Invoice.class), 
				new EntityDescriptorNodeMatcher(Bill.class),
				new EntityDescriptorNodeMatcher(Charge.class))
		);
		// ensure build() was not called
		assertTrue(b.getTableName() == null);
	}

	@Test
	public void leafNode_wrongPath() {
		assertFalse(
			match(root.getChildren().get(0).getChildren().get(0), new Builder(Charge.class),
				new EntityDescriptorNodeMatcher(Invoice.class), 
				new EntityDescriptorNodeMatcher(Bill.class),
				new EntityDescriptorNodeMatcher(Invoice.class))
		);
	}

	@Test
	public void leafNode_wrongPath_tooShort() {
		assertFalse(
			match(root.getChildren().get(0), new Builder(Bill.class),
				new EntityDescriptorNodeMatcher(Invoice.class))
		);
	}

	@Test
	public void leafNode_wrongPath_tooLong() {
		assertFalse(
			match(root.getChildren().get(0).getChildren().get(0), new Builder(Bill.class),
					new EntityDescriptorNodeMatcher(Invoice.class), 
					new EntityDescriptorNodeMatcher(Bill.class),
					new EntityDescriptorNodeMatcher(Charge.class),
					new EntityDescriptorNodeMatcher(Invoice.class)
					)
		);
	}

	
	@Test
	public void noAlterationToTheLeafNode() {
		Node<EntityDescriptor> target = root.getChildren().get(0).getChildren().get(0); 
		EntityDescriptor targetValue = target.getValue();
		assertTrue(
			match(target, new Builder(Charge.class),
				new EntityDescriptorNodeMatcher(Invoice.class), 
				new EntityDescriptorNodeMatcher(Bill.class),
				new EntityDescriptorNodeMatcher(Charge.class))
		);
		assertSame(targetValue, root.getChildren().get(0).getChildren().get(0).getValue());
	}

	
	@Test
	public void notTableAnnotatedEntity() {
		assertFalse(match(root, new Builder(Integer.class), new EntityDescriptorNodeMatcher(Invoice.class)));
	}
}
