package com.asentinel.common.jdbc.resource.adapters;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.flavors.TableIntrospector;
import com.asentinel.common.resource.ByteArrayTempResource;
import org.junit.Test;
import org.springframework.core.io.ResourceLoader;

import java.util.Map;

import static com.asentinel.common.jdbc.resource.adapters.TableResourceLoader.getLocationElements;
import static com.asentinel.common.jdbc.resource.adapters.TableResourceLoader.matches;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TableResourceLoaderTestCase {
	
	private final SqlQuery queryEx = mock(SqlQuery.class);
	private final TableIntrospector tableIntrospector = mock(TableIntrospector.class);
	private final ResourceLoader fallbackResourceLoader = mock(ResourceLoader.class);
	
	private final TableResourceLoader loader = new TableResourceLoader(queryEx, tableIntrospector, fallbackResourceLoader) {
		@Override
		protected Map<String, String> loadMap(String[] locationElements) {
			return Map.of("key.1", "value.1");
		}
	};


	@Test
	public void getLocationElements1() {
		String[] elems = getLocationElements("db://table/key,value");
		assertArrayEquals(new String[] {"table", "key", "value"}, elems);
	}
	
	@Test
	public void getLocationElements2() {
		String[] elems = getLocationElements("db://table/key,value_en.properties");
		assertArrayEquals(new String[] {"table", "key", "value_en"}, elems);
	}

	@Test
	public void getLocationElements3() {
		String[] elems = getLocationElements("DB://TABLE/KEY,VALUE");
		assertArrayEquals(new String[] {"TABLE", "KEY", "VALUE"}, elems);
	}

	
	@Test
	public void getLocationElements4() {
		String[] elems = getLocationElements("db:// table / key , value ");
		assertArrayEquals(new String[] {"table", "key", "value"}, elems);
	}

	@Test
	public void matches1() {
		assertTrue(matches("db://table/key,value"));
	}
	
	@Test
	public void matches2() {
		assertTrue(matches("db:// table/ key, value"));
	}

	@Test
	public void matches3() {
		assertTrue(matches("DB://TABLE/key,value"));
	}

	@Test
	public void matches4() {
		assertFalse(matches("file://test.txt"));
	}

	@Test
	public void matches5() {
		assertFalse(matches(""));
	}
	
	@Test
	public void fallback() {
		var l = "file://test.txt";
		when(fallbackResourceLoader.getResource(l)).thenReturn(new ByteArrayTempResource());
		var r = loader.getResource(l);
		assertTrue(r instanceof ByteArrayTempResource);
		verifyNoInteractions(queryEx, tableIntrospector);
	}
	
	@Test
	public void nonExisting() {
		when(tableIntrospector.supports("table", "value")).thenReturn(false);
		
		var l = "db://table/key,value";
		var r = loader.getResource(l);
		assertTrue(r instanceof TableResource);
		assertFalse(r.exists());
		assertEquals(l, r.getFilename());
		assertEquals(l, r.getDescription());
		verifyNoInteractions(queryEx, fallbackResourceLoader);
	}

	@Test
	public void existing() {
		when(tableIntrospector.supports("table", "value")).thenReturn(true);
		
		var l = "db://table/key,value";
		var r = loader.getResource(l);
		assertTrue(r instanceof TableResource);
		assertTrue(r.exists());
		assertEquals(l, r.getFilename());
		assertEquals(l, r.getDescription());
		assertEquals(Map.of("key.1", "value.1"), ((TableResource) r).getKeyValues());
		
		verifyNoInteractions(queryEx, fallbackResourceLoader);
	}

}
