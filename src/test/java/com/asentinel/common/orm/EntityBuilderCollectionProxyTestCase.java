package com.asentinel.common.orm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.function.Function;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.orm.EntityBuilderDefaultCollectionInitTestCase.CollectionRootCollection;
import com.asentinel.common.orm.EntityBuilderDefaultCollectionInitTestCase.CollectionRootHashSet;
import com.asentinel.common.orm.EntityBuilderDefaultCollectionInitTestCase.CollectionRootList;
import com.asentinel.common.orm.EntityBuilderDefaultCollectionInitTestCase.CollectionRootMap;
import com.asentinel.common.orm.EntityBuilderDefaultCollectionInitTestCase.CollectionRootSet;
import com.asentinel.common.orm.EntityBuilderDefaultCollectionInitTestCase.CollectionRootSortedMap;
import com.asentinel.common.orm.EntityBuilderDefaultCollectionInitTestCase.CollectionRootSortedSet;
import com.asentinel.common.orm.EntityBuilderDefaultCollectionInitTestCase.Kid;

public class EntityBuilderCollectionProxyTestCase {
	
	ResultSet rs = mock(ResultSet.class);
	
	@SuppressWarnings({ "rawtypes" })
	Function loader = mock(Function.class);
	
	List<Kid> kids = Arrays.asList(new Kid(1), new Kid(2));
	
	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws SQLException {
		// we need at least 2 rows because we need to make sure 
		// we do not proxy the collection multiple times
		when(rs.getInt("t_ParentId")).thenReturn(10, 10);
		when(loader.apply(10)).thenReturn(kids);
	}
	
	@SuppressWarnings("unchecked")
	@After
	public void after() {
		verify(loader, times(1)).apply(10);
	}
	
	@Test
	public void testCollectionProxyRootCollection() throws SQLException {
		testClass(CollectionRootCollection.class, ArrayList.class);
	}

	@Test
	public void testCollectionProxyRootList() throws SQLException {
		testClass(CollectionRootList.class, ArrayList.class);
	}

	@Test
	public void testCollectionProxyRootSet() throws SQLException {
		testClass(CollectionRootSet.class, LinkedHashSet.class);
	}
	
	@Test
	public void testCollectionProxyRootHashSet() throws SQLException {
		testClass(CollectionRootHashSet.class, HashSet.class);
	}

	@Test
	public void testCollectionProxyRootMap() throws SQLException {
		testClass(CollectionRootMap.class, Map.class);
	}
	
	@Test
	public void testCollectionProxyRootSortedSet() throws SQLException {
		testClass(CollectionRootSortedSet.class, SortedSet.class);
	}
	
	@Test
	public void testCollectionProxyRootSortedMap() throws SQLException {
		testClass(CollectionRootSortedMap.class, SortedMap.class);
	}
	
	
	private Node<EntityDescriptor> createTreeForClass(Class<?> clasz) {
		SimpleEntityDescriptor rootEd = new SimpleEntityDescriptor.Builder(clasz).tableAlias("t").build();
		@SuppressWarnings("unchecked")
		CollectionProxyEntityDescriptor kidEd = new CollectionProxyEntityDescriptor(loader, 
				Kid.class, ReflectionUtils.findField(clasz, "kids"), "KidId");
		Node<EntityDescriptor> root = new SimpleNode<>(rootEd);
		root.addChild(new SimpleNode<EntityDescriptor>(kidEd));
		return root;
	}
	
	
	private void testClass(Class<?> clasz, Class<?> expectedCollectionType) throws SQLException {
		Node<EntityDescriptor> root = createTreeForClass(clasz);
		
		EntityBuilder<CollectionRootCollection> eb = new EntityBuilder<>(root);
		eb.processRow(rs);
		eb.processRow(rs);
		Object rootInstance = eb.getEntity();
		Object kidsInstance = ReflectionTestUtils.getField(rootInstance, "kids");
		
		assertTrue(expectedCollectionType.isAssignableFrom(kidsInstance.getClass()));
		
		// test that the loading happens
		assertEquals(kids.size(), (int) ReflectionTestUtils.invokeMethod(kidsInstance, "size"));
	}
	
}
