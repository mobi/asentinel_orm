package com.asentinel.common.orm.integration.tests;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.SortedSet;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.orm.AutoEagerLoader;
import com.asentinel.common.orm.EntityBuilder;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.FetchType;
import com.asentinel.common.orm.RelationType;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.orm.collections.OrmTreeSet;
import com.asentinel.common.orm.ed.tree.DefaultEntityDescriptorTreeRepository;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

/**
 * This test was used to replicate a bug, it is now part of the standard set of
 * tests.
 * 
 * @since 1.60.14
 * @author Razvan Popian
 *
 */
public class SelfReferencingEntityWithCollectionTestCase {
	private final static Logger log = LoggerFactory.getLogger(SelfReferencingEntityWithCollectionTestCase.class);
	
	private final SqlBuilderFactory sbFactory = mock(SqlBuilderFactory.class);
	private final DefaultEntityDescriptorTreeRepository treeRepo = new DefaultEntityDescriptorTreeRepository();
	{
		treeRepo.setSqlBuilderFactory(sbFactory);
	}
	
	private final ResultSet rs = mock(ResultSet.class);
	
	
	@Test
	public void test() throws SQLException {
		Node<EntityDescriptor> rootNode = treeRepo.getEntityDescriptorTree(A.class, AutoEagerLoader.forAllRootChildren());
		SimpleEntityDescriptor root = (SimpleEntityDescriptor) rootNode.getValue();
		SimpleEntityDescriptor c = (SimpleEntityDescriptor) rootNode.getChildren().get(0).getValue();
		assertSame(C.class, c.getEntityClass());
		SimpleEntityDescriptor a = (SimpleEntityDescriptor) rootNode.getChildren().get(1).getValue();
		assertSame(A.class, a.getEntityClass());
		
		when(rs.getInt(root.getTableAlias() + root.getColumnAliasSeparator() + "IDA")).thenReturn(1, 1, 2, 2);
		when(rs.getInt(c.getTableAlias() + c.getColumnAliasSeparator() + "IDC")).thenReturn(10, 20, 30, 40);
		when(rs.getInt(a.getTableAlias() + a.getColumnAliasSeparator() + "IDA")).thenReturn(2, 2, 1, 1);
		when(rs.getInt(a.getTableAlias() + a.getColumnAliasSeparator() + "FKA")).thenReturn(1, 1, 2, 2);
		
		EntityBuilder<A> eb = new EntityBuilder<>(rootNode);
		
		// there are 4 rows in the resultset so we call process 4 times
		eb.processRow(rs);
		eb.processRow(rs);
		eb.processRow(rs);
		eb.processRow(rs);
		
		assertEquals(2, eb.getEntityList().size());
		A a1 = eb.getEntityList().get(0);
		A a2 = eb.getEntityList().get(1);
		
		assertEquals(1, a1.id);
		assertEquals(2, a1.b.id);
		assertTrue(a1.set instanceof OrmTreeSet);
		assertEquals(Set.of(10, 20), a1.set.stream().map(x -> x.id).collect(toSet()));
		
		assertEquals(2, a2.id);
		assertEquals(1, a2.b.id);
		assertTrue(EntityUtils.isProxy(a2.set));
		assertFalse(EntityUtils.isLoadedProxy(a2.set));
		
		// verify no attempt is made to load any proxy
		verify(sbFactory, times(5)).getSqlQuery();
		verifyNoMoreInteractions(sbFactory);
		
		eb.getEntityList().forEach(e -> log.debug("test - {}", e));
	}

	
	@Table("A")
	private static class A {
		
		@PkColumn("IDA")
		int id;
		
		@Child(fkName = "FKC", parentRelationType = RelationType.MANY_TO_ONE, fetchType = FetchType.LAZY)
		SortedSet<C> set;
		
		@Child(fkName = "FKA", fetchType = FetchType.LAZY)
		A b;

		@Override
		public String toString() {
			return "A [id=" + id 
						+ ", b=" + (b != null ? String.valueOf(b.id) : null)
						+ ", set=" + set  
						+ "]";
		}
		
		
	}
	
	@Table("C")
	private static class C implements Comparable<C> {
		
		@PkColumn("IDC")
		int id;

		@Override
		public String toString() {
			return String.valueOf(id);
		}

		@Override
		public int compareTo(C o) {
			return Integer.compare(id, o.id);
		}

	}
}
