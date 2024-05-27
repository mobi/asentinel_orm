package com.asentinel.common.orm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.jdbc.ReusableRowMappers;
import com.asentinel.common.orm.ed.tree.EntityDescriptorTreeRepository;
import com.asentinel.common.orm.jql.SqlBuilder;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.mappers.IntRowMapper;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class CollectionProxyEntityDescriptorStaticMethodsTestCase {

	static final int PARENT_ID = 17;
	
	SqlBuilderFactory sbFactory = mock(SqlBuilderFactory.class);
	
	EntityDescriptorTreeRepository treeRepo = mock(EntityDescriptorTreeRepository.class);
	
	@SuppressWarnings("unchecked")
	SqlBuilder<TestLl> sb = mock(SqlBuilder.class);
	
	@SuppressWarnings("rawtypes")
	ArgumentCaptor<Node> capture = ArgumentCaptor.forClass(Node.class);

	List<TestLl> loadedTestLl = Collections.emptyList();

	@Test
	public void testNoRootTable(){
		when(sbFactory.newSqlBuilder(TestLl.class)).thenReturn(sb);
		when(sb.select()).thenReturn(sb);
		when(sb.where()).thenReturn(sb);
		when(sb.column("fk")).thenReturn(sb);
		when(sb.eq(PARENT_ID)).thenReturn(sb);
		when(sb.exec()).thenReturn(loadedTestLl);
		
		Function<Object, Collection<?>> loader = CollectionProxyEntityDescriptor.getLoader(sbFactory, TestLl.class, "fk", null);
		
		Collection<?> entities = loader.apply(PARENT_ID);
		
		assertSame(loadedTestLl, entities);
	}

	
	@Test
	public void testRootTableSame(){
		when(sbFactory.newSqlBuilder(TestLl.class)).thenReturn(sb);
		when(sb.select()).thenReturn(sb);
		when(sb.where()).thenReturn(sb);
		when(sb.column("fk")).thenReturn(sb);
		when(sb.eq(PARENT_ID)).thenReturn(sb);
		when(sb.exec()).thenReturn(loadedTestLl);
		
		Function<Object, Collection<?>> loader = CollectionProxyEntityDescriptor.getLoader(sbFactory, TestLl.class, "fk", "Test");
		
		Collection<?> entities = loader.apply(PARENT_ID);
		
		assertSame(loadedTestLl, entities);
	}


	@SuppressWarnings("unchecked")
	@Test
	public void testRootTableOverride(){
		when(sbFactory.getEntityDescriptorTreeRepository()).thenReturn(treeRepo);
		// the return value does not matter
		when(treeRepo.getEntityDescriptorTree(capture.capture()))
			.thenReturn(new SimpleNode<EntityDescriptor>(
				new EntityDescriptor(TestLl.class, new IntRowMapper("id"), ReusableRowMappers.ROW_MAPPER_INTEGER, "test")));
		when(sbFactory.newSqlBuilder(TestLl.class)).thenReturn(sb);
		when(sb.select(any(Node.class))).thenReturn(sb);
		when(sb.where()).thenReturn(sb);
		when(sb.column("fk")).thenReturn(sb);
		when(sb.eq(PARENT_ID)).thenReturn(sb);
		when(sb.exec()).thenReturn(loadedTestLl);
		
		Function<Object, Collection<?>> loader = CollectionProxyEntityDescriptor.getLoader(sbFactory, TestLl.class, "fk", "Test2");
		
		Collection<?> entities = loader.apply(PARENT_ID);
		
		assertSame(loadedTestLl, entities);
		assertEquals(TestLl.class, ((SimpleEntityDescriptor) (capture.getValue().getValue())).getEntityClass());
		assertTrue("Test2".equalsIgnoreCase(((SimpleEntityDescriptor) (capture.getValue().getValue())).getTableName()));
	}
	
	
	@Test
	public void testManyToManyNoRootTable() {
		when(sbFactory.newSqlBuilder(TestLl.class)).thenReturn(sb);
		when(sb.select()).thenReturn(sb);
		when(sb.where()).thenReturn(sb);
		when(sb.id()).thenReturn(sb);
		when(sb.in()).thenReturn(sb);
		when(sb.lp()).thenReturn(sb);
		when(sb.sql("select TypeId from link where VendorId = ?", PARENT_ID)).thenReturn(sb);
		when(sb.rp()).thenReturn(sb);
		when(sb.exec()).thenReturn(loadedTestLl);
		
		Function<Object, Collection<?>> loader = CollectionProxyEntityDescriptor.getLoader(sbFactory, TestLl.class, "VendorId", null, "TypeId", "link");
		
		Collection<?> entities = loader.apply(PARENT_ID);
		
		assertSame(loadedTestLl, entities);
	}

	@Test
	public void testManyToManyRootTableSame() {
		when(sbFactory.newSqlBuilder(TestLl.class)).thenReturn(sb);
		when(sb.select()).thenReturn(sb);
		when(sb.where()).thenReturn(sb);
		when(sb.id()).thenReturn(sb);
		when(sb.in()).thenReturn(sb);
		when(sb.lp()).thenReturn(sb);
		when(sb.sql("select TypeId from link where VendorId = ?", PARENT_ID)).thenReturn(sb);
		when(sb.rp()).thenReturn(sb);
		when(sb.exec()).thenReturn(loadedTestLl);
		
		Function<Object, Collection<?>> loader = CollectionProxyEntityDescriptor.getLoader(sbFactory, TestLl.class, "VendorId", "TEST", "TypeId", "link");
		
		Collection<?> entities = loader.apply(PARENT_ID);
		
		assertSame(loadedTestLl, entities);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testManyToManyRootTableOverride() {
		when(sbFactory.getEntityDescriptorTreeRepository()).thenReturn(treeRepo);
		// the return value does not matter
		when(treeRepo.getEntityDescriptorTree(capture.capture()))
			.thenReturn(new SimpleNode<EntityDescriptor>(
				new EntityDescriptor(TestLl.class, new IntRowMapper("id"), ReusableRowMappers.ROW_MAPPER_INTEGER, "test")));
		when(sbFactory.newSqlBuilder(TestLl.class)).thenReturn(sb);
		when(sb.select(any(Node.class))).thenReturn(sb);
		when(sb.where()).thenReturn(sb);
		when(sb.id()).thenReturn(sb);
		when(sb.in()).thenReturn(sb);
		when(sb.lp()).thenReturn(sb);
		when(sb.sql("select TypeId from link where VendorId = ?", PARENT_ID)).thenReturn(sb);
		when(sb.rp()).thenReturn(sb);
		when(sb.exec()).thenReturn(loadedTestLl);
		
		Function<Object, Collection<?>> loader = CollectionProxyEntityDescriptor.getLoader(sbFactory, TestLl.class, "VendorId", "Test2", "TypeId", "link");
		
		Collection<?> entities = loader.apply(PARENT_ID);
		
		assertSame(loadedTestLl, entities);
		assertEquals(TestLl.class, ((SimpleEntityDescriptor) (capture.getValue().getValue())).getEntityClass());
		assertTrue("Test2".equalsIgnoreCase(((SimpleEntityDescriptor) (capture.getValue().getValue())).getTableName()));
	}
	
	
	@Table("Test")
	private static class TestLl {
		@PkColumn("id")
		int id = PARENT_ID;
	}

}
