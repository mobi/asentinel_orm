package com.asentinel.common.jdbc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.jdbc.support.lob.LobHandler;

import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;
import com.asentinel.common.orm.proxy.InputStreamProxy;

/**
 * @author Razvan Popian
 */
public class ConversionSupportTestCase {

	private final LobHandler lh = mock(LobHandler.class);
	private final ConversionSupport cs = new ConversionSupport() {
		{
			setLobHandler(lh);
		}
	};
	
	private final ResultSet rs = mock(ResultSet.class);
	private final SqlQuery qEx = mock(SqlQuery.class);
	
	private final ByteArrayInputStream bytes = new ByteArrayInputStream(new byte[] {1, 2, 3});

	@Test
	public void inputStreamEager() throws SQLException {
		when(lh.getBlobAsBinaryStream(rs, "bytes")).thenReturn(bytes);
		InputStream in = (InputStream) cs.getValueInternal(1, TypeDescriptor.valueOf(InputStream.class), rs, new ColumnMetadata("bytes"));
		assertSame(bytes, in);
	}
	
	@Test
	public void inputStreamLazy() throws SQLException {
		cs.setQueryExecutor(qEx);
		InputStream in = (InputStream) cs.getValueInternal(new InputStreamWrapper(), TypeDescriptor.valueOf(InputStream.class), rs, new ColumnMetadata("bytes"));
		assertTrue(in instanceof InputStreamProxy);
		assertFalse(((InputStreamProxy) in).isLoaded());
	}
	
	@Table("test")
	private static class InputStreamWrapper {
		@PkColumn("id")
		int id;
		
	}
}
