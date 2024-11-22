package com.asentinel.common.orm;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.jdbc.ReusableRowMappers;
import com.asentinel.common.orm.ed.tree.EntityDescriptorTreeRepository;
import com.asentinel.common.orm.jql.SqlBuilder;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.mappers.IntRowMapper;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProxyEntityDescriptorStaticMethodsTestCase {
	
	static final int ID = 17;
	
	SqlBuilderFactory sbFactory = mock(SqlBuilderFactory.class);
	
	EntityDescriptorTreeRepository treeRepo = mock(EntityDescriptorTreeRepository.class);
	
	@SuppressWarnings("unchecked")
	SqlBuilder<TestLl> sb = mock(SqlBuilder.class);
	
	@SuppressWarnings("rawtypes")
	ArgumentCaptor<Node> capture = ArgumentCaptor.forClass(Node.class);

	TestLl loadedTestLl = new TestLl();

	@Test
	public void testNoRootTable(){
		when(sbFactory.newSqlBuilder(TestLl.class)).thenReturn(sb);
		when(sb.select()).thenReturn(sb);
		when(sb.where()).thenReturn(sb);
		when(sb.id()).thenReturn(sb);
		when(sb.eq(ID)).thenReturn(sb);
		when(sb.execForEntity()).thenReturn(loadedTestLl);
		
		Function<Object, TestLl> loader = ProxyEntityDescriptor.getLoader(sbFactory, TestLl.class, null);
		
		Object entity = loader.apply(ID);
		
		assertSame(loadedTestLl, entity);
	}

	
	@Test
	public void testRootTableSame(){
		when(sbFactory.newSqlBuilder(TestLl.class)).thenReturn(sb);
		when(sb.select()).thenReturn(sb);
		when(sb.where()).thenReturn(sb);
		when(sb.id()).thenReturn(sb);
		when(sb.eq(ID)).thenReturn(sb);
		when(sb.execForEntity()).thenReturn(loadedTestLl);
		
		Function<Object, TestLl> loader = ProxyEntityDescriptor.getLoader(sbFactory, TestLl.class, "Test");
		
		Object entity = loader.apply(ID);
		
		assertSame(loadedTestLl, entity);
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
		when(sb.id()).thenReturn(sb);
		when(sb.eq(ID)).thenReturn(sb);
		when(sb.execForEntity()).thenReturn(loadedTestLl);
		
		Function<Object, TestLl> loader = ProxyEntityDescriptor.getLoader(sbFactory, TestLl.class, "Test2");
		
		Object entity = loader.apply(ID);
		
		assertSame(loadedTestLl, entity);
		assertEquals(TestLl.class, ((SimpleEntityDescriptor) (capture.getValue().getValue())).getEntityClass());
		assertTrue("Test2".equalsIgnoreCase(((SimpleEntityDescriptor) (capture.getValue().getValue())).getTableName()));
	}


	@Table("Test")
	private static class TestLl {
		@PkColumn("id")
		int id = ID;
	}

}
