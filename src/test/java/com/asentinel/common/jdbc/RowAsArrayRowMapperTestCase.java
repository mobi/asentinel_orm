package com.asentinel.common.jdbc;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.junit.Test;

/**
 * Shallow test for the {@link RowAsArrayRowMapper}. A deeper test of the
 * {@link AbstractRowMapper} methods is done in the {@link RowAsMapRowMapperTestCase}
 * class.
 */
public class RowAsArrayRowMapperTestCase {
	
	@Test
	public void test() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		ResultSetMetaData md = createMock(ResultSetMetaData.class);
		
		expect(rs.getMetaData()).andReturn(md).anyTimes();
		
		int i = 1;
		expect(md.getColumnType(i)).andReturn(Types.VARCHAR);
		expect(rs.getObject(i)).andReturn("val_" + i);
		i++;
		expect(md.getColumnType(i)).andReturn(Types.CHAR);
		expect(rs.getObject(i)).andReturn("val_" + i);
		i++;
		
		int colCount = i - 1;
		expect(md.getColumnCount()).andReturn(colCount);

		replay(rs, md);
		Object[] row = new RowAsArrayRowMapper().mapRow(rs, 1);
		verify(rs, md);
		
		assertEquals(colCount, row.length);
		assertEquals("val_1", row[0]);
		assertEquals("val_2", row[1]);
		

	}


}
