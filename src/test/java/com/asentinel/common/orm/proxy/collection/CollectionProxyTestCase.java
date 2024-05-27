package com.asentinel.common.orm.proxy.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.proxy.ProxyFactorySupport;

public class CollectionProxyTestCase {
	
	final static String LOADER_FIELD_NAME = (String) ReflectionTestUtils.getField(ProxyFactorySupport.class, "LOADER_FIELD_NAME");
	
	CollectionProxyFactory factory = CollectionProxyFactory.getInstance();
	
	@SuppressWarnings({ "rawtypes" })
	Function loader = mock(Function.class);
	
	@SuppressWarnings("unchecked")
	ArrayList<Integer> createProxy() {
		return factory.newProxy(ArrayList.class, loader, 10);
	}
	
	static boolean isLoaded(Object proxy) {
		return ReflectionTestUtils.getField(proxy, LOADER_FIELD_NAME) == null
				&& ReflectionTestUtils.getField(proxy, CollectionProxyFactory.PARENT_ID_FIELD_NAME) == null;
	}

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		when(loader.apply(10)).thenReturn(Arrays.asList(1, 2));
	}

	@Test
	public void toString_Does_Not_Trigger_Load() {
		Object proxy = createProxy();
		assertFalse(EntityUtils.isLoadedProxy(proxy));
		
		// check toString
		String s = proxy.toString();
		verifyNoMoreInteractions(loader);
		assertEquals(String.format(CollectionToStringInterceptor.PROXY, proxy.getClass().getSuperclass().getName()), s);
	}

	@Test
	public void toString_Renders_Ok_After_Load() {
		List<Integer> proxy = createProxy();
		assertFalse(EntityUtils.isLoadedProxy(proxy));
		
		// trigger load
		proxy.add(3);
		
		// check toString
		String s = proxy.toString();
		assertTrue(isLoaded(proxy));
		assertTrue(EntityUtils.isLoadedProxy(proxy));
		assertNotEquals(String.format(CollectionToStringInterceptor.PROXY, proxy.getClass().getSuperclass().getName()), s);
		
		// check list content
		assertEquals(Arrays.asList(1, 2, 3), proxy);
	}
	
	@Test
	public void arbitrary_Method_Triggers_Load() {
		List<Integer> proxy = createProxy();
		assertFalse(EntityUtils.isLoadedProxy(proxy));
		
		assertEquals(2, proxy.size());
		assertTrue(isLoaded(proxy));
		assertTrue(EntityUtils.isLoadedProxy(proxy));
		
		// check list content
		assertEquals(Arrays.asList(1, 2), proxy);
	}
	
	@Test
	public void super_Method_Triggers_Load() {
		List<Integer> proxy = createProxy();
		assertFalse(EntityUtils.isLoadedProxy(proxy));
		
		proxy.hashCode();
		assertTrue(isLoaded(proxy));
		assertTrue(EntityUtils.isLoadedProxy(proxy));
		
		// check list content
		assertEquals(Arrays.asList(1, 2), proxy);
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void lazy_Load_Happens_Exactly_Once() {
		List<Integer> proxy = createProxy();
		assertFalse(EntityUtils.isLoadedProxy(proxy));
		
		// first invoke triggers the load
		assertEquals(1, proxy.indexOf(2));
		verify(loader).apply(10);
		assertTrue(isLoaded(proxy));
		assertTrue(EntityUtils.isLoadedProxy(proxy));
		assertEquals(Arrays.asList(1, 2), proxy);

		// second invoke should not trigger another load
		assertEquals(1, proxy.indexOf(2));
		verifyNoMoreInteractions(loader);
		assertTrue(isLoaded(proxy));
	}
	
	
	
}
