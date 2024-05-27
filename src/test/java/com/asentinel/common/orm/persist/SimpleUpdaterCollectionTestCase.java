package com.asentinel.common.orm.persist;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.test.util.ReflectionTestUtils;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

/**
 * Tests the collections insert/update operations. Ensures the following:
 * 
 * <li> The {@link SimpleUpdater#update(java.util.Collection)} method is properly creating
 * insert statements for new entities and update statements for existing entities. 
 * <li> Blobs are properly handled and their associated resources are cleanedup.
 * 
 * @see SimpleUpdaterTestCase
 *  
 * @author Razvan Popian
 */
public class SimpleUpdaterCollectionTestCase {
	
	// we use this constant because the sequence next val instruction varies depending on the selected
	// JdbcFlavor
	final static String SEQ_NEXT_VAL_PLACEHOLDER = "[SEQ]";
	
	final static int GENERATED_ID = 10;
	final static String INSERT_AUTO_ID = "insert into table(tableId, name, BlobFieldInputStream, BlobFieldByteArray, BooleanField, insertable) values(" + SEQ_NEXT_VAL_PLACEHOLDER + ", ?, ?, ?, ?, ?)";
	final static String INSERT_AUTO_ID_NO_SEQ = "insert into table(name, BlobFieldInputStream, BlobFieldByteArray, BooleanField, insertable) values(?, ?, ?, ?, ?)";
	final static String UPDATE = "update table set name = ?, BlobFieldInputStream = ?, BlobFieldByteArray = ?, BooleanField = ?, updatable = ? where tableId = ?";
	
	boolean closeCalled = false;
	final InputStream in = new ByteArrayInputStream(new byte[] {1,2,3}) {
		@Override
		public void close() throws IOException {
			super.close();
			closeCalled = true;
		}
	};
	final byte[] bArray = new byte[] {10,20,30};
	
	JdbcFlavor jdbcFlavor = new PostgresJdbcFlavor(); // should be mocked, for now since PG is our target we are ok with it

	JdbcOperations jdbcOps = createMock(JdbcOperations.class);
	SqlQuery ex = createMock(SqlQuery.class);
	Updater u = new SimpleUpdater(jdbcFlavor, ex);
	
	Capture<PreparedStatementCreator> captureInsertCreator = Capture.newInstance();
	Capture<PreparedStatementCallback<int[]>> captureInsertAction = Capture.newInstance();
	
	Capture<String> captureSqlUpdate = Capture.newInstance();
	Capture<BatchPreparedStatementSetter> captureBatchSetter = Capture.newInstance();
	
	Connection conn0 = createMock(Connection.class);
	PreparedStatement ps0 = createMock(PreparedStatement.class);
	ResultSet rs = createMock(ResultSet.class);
	ResultSetMetaData rsmd = createMock(ResultSetMetaData.class);
	
	Connection conn1 = createMock(Connection.class);
	PreparedStatement ps1 = createMock(PreparedStatement.class);
	
	Capture<String[]> capturePkName = Capture.newInstance();
	
	final ByteArrayOutputStream baos0 = new ByteArrayOutputStream();

	final ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
	
	@Before
	public void setup() throws SQLException {
		expect(ex.getJdbcOperations()).andReturn(jdbcOps).anyTimes();
		
		expect(rs.getMetaData()).andReturn(rsmd);
		expect(rsmd.getColumnCount()).andReturn(1);
		expect(rsmd.getColumnLabel(1)).andReturn("tableId");	
		
		expect(rs.next()).andReturn(true);
		expect(rs.getObject(1)).andReturn(new BigDecimal(GENERATED_ID));
		expect(rs.next()).andReturn(false);
		rs.close();
		
	}
	
	@Test
	public void testAuto() throws SQLException {
		List<ComplexEntity> list = 
			Arrays.asList(
				new ComplexEntity(0, "insert_entity", in, bArray, true, 10, 100),
				new ComplexEntity(11, "update_entity", in, bArray, false, 20, 200)
			);
		
		expect(jdbcOps.execute(capture(captureInsertCreator), capture(captureInsertAction)))
			.andReturn(new int[]{Statement.SUCCESS_NO_INFO});
		
		expect(jdbcOps.batchUpdate(capture(captureSqlUpdate), capture(captureBatchSetter)))
				.andReturn(new int[]{Statement.SUCCESS_NO_INFO});
		
		
		replay(jdbcOps, ex);
		
		u.update(list);
		
		verify(jdbcOps, ex);
		
		PreparedStatementCreator psc = captureInsertCreator.getValue();
		String sqlInsert = (String) ReflectionTestUtils.getField(psc, "sql");
		assertEquals(INSERT_AUTO_ID.replaceAll("\\s", "").toLowerCase(), 
				sqlInsert
				.replaceAll("\\s", "")
				.replace(jdbcFlavor.getSqlTemplates().getSqlForNextSequenceVal("ComplexEntity_seq"), SEQ_NEXT_VAL_PLACEHOLDER)
				.toLowerCase()
		);
		
		PreparedStatementCallback<int[]> action = captureInsertAction.getValue();
		assertNotNull(action);
		
		String sqlUpdate = captureSqlUpdate.getValue();
		assertEquals(UPDATE.replaceAll("\\s", "").toLowerCase(), 
				sqlUpdate.replaceAll("\\s", "").toLowerCase()
		);
		
		BatchPreparedStatementSetter batchSetter = captureBatchSetter.getValue();
		
		// validate insert callbacks
		expect(conn0.prepareStatement(anyObject(String.class), capture(capturePkName))).andReturn(ps0);
		ps0.setString(1, "insert_entity");
		ps0.setBinaryStream(2, in);
		ps0.setBytes(3, bArray);
		ps0.setString(4, "Y");
		ps0.setObject(5, 10);
		
		ps0.addBatch();
		
		expect(ps0.executeBatch()).andReturn(new int[]{Statement.SUCCESS_NO_INFO});
		expect(ps0.getGeneratedKeys()).andReturn(rs);
		
		// simulate the lifecycle of the PreparedStatementCreator and PreparedStatementCallback
		replay(ps0, conn0, rs, rsmd);
		// PreparedStatementCreator
		psc.createPreparedStatement(conn0);
		assertNotNull(ReflectionTestUtils.getField(psc, "lobCreator"));
		
		// PreparedStatementCallback
		action.doInPreparedStatement(ps0);
		
		// PreparedStatementCreator again
		closeCalled = false;
		ReflectionTestUtils.invokeMethod(psc, "cleanupParameters");
		verify(ps0, conn0, rs, rsmd);
		
		if (jdbcFlavor instanceof PostgresJdbcFlavor) {
			assertEquals("tableid", capturePkName.getValue()[0]);
		} else {
			assertEquals("tableId", capturePkName.getValue()[0]);
		}
		assertTrue(closeCalled);
		assertNull(ReflectionTestUtils.getField(psc, "lobCreator"));
		assertEquals(ReflectionTestUtils.getField(psc, "allArgs"), Arrays.asList("insert_entity", in, bArray, "Y", 10));
		assertEquals(GENERATED_ID, list.get(0).id);

		
		
		// validate update callback
		ps1.setString(1, "update_entity");
		ps1.setBinaryStream(2, in);
		ps1.setBytes(3, bArray);
		ps1.setString(4, "N");
		ps1.setObject(5, 200);
		ps1.setObject(6, list.get(1).id);

		// simulate the lifecycle of the BatchPreparedStatementSetter
		replay(ps1, conn1);
		batchSetter.setValues(ps1, 0);
		
		assertNotNull(ReflectionTestUtils.getField(batchSetter, "lobCreator"));
		
		closeCalled = false;
		ReflectionTestUtils.invokeMethod(batchSetter, "cleanupParameters");
		verify(ps1, conn1);

		assertTrue(closeCalled);
		assertNull(ReflectionTestUtils.getField(batchSetter, "lobCreator"));
		assertEquals(ReflectionTestUtils.getField(batchSetter, "allArgs"), Arrays.asList("update_entity", in, bArray, "N", 200, list.get(1).id));
		
	}
	
	
	@Test
	public void testAuto_NoSeq() throws SQLException {
		List<ComplexEntityNoSeq> list =
			Arrays.asList(
				new ComplexEntityNoSeq(0, "insert_entity", in, bArray, true, 10, 100),
				new ComplexEntityNoSeq(11, "update_entity", in, bArray, false, 20, 200)
		);
		
		expect(jdbcOps.execute(capture(captureInsertCreator), capture(captureInsertAction)))
			.andReturn(new int[]{Statement.SUCCESS_NO_INFO});
		
		expect(jdbcOps.batchUpdate(capture(captureSqlUpdate), capture(captureBatchSetter)))
				.andReturn(new int[]{Statement.SUCCESS_NO_INFO});
		
		
		replay(jdbcOps, ex);
		
		u.update(list);
		
		verify(jdbcOps, ex);
		
		PreparedStatementCreator psc = captureInsertCreator.getValue();
		String sqlInsert = (String) ReflectionTestUtils.getField(psc, "sql");
		assertEquals(INSERT_AUTO_ID_NO_SEQ.replaceAll("\\s", "").toLowerCase(), 
				sqlInsert
				.replaceAll("\\s", "")
				.replace(jdbcFlavor.getSqlTemplates().getSqlForNextSequenceVal("ComplexEntity_seq"), SEQ_NEXT_VAL_PLACEHOLDER)
				.toLowerCase()
		);
		
		PreparedStatementCallback<int[]> action = captureInsertAction.getValue();
		assertNotNull(action);
		
		String sqlUpdate = captureSqlUpdate.getValue();
		assertEquals(UPDATE.replaceAll("\\s", "").toLowerCase(), 
				sqlUpdate.replaceAll("\\s", "").toLowerCase()
		);
		
		BatchPreparedStatementSetter batchSetter = captureBatchSetter.getValue();
		
		// validate insert callbacks
		expect(conn0.prepareStatement(anyObject(String.class), capture(capturePkName))).andReturn(ps0);
		ps0.setString(1, "insert_entity");
		ps0.setBinaryStream(2, in);
		ps0.setBytes(3, bArray);
		ps0.setString(4, "Y");
		ps0.setObject(5,  10);
		
		ps0.addBatch();
		
		expect(ps0.executeBatch()).andReturn(new int[]{Statement.SUCCESS_NO_INFO});
		expect(ps0.getGeneratedKeys()).andReturn(rs);
		
		// simulate the lifecycle of the PreparedStatementCreator and PreparedStatementCallback
		replay(ps0, conn0, rs, rsmd);
		// PreparedStatementCreator
		psc.createPreparedStatement(conn0);
		assertNotNull(ReflectionTestUtils.getField(psc, "lobCreator"));
		
		// PreparedStatementCallback
		action.doInPreparedStatement(ps0);
		
		// PreparedStatementCreator again
		closeCalled = false;
		ReflectionTestUtils.invokeMethod(psc, "cleanupParameters");
		verify(ps0, conn0, rs, rsmd);
		
		if (jdbcFlavor instanceof PostgresJdbcFlavor) {
			assertEquals("tableid", capturePkName.getValue()[0]);
		} else {
			assertEquals("tableId", capturePkName.getValue()[0]);
		}
		assertTrue(closeCalled);
		assertNull(ReflectionTestUtils.getField(psc, "lobCreator"));
		assertEquals(ReflectionTestUtils.getField(psc, "allArgs"), Arrays.asList("insert_entity", in, bArray, "Y", 10));
		assertEquals(GENERATED_ID, list.get(0).id);

		
		
		// validate update callback
		ps1.setString(1, "update_entity");
		ps1.setBinaryStream(2, in);
		ps1.setBytes(3, bArray);
		ps1.setString(4, "N");
		ps1.setObject(5, 200);
		ps1.setObject(6, list.get(1).id);

		// simulate the lifecycle of the BatchPreparedStatementSetter
		replay(ps1, conn1);
		batchSetter.setValues(ps1, 0);
		
		assertNotNull(ReflectionTestUtils.getField(batchSetter, "lobCreator"));
		
		closeCalled = false;
		ReflectionTestUtils.invokeMethod(batchSetter, "cleanupParameters");
		verify(ps1, conn1);

		assertTrue(closeCalled);
		assertNull(ReflectionTestUtils.getField(batchSetter, "lobCreator"));
		assertEquals(ReflectionTestUtils.getField(batchSetter, "allArgs"), Arrays.asList("update_entity", in, bArray, "N", 200, list.get(1).id));
	}
	

	
	@Table("table")
	static class ComplexEntity {
		@PkColumn(value = "tableId", sequence = "ComplexEntity_seq")
		int id;
		
		@Column("name")
		String name;
		
		@Column("BlobFieldInputStream")
		InputStream in;

		@Column("BlobFieldByteArray")
		byte[] bArray;
		
		@Column("BooleanField")
		boolean boolField = true;
		
		@Column(value = "insertable", updatable = false)
		int insertable;
		
		@Column(value = "updatable", insertable = false)
		int updatable;

		public ComplexEntity(int id, String name, InputStream in,
				byte[] bArray, boolean boolField,
				int insertable, int updatable) {
			this.id = id;
			this.name = name;
			this.in = in;
			this.bArray = bArray;
			this.boolField = boolField;
			this.insertable = insertable;
			this.updatable = updatable;
		}
	}
	
	@Table("table")
	static class ComplexEntityNoSeq {
		@PkColumn(value = "tableId")
		int id;
		
		@Column("name")
		String name;
		
		@Column("BlobFieldInputStream")
		InputStream in;

		@Column("BlobFieldByteArray")
		byte[] bArray;
		
		@Column("BooleanField")
		boolean boolField = true;
		
		@Column(value = "insertable", updatable = false)
		int insertable;
		
		@Column(value = "updatable", insertable = false)
		int updatable;

		public ComplexEntityNoSeq(int id, String name, InputStream in,
				byte[] bArray, boolean boolField,
				int insertable, int updatable) {
			this.id = id;
			this.name = name;
			this.in = in;
			this.bArray = bArray;
			this.boolField = boolField;
			this.insertable = insertable;
			this.updatable = updatable;
		}
	}
	
}
