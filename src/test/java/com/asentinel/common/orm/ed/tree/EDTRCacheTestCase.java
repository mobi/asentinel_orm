package com.asentinel.common.orm.ed.tree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.support.lob.LobHandler;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.EntityDescriptorNodeCallback;
import com.asentinel.common.orm.Invoice;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.mappers.AnnotationRowMapper;

public class EDTRCacheTestCase {
	
	private final LobHandler lh = mock(LobHandler.class);
	private final SqlQuery qEx = mock(SqlQuery.class);
	private final ConversionService cs = mock(ConversionService.class);
	private final SqlBuilderFactory sbf = mock(SqlBuilderFactory.class);
	private final DefaultEntityDescriptorTreeRepository edtrRepo = new DefaultEntityDescriptorTreeRepository(lh);
	private final EntityDescriptorNodeCallback nc = (n, b) -> false;
	
	{
		edtrRepo.setConversionService(cs);
		edtrRepo.setSqlBuilderFactory(sbf);
		when(sbf.getSqlQuery()).thenReturn(qEx);
	}

	@Test
	public void testCached() {
		Node<EntityDescriptor> t1 = edtrRepo.getEntityDescriptorTree(Invoice.class);
		Node<EntityDescriptor> t2 = edtrRepo.getEntityDescriptorTree(Invoice.class);
		assertNotSame("No defensive copy made.", t1, t2);
		assertTrue(equalTrees(t1, t2));
	}
	
	@Test
	public void testCachedWithAlias() {
		Node<EntityDescriptor> t1 = edtrRepo.getEntityDescriptorTree(Invoice.class, "a");
		Node<EntityDescriptor> t2 = edtrRepo.getEntityDescriptorTree(Invoice.class, "a");
		assertNotSame("No defensive copy made.", t1, t2);
		assertTrue(equalTrees(t1, t2));
	}
	

	@Test
	public void testNotCached() {
		Node<EntityDescriptor> t1 = edtrRepo.getEntityDescriptorTree(Invoice.class, nc);
		Node<EntityDescriptor> t2 = edtrRepo.getEntityDescriptorTree(Invoice.class, nc);
		assertNotSame("Not a new tree.", t1, t2);
		assertFalse(equalTrees(t1, t2));
	}
	
	@Test
	public void testNotCachedWithAlias() {
		Node<EntityDescriptor> t1 = edtrRepo.getEntityDescriptorTree(Invoice.class, "a", nc);
		Node<EntityDescriptor> t2 = edtrRepo.getEntityDescriptorTree(Invoice.class, "a", nc);
		assertNotSame("Not a new tree.", t1, t2);
		assertFalse(equalTrees(t1, t2));
	}
	
	// not related to caching
	@Test
	public void testServicesSetInRowMapperForClass() {
		Node<EntityDescriptor> root = edtrRepo.getEntityDescriptorTree(Invoice.class);
		root.traverse(n -> {
				AnnotationRowMapper<?> rm = ((AnnotationRowMapper<?>) n.getValue().getEntityRowMapper());
				assertSame(cs, rm.getConversionService());
				assertSame(qEx, rm.getQueryExecutor());
				assertSame(lh, rm.getLobHandler());
		});
	}

	// not related to caching
	@Test
	public void testServicesSetInRowMapperForDescriptor() {
		Node<EntityDescriptor> root = edtrRepo.getEntityDescriptorTree(
				new SimpleNode<>(new SimpleEntityDescriptor(Invoice.class)));
		root.traverse(n -> {
				AnnotationRowMapper<?> rm = ((AnnotationRowMapper<?>) n.getValue().getEntityRowMapper());
				assertSame(cs, rm.getConversionService());
				assertSame(qEx, rm.getQueryExecutor());
				assertSame(lh, rm.getLobHandler());
		});
	}

	
	// should not be considered a valid production tree equality function,
	// it's just good enough for this test
	private boolean equalTrees(Node<EntityDescriptor> t1, Node<EntityDescriptor> t2) {
		List<EntityDescriptor> l1 = new ArrayList<>();
		List<EntityDescriptor> l2  = new ArrayList<>();
		
		t1.traverse(n -> l1.add(n.getValue()));
		
		t2.traverse(n -> l2.add(n.getValue()));
		
		return l1.equals(l2);
	}
}
