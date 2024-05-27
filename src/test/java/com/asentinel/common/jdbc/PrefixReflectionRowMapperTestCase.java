package com.asentinel.common.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class PrefixReflectionRowMapperTestCase {
	
	private final static Logger log = LoggerFactory.getLogger(PrefixReflectionRowMapperTestCase.class);
	
	private static final String PREFIX = "prefix_";
	
	private ResultSet rs = createMock(ResultSet.class);
	private ResultSetMetaData rsmd = createMock(ResultSetMetaData.class);

	
	@Before
	public void setup() throws SQLException {
		
		expect(rs.getMetaData()).andReturn(rsmd).anyTimes();
		
		expect(rs.wasNull()).andReturn(false).anyTimes();
		expect(rs.getRow()).andReturn(0).anyTimes();
		expect(rs.getFetchSize()).andReturn(10).anyTimes();

		expect(rs.next()).andReturn(true);
		expect(rs.getInt(PREFIX + "id")).andReturn(1);
		expect(rs.getObject(PREFIX + "name")).andReturn("test");
		expect(rs.next()).andReturn(false);
		rs.close();
	}
	
	@Test
	public void testNormalOperation() throws SQLException {
		String[] cols = new String[] {
				PREFIX + "id",
				"ignored_column1",
				PREFIX + "name",
				"ignored_column2",
				"ignored_column3",
		};
		expect(rsmd.getColumnCount()).andReturn(cols.length).anyTimes();
		int i = 1;
		for (String col: cols) {
			expect(rsmd.getColumnName(i)).andReturn(col);
			i++;
		}
		
		replay(rs, rsmd);
		List<PrefixBean> results = ResultSetUtils.asList(rs, new PrefixReflectionRowMapper<PrefixBean>(PrefixBean.class, PREFIX));
		verify(rs, rsmd);
		
		assertEquals(1, results.size());
		PrefixBean bean = results.get(0);
		assertEquals(1, bean.getId());
		assertEquals("test", bean.getName());
		
	}
	

	@Test
	public void testMissingMethod() throws SQLException {
		String[] cols = new String[] {
				PREFIX + "id",
				"ignored_column1",
				PREFIX + "name2222",
				"ignored_column2",
				"ignored_column3",
		};
		expect(rsmd.getColumnCount()).andReturn(cols.length).anyTimes();
		int i = 1;
		for (String col: cols) {
			expect(rsmd.getColumnName(i)).andReturn(col);
			i++;
		}
		
		replay(rs, rsmd);
		try {
			ResultSetUtils.asList(rs, new PrefixReflectionRowMapper<PrefixBean>(PrefixBean.class, PREFIX));
			fail("Should not get to this line.");
		} catch (Exception e) {
			log.debug("Expected exception: " + e.getMessage());
		}
		
	}

	@Test
	public void testMissingMethodWithBestEffortMode() throws SQLException {
		String[] cols = new String[] {
				PREFIX + "id",
				"ignored_column1",
				PREFIX + "name2222",
				"ignored_column2",
				"ignored_column3",
		};
		expect(rsmd.getColumnCount()).andReturn(cols.length).anyTimes();
		int i = 1;
		for (String col: cols) {
			expect(rsmd.getColumnName(i)).andReturn(col);
			i++;
		}
		
		replay(rs, rsmd);
		PrefixBean b = ResultSetUtils.asList(rs, new PrefixReflectionRowMapper<PrefixBean>(PrefixBean.class, PREFIX, true)).get(0);
		assertEquals(1, b.id);
		assertNull(b.name);
	}
	
	
	@Test
	public void testIgnoredColumn() throws SQLException {
		String[] cols = new String[] {
				PREFIX + "id",
				PREFIX + "IgnoredColumn",
				PREFIX + "name",
		};
		expect(rsmd.getColumnCount()).andReturn(cols.length).anyTimes();
		int i = 1;
		for (String col: cols) {
			expect(rsmd.getColumnName(i)).andReturn(col);
			i++;
		}
		
		replay(rs, rsmd);
		PrefixReflectionRowMapper<PrefixBean> mapper = new PrefixReflectionRowMapper<PrefixBean>(PrefixBean.class, PREFIX);
		mapper.ignore("IGNOREDColumn");
		ResultSetUtils.asList(rs, mapper);
		verify(rs, rsmd);		
	}
	

	@Test
	public void testIgnoredColumnWithPrefix() throws SQLException {
		String[] cols = new String[] {
				PREFIX + "id",
				PREFIX + "IgnoredColumn",
				PREFIX + "name",
		};
		expect(rsmd.getColumnCount()).andReturn(cols.length).anyTimes();
		int i = 1;
		for (String col: cols) {
			expect(rsmd.getColumnName(i)).andReturn(col);
			i++;
		}
		
		replay(rs, rsmd);
		PrefixReflectionRowMapper<PrefixBean> mapper = new PrefixReflectionRowMapper<PrefixBean>(PrefixBean.class, PREFIX);
		mapper.ignore(PREFIX + "IGNOREDColumn");
		ResultSetUtils.asList(rs, mapper);
		verify(rs, rsmd);
	}
	
	
	public static class PrefixBean {
		private int id;
		private String name;
		
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}

}
