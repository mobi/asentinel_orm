package com.asentinel.common.orm;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.util.ReflectionTestUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.orm.SimpleEntityDescriptor.Builder;
import com.asentinel.common.orm.ed.tree.DefaultEntityDescriptorTreeRepository;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.IntRowMapper;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class AutoLazyLoaderTestCase {

	DefaultEntityDescriptorTreeRepository edtr = new DefaultEntityDescriptorTreeRepository();
	SqlQuery qEx = createMock(SqlQuery.class);
	SqlBuilderFactory sbf = createMock(SqlBuilderFactory.class);
	
	@Before
	public void setup() {
		expect(sbf.getSqlQuery()).andReturn(qEx).anyTimes();
		replay(sbf);
	}
	
	@After
	public void tearDown() {
		verify(sbf);
	}
	
	@Test
	public void alreadyProxyEntityDescriptor() {
		edtr.setSqlBuilderFactory(sbf);
		AutoLazyLoader l = AutoLazyLoader.forPath(ParentEntity.class, ChildEntity.class);
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(ParentEntity.class);
		assertEquals(1, root.getChildren().size());
		assertTrue(EntityDescriptorUtils.isProxyEntityDescriptor(root.getChildren().get(0).getValue()));
		EntityDescriptor ed = root.getChildren().get(0).getValue();
		
		// ensure the AutoLazyLoader would not create another proxy entity descriptor
		l.customize(root.getChildren().get(0), new SimpleEntityDescriptor.Builder(ChildEntity.class), sbf);
		assertSame(ed, root.getChildren().get(0).getValue());
	}
	
	@Test(expected = IllegalStateException.class)
	public void rootLazyLoad() {
		// we call customize just to trigger the path check
		new AutoLazyLoader(sbf, Invoice.class).customize(null, null);
	}
	
	@Test
	public void ensureProperFkNameisDetermined() {
		edtr.setSqlBuilderFactory(sbf);
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(ParentEntity.class, "root");
		assertEquals(1, root.getChildren().size());
		Node<EntityDescriptor> proxyEdNode = root.getChildren().get(0);
		ProxyEntityDescriptor<?> proxyEd = (ProxyEntityDescriptor<?>) proxyEdNode.getValue();
		assertTrue(EntityDescriptorUtils.isProxyEntityDescriptor(proxyEd));
		IntRowMapper m = (IntRowMapper) proxyEd.getEntityIdRowMapper();
		String fk = TargetMembersHolder.getInstance().getTargetMembers(ChildEntity.class)
				.getPkColumnMember().getPkColumnAnnotation().value();
		assertEquals("root_" + fk, m.getColumnName());
		
	}
	
	
	@Test
	public void ManyToOne_No_FK_Specified() {
		AutoLazyLoader l = new AutoLazyLoader(sbf, Invoice.class, Bill.class);
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(Invoice.class, l);
		System.out.println(root.toStringAsTree());
		for (Node<EntityDescriptor> edn: root.getChildren()) {
			assertTrue(edn.getValue() instanceof CollectionProxyEntityDescriptor);
			String expectedFkName = ((SimpleEntityDescriptor) root.getValue()).getPkName();
			assertEquals(expectedFkName, edn.getValue().getName());
		}
	}
	
	@Test
	public void ManyToOne_FK_Specified() {
		AutoLazyLoader l = new AutoLazyLoader(sbf, Invoice.class, Bill.class);
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(Invoice.class,
				new EntityDescriptorNodeCallback() {
					// trick to set the fk name so that we do not define another class for this test
					@Override
					public boolean customize(Node<EntityDescriptor> node, Builder builder) {
						if (Bill.class == builder.getEntityClass()) {
							builder.fkName("testFkId");
						}
						return true;
					}
				},
				l);
		System.out.println(root.toStringAsTree());
		for (Node<EntityDescriptor> edn: root.getChildren()) {
			assertTrue(edn.getValue() instanceof CollectionProxyEntityDescriptor);
			assertEquals("testFkId", edn.getValue().getName());
		}
	}
	

	@Test
	public void OneToMany_No_FK_Specified() {
		AutoLazyLoader l = new AutoLazyLoader(sbf, LazyLoaderTestCase.Bill.class, LazyLoaderTestCase.Invoice.class);
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(LazyLoaderTestCase.Bill.class, l);
		boolean found = false;
		SimpleEntityDescriptor red = (SimpleEntityDescriptor) root.getValue();
		for (Node<EntityDescriptor> edn: root.getChildren()) {
			if (edn.getValue() instanceof ProxyEntityDescriptor) {
				found = true;
				RowMapper<?> idMapper = edn.getValue().getEntityIdRowMapper();
				String expectedFkName = red.getTableAlias() + red.getColumnAliasSeparator() + LazyLoaderTestCase.Invoice.PK_NAME; 
				assertEquals(expectedFkName,
						ReflectionTestUtils.getField(idMapper, "colName").toString());
				assertEquals(expectedFkName,
						edn.getValue().getName());
			}
		}
		assertTrue(found);
		System.out.println(root.toStringAsTree());
	}
	
	@Test
	public void OneToMany_FK_Specified() {
		AutoLazyLoader l = new AutoLazyLoader(sbf, LazyLoaderTestCase.Bill.class, LazyLoaderTestCase.Invoice.class);
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(LazyLoaderTestCase.Bill.class,
			new EntityDescriptorNodeCallback() {
				// trick to set the fk name so that we do not define another class for this test
				@Override
				public boolean customize(Node<EntityDescriptor> node, Builder builder) {
					if (LazyLoaderTestCase.Invoice.class == builder.getEntityClass()) {
						builder.fkName("testFkId");
					}
					return true;
				}
		
			},
			l
		);
		boolean found = false;
		SimpleEntityDescriptor red = (SimpleEntityDescriptor) root.getValue();
		for (Node<EntityDescriptor> edn: root.getChildren()) {
			if (edn.getValue() instanceof ProxyEntityDescriptor) {
				found = true;
				RowMapper<?> idMapper = edn.getValue().getEntityIdRowMapper();
				String expectedFkName = red.getTableAlias() + red.getColumnAliasSeparator() + "testFkId"; 
				assertEquals(expectedFkName,
						ReflectionTestUtils.getField(idMapper, "colName").toString());
				assertEquals(expectedFkName,
						edn.getValue().getName());
			}
		}
		assertTrue(found);
		System.out.println(root.toStringAsTree());
	}

	@Test
	public void OneToOne() {
		AutoLazyLoader l = new AutoLazyLoader(sbf, LazyLoaderTestCase.Bill.class, LazyLoaderTestCase.Invoice.class);
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(LazyLoaderTestCase.Bill.class,
			new EntityDescriptorNodeCallback() {
				// trick to set the parent relation to ONE to ONE  so that we do not define another class for this test
				@Override
				public boolean customize(Node<EntityDescriptor> node, Builder builder) {
					if (LazyLoaderTestCase.Invoice.class == builder.getEntityClass()) {
						builder.parentRelationType(RelationType.ONE_TO_ONE);
					}
					return true;
				}
		
			},
			l
		);
		boolean found = false;
		SimpleEntityDescriptor red = (SimpleEntityDescriptor) root.getValue();
		for (Node<EntityDescriptor> edn: root.getChildren()) {
			if (edn.getValue() instanceof ProxyEntityDescriptor) {
				found = true;
				RowMapper<?> idMapper = edn.getValue().getEntityIdRowMapper();
				String expectedFkName = red.getTableAlias() + red.getColumnAliasSeparator() + red.getPkName(); 
				assertEquals(expectedFkName,
						ReflectionTestUtils.getField(idMapper, "colName").toString());
				assertEquals(expectedFkName,
						edn.getValue().getName());
			}
		}
		assertTrue(found);
		System.out.println(root.toStringAsTree());
	}
	

	@Test
	public void testAllChildrenAreProxiedForWildcard() {
		AutoLazyLoader l = new AutoLazyLoader(sbf, LazyLoaderTestCase.Bill.class, null);
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(LazyLoaderTestCase.Bill.class, l);
		System.out.println(root.toStringAsTree());
		SimpleEntityDescriptor red = (SimpleEntityDescriptor) root.getValue();
		for (Node<EntityDescriptor> edn: root.getChildren()) {
			assertTrue(edn.getValue() instanceof ProxyEntityDescriptor);
			RowMapper<?> idMapper = edn.getValue().getEntityIdRowMapper();
			String expectedFkName = null;
			if (edn.getValue().getEntityClass() == LazyLoaderTestCase.Invoice.class) {
				expectedFkName = red.getTableAlias() + red.getColumnAliasSeparator() + LazyLoaderTestCase.Invoice.PK_NAME;
			} else if (edn.getValue().getEntityClass() == LazyLoaderTestCase.CostCenter.class) {
				expectedFkName = red.getTableAlias() + red.getColumnAliasSeparator() + LazyLoaderTestCase.CostCenter.PK_NAME;
			}
			assertEquals(expectedFkName,
					ReflectionTestUtils.getField(idMapper, "colName").toString());
			assertEquals(expectedFkName,
					edn.getValue().getName());
		}
	}

	
	@Test
	public void ManyToMany_No_Fk_Specified() {
		AutoLazyLoader l = AutoLazyLoader.forAllRootChildren(sbf);
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(ManyToManyRoot.class, l);
		System.out.println(root.toStringAsTree());
		CollectionProxyEntityDescriptor ed = (CollectionProxyEntityDescriptor) root.getChildren().get(0).getValue();
		assertTrue(ed instanceof CollectionProxyEntityDescriptor);
		assertEquals("rid", ed.getName());
	}
	
	@Test
	public void ManyToMany_Fk_Specified() {
		AutoLazyLoader l = AutoLazyLoader.forAllRootChildren(sbf);
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(ManyToManyRoot.class,
				new EntityDescriptorNodeCallback() {
					// trick to set the fk name so that we do not define another class for this test
					@Override
					public boolean customize(Node<EntityDescriptor> node, Builder builder) {
						if (ManyToManyChild.class == builder.getEntityClass()) {
							((ManyToManyEntityDescriptor.Builder) builder).manyToManyLeftFkName("testFkId");
						}
						return true;
					}
				},

				l);
		System.out.println(root.toStringAsTree());
		CollectionProxyEntityDescriptor ed = (CollectionProxyEntityDescriptor) root.getChildren().get(0).getValue();
		assertTrue(ed instanceof CollectionProxyEntityDescriptor);
		assertEquals("testFkId", ed.getName());
	}
	
	// FYI: for many to many we are not testing here the value of the right fk
	
	
	@Table("root")
	private static class ManyToManyRoot {
		@PkColumn("rid")
		int id;
		
		@Child(parentRelationType = RelationType.MANY_TO_MANY, manyToManyTable = "link")
		List<ManyToManyChild> list;
	}
	
	@Table("child")
	private static class ManyToManyChild {
		@PkColumn("cid")
		int cid;
		
	}
	
	@Table("parent")
	private static class ParentEntity {
		@PkColumn("pid")
		int pid;
		
		@Child(fetchType = FetchType.LAZY)
		ChildEntity e;
	}
	
	@Table("child")
	private static class ChildEntity {
		@PkColumn("cid")
		int cid;
	}
	
}
