package com.asentinel.common.orm;

import static com.asentinel.common.orm.EntityDescriptorUtils.getEntityDescriptor;
import static com.asentinel.common.orm.EntityDescriptorUtils.getEntityDescriptorNode;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.orm.ed.tree.DefaultEntityDescriptorTreeRepository;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class AutoEagerLoaderTestCase {
	
	SqlQuery qEx = createMock(SqlQuery.class);
	SqlBuilderFactory sbf = createMock(SqlBuilderFactory.class);
	DefaultEntityDescriptorTreeRepository edtr = new DefaultEntityDescriptorTreeRepository();
	{
		edtr.setSqlBuilderFactory(sbf);
	}

	@Before
	public void setup() {
		expect(sbf.getSqlQuery()).andReturn(qEx).anyTimes();
		replay(sbf);
	}
	
	@After
	public void tearDown() {
		verify(sbf);
	}	
	
	@Test(expected = IllegalArgumentException.class)
	public void wrongPath() {
		AutoEagerLoader.forPath((Object[]) null);
	}

	@Test(expected = IllegalStateException.class)
	public void noSqlBuilderFactory() {
		edtr.setSqlBuilderFactory(null);
		edtr.getEntityDescriptorTree(ParentEntity.class);
	}
	
	
	@Test
	public void valueSetToNullIfTheTargetNodeIsLazy() {
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(ParentEntity.class);
		Node<EntityDescriptor> testNode = getEntityDescriptorNode(root, null, ChildEntity.class);
		AutoEagerLoader ael = AutoEagerLoader.forAllRootChildren();
		ael.customize(testNode, new SimpleEntityDescriptor.Builder(ChildEntity.class));
		assertNull(testNode.getValue());
	}
	
	
	@Test
	public void noActionIfTheTargetNodeIsNotLazy() {
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(ParentEntity3.class);
		Node<EntityDescriptor> testNode = getEntityDescriptorNode(root, null, ChildEntity3.class);
		EntityDescriptor testEd = testNode.getValue();
		AutoEagerLoader ael = AutoEagerLoader.forAllRootChildren();
		ael.customize(testNode, new SimpleEntityDescriptor.Builder(ChildEntity3.class));
		assertSame(testEd, testNode.getValue());
	}
	
	@Test
	public void defaultBehavior() {
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(ParentEntity.class);
		EntityDescriptor testEd = getEntityDescriptor(root, null, ChildEntity.class);
		assertTrue(testEd instanceof ProxyEntityDescriptor);
	}
	
	@Test
	public void eagerLoad() {
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(ParentEntity.class, AutoEagerLoader.forPath(null, ChildEntity.class));
		EntityDescriptor testEd = getEntityDescriptor(root, null, ChildEntity.class);
		assertTrue(testEd instanceof SimpleEntityDescriptor);
	}
	

	@Test
	public void defaultBehaviorCollection() {
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(ParentEntity2.class);
		EntityDescriptor testEd = getEntityDescriptor(root, null, ChildEntity2.class);
		assertTrue(testEd instanceof CollectionProxyEntityDescriptor);
	}
	
	@Test
	public void eagerLoadCollection() {
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(ParentEntity2.class, AutoEagerLoader.forPath(null, ChildEntity2.class));
		EntityDescriptor testEd = getEntityDescriptor(root, null, ChildEntity2.class);
		assertTrue(testEd instanceof SimpleEntityDescriptor);
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

	
	@Table("parent")
	private static class ParentEntity2 {
		@PkColumn("pid")
		int pid;
		
		@Child(parentRelationType = RelationType.MANY_TO_ONE, fetchType = FetchType.LAZY)
		List<ChildEntity2> es;
	}
	
	@Table("child")
	private static class ChildEntity2 {
		@PkColumn("cid")
		int cid;
	}

	
	@Table("parent")
	private static class ParentEntity3 {
		@PkColumn("pid")
		int pid;
		
		@Child
		ChildEntity3 e;
	}
	
	@Table("child")
	private static class ChildEntity3 {
		@PkColumn("cid")
		int cid;
	}

}
