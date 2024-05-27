package com.asentinel.common.jdbc.resource.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

import org.junit.Test;
import org.springframework.util.FileCopyUtils;

public class TableResourceTestCase {
	
	private final LinkedHashMap<String, String> map = new LinkedHashMap<>();
	{
		map.put("key.1", "value.1");
		map.put("key.2", "value.2");
	}

	@Test
	public void notExisting2() {
		assertFalse(new TableResource("test", null).exists());
	}
	
	@Test
	public void resultingText() throws IOException {
		var tr = new TableResource("test", map);
		assertEquals("test", tr.getDescription());
		assertEquals("test", tr.getFilename());
		assertTrue(tr.exists());
		
		var in = tr.getInputStream();
		var bytes = FileCopyUtils.copyToByteArray(in);
		var text = new String(bytes, StandardCharsets.UTF_8);
		assertEquals("key.1=value.1\nkey.2=value.2\n", text);
	}

	@Test
	public void nullValue() throws IOException {
		map.put("key.1", null);
		var tr = new TableResource("test", map);
		assertEquals("test", tr.getDescription());
		assertEquals("test", tr.getFilename());
		assertTrue(tr.exists());
		
		var in = tr.getInputStream();
		var bytes = FileCopyUtils.copyToByteArray(in);
		var text = new String(bytes, StandardCharsets.UTF_8);
		assertEquals("key.2=value.2\n", text);
	}

	
	@Test
	public void emptyValue() throws IOException {
		map.put("key.1", "");
		var tr = new TableResource("test", map);
		assertEquals("test", tr.getDescription());
		assertEquals("test", tr.getFilename());
		assertTrue(tr.exists());
		
		var in = tr.getInputStream();
		var bytes = FileCopyUtils.copyToByteArray(in);
		var text = new String(bytes, StandardCharsets.UTF_8);
		assertEquals("key.2=value.2\n", text);
	}

	@Test
	public void emptyKey() throws IOException {
		map.remove("key.1");
		map.put("", "value.1");
		var tr = new TableResource("test", map);
		assertEquals("test", tr.getDescription());
		assertEquals("test", tr.getFilename());
		assertTrue(tr.exists());
		
		var in = tr.getInputStream();
		var bytes = FileCopyUtils.copyToByteArray(in);
		var text = new String(bytes, StandardCharsets.UTF_8);
		assertEquals("key.2=value.2\n", text);
	}

}
