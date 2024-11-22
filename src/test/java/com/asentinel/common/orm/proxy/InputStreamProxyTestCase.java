package com.asentinel.common.orm.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;
import org.springframework.jdbc.support.lob.LobHandler;

import com.asentinel.common.jdbc.InputStreamRowMapper;
import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

/**
 * Tests the functionality of the {@link InputStreamProxy}.
 * 
 * @author Razvan Popian
 */
public class InputStreamProxyTestCase {
	
	private final SqlQuery qEx = mock(SqlQuery.class);
	private final LobHandler lh = mock(LobHandler.class);
	
	@Test
	public void standardOperation() throws IOException {
		StreamWrapper sw = new StreamWrapper();
		byte[] bytes = {1, 2, 3};
		
		when(qEx.queryForObject(and(contains("bytes"), and(contains("SW"), contains("id"))), 
				any(InputStreamRowMapper.class), 
				eq(sw.id)))
			.thenReturn(new ByteArrayInputStream(bytes));
		
		@SuppressWarnings("resource")
		InputStreamProxy p = new InputStreamProxy(qEx, lh, sw, "bytes");
		
		assertFalse(p.isLoaded());
		
		assertEquals(bytes[0], p.read());
		
		assertTrue(p.isLoaded());
		
		assertEquals(bytes[1], p.read());
		assertEquals(bytes[2], p.read());
		assertEquals(-1, p.read());
	}
	
	@SuppressWarnings("resource")
	@Test(expected = IllegalStateException.class)
	public void failBecauseNotEntity() {
		new InputStreamProxy(qEx, lh, "", "bytes");
	}
	
	
	@Table("SW")
	private static class StreamWrapper {
		@PkColumn("id")
		int id = 10;
	}
}
