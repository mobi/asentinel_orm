package com.asentinel.common.jdbc;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

import com.asentinel.common.jdbc.flavors.DefaultPreparedStatementParametersSetter;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;

public class CustomPreparedStatementCreatorTestCase {
	
	private final JdbcFlavor jdbcFlavor = createMock(JdbcFlavor.class);
	private final LobHandler lh = createMock(LobHandler.class);
	
	// TODO: add more tests for blobs etc
	
	@Before
	public void setup() {
		expect(jdbcFlavor.getLobHandler()).andReturn(lh).anyTimes();
		expect(jdbcFlavor.getPreparedStatementParametersSetter()).andReturn(new DefaultPreparedStatementParametersSetter()).anyTimes();
		replay(jdbcFlavor);
	}
	
	@After
	public void tearDown() {
		verify(jdbcFlavor);
	}	
	
	@Test
	public void testNoKeys() throws SQLException {
		CustomPreparedStatementCreator psc = new CustomPreparedStatementCreator(jdbcFlavor, "sql", 1, 2);
		Connection conn = createMock(Connection.class);
		PreparedStatement ps = createMock(PreparedStatement.class);
		expect(ps.getConnection()).andReturn(conn).anyTimes();
		ps.setObject(1, 1);
		ps.setObject(2, 2);
		expect(conn.prepareStatement("sql")).andReturn(ps);
		
		replay(conn, ps);
		psc.createPreparedStatement(conn);
		verify(conn, ps);
	}

	@Test
	public void testKeys() throws SQLException {
		String[] ids = new String[]{"id"};
		CustomPreparedStatementCreator psc = new CustomPreparedStatementCreator(jdbcFlavor, "sql", ids, 1, 2);
		Connection conn = createMock(Connection.class);
		PreparedStatement ps = createMock(PreparedStatement.class);
		expect(ps.getConnection()).andReturn(conn).anyTimes();
		ps.setObject(1, 1);
		ps.setObject(2, 2);
		expect(conn.prepareStatement("sql", ids)).andReturn(ps);
		
		replay(conn, ps);
		psc.createPreparedStatement(conn);
		verify(conn, ps);
	}
	
	
	@Test
	public void testCleanupWithLobCreator() throws IOException {
		Closeable c = createMock(Closeable.class);
		c.close();
		
		LobCreator lc = createMock(LobCreator.class);
		lc.close();
		
		CustomPreparedStatementCreator psc = new CustomPreparedStatementCreator(jdbcFlavor, "sql", 1, c);
		psc.lobCreator = lc;
		
		replay(c, lc);
		psc.cleanupParameters();
		verify(c, lc);
	}
	
	@Test
	public void testCleanupWoLobCreator() throws IOException {
		Closeable c = createMock(Closeable.class);
		c.close();
		
		CustomPreparedStatementCreator psc = new CustomPreparedStatementCreator(jdbcFlavor, "sql", 1, c);
		
		replay(c);
		psc.cleanupParameters();
		verify(c);
	}
	
}
