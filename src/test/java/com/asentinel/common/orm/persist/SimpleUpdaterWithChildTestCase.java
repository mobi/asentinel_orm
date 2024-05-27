package com.asentinel.common.orm.persist;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.Capture;
import org.easymock.IAnswer;
import org.junit.Test;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class SimpleUpdaterWithChildTestCase {
	
	final static String SEQ_NEXT_VAL_PLACEHOLDER = "[SEQ]";
	
	final static String INSERT = "insert into A(aId, name, x2Id, xId) values(?, ?, ?, ?)";
	final static String INSERT_AUTO_ID = "insert into A(aId, name, x2Id, xId) values(" + SEQ_NEXT_VAL_PLACEHOLDER+ ", ?, ?, ?)";
	final static String INSERT_AUTO_ID_NO_SEQ = "insert into A(name, x2Id, xId) values(?, ?, ?)";
	final static String UPDATE = "update A set name = ?, x2Id = ?, xId = ? where aId = ?";
	final static int ID = 11;

	JdbcFlavor jdbcFlavor = new PostgresJdbcFlavor();
	
	SqlQuery ex = createMock(SqlQuery.class);
	Updater u = new SimpleUpdater(jdbcFlavor, ex);
	
	Capture<String> cSql = Capture.newInstance();
	Capture<Integer> c0 = Capture.newInstance();
	Capture<Integer> c1 = Capture.newInstance();
	Capture<Integer> c2 = Capture.newInstance();
	Capture<Integer> c3 = Capture.newInstance();
	
	Capture<String[]> cIdNames = Capture.newInstance();
	Capture<KeyHolder> cKeyHolder = Capture.newInstance();
	
	A a = new A(1, "test", 2, new X(10), new X(20));
	AA aa = new AA(1, "test", 2, new X(10), new X(20));
	AAA aaa = new AAA(1, "test", 2, new X(10), new X(20));
	
	@Test
	public void testInsert() {
		expect(ex.update(capture(cSql), capture(c0),
				capture(c1), capture(c2), capture(c3)))
				.andReturn(1);
		replay(ex);
		int count = u.update(a, UpdateType.INSERT);
		verify(ex);
		assertEquals(1, count);
		assertEquals(a.id, c0.getValue().intValue());
		assertEquals(a.name, c1.getValue());
		assertEquals(a.x2Id, c2.getValue().intValue());
		assertEquals(a.x1.id, c3.getValue().intValue());
		assertEquals(INSERT.replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "").toLowerCase()
		);
	}
	
	@Test
	public void testInsertWithNullChild() {
		a.x1 = null;
		a.x2 = null;
		expect(ex.update(capture(cSql), capture(c0),
				capture(c1), capture(c2), capture(c3)))
				.andReturn(1);
		replay(ex);
		int count = u.update(a, UpdateType.INSERT);
		verify(ex);
		assertEquals(1, count);
		assertEquals(a.id, c0.getValue().intValue());
		assertEquals(a.name, c1.getValue());
		assertEquals(a.x2Id, c2.getValue().intValue());
		assertEquals(null, c3.getValue());
		assertEquals(INSERT.replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "").toLowerCase()
		);
	}
	

	@Test
	public void testUpdate() {
		expect(ex.update(capture(cSql), capture(c0),
				capture(c1), capture(c2), capture(c3)))
				.andReturn(1);
		replay(ex);
		int count = u.update(a, UpdateType.UPDATE);
		verify(ex);
		assertEquals(1, count);
		assertEquals(a.name, c0.getValue());
		assertEquals(a.x2Id, c1.getValue().intValue());
		assertEquals(a.x1.id, c2.getValue().intValue());
		assertEquals(a.id, c3.getValue().intValue());
		assertEquals(UPDATE.replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "").toLowerCase()
		);
	}
	
	@Test
	public void testInsertAutoIdNoSeq() {
		expect(ex.update(capture(cSql), capture(cIdNames), capture(cKeyHolder),
				capture(c1), capture(c2), capture(c3)))
			.andAnswer(new IAnswer<Integer>() {

				@Override
				public Integer answer() throws Throwable {
					Object[] args = getCurrentArguments();
					for (Object arg: args) {
						if (arg instanceof GeneratedKeyHolder) {
							GeneratedKeyHolder generatedKeyHolder = (GeneratedKeyHolder) arg;
							List<Map<String, Object>> generatedKeys = generatedKeyHolder.getKeyList();
							generatedKeys.add(new HashMap<String, Object>());
							generatedKeys.get(0).put("", ID);
							return 1;
						}
					}
					fail("Can not find KeyHolder argument.");
					// make the compiler happy, return null
					return null;
				}
			})
		;
		
		replay(ex);
		int count = u.update(a, UpdateType.INSERT_AUTO_ID);
		verify(ex);
		assertEquals(1, count);
		assertEquals(a.id, ID);
		assertEquals(a.name, c1.getValue());
		assertEquals(a.x2Id, c2.getValue().intValue());
		assertEquals(a.x1.id, c3.getValue().intValue());
		assertEquals(INSERT_AUTO_ID_NO_SEQ.replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "").toLowerCase()
		);
	}

	@Test
	public void testInsertAutoId() {
		expect(ex.update(capture(cSql), capture(cIdNames), capture(cKeyHolder),
				capture(c1), capture(c2), capture(c3)))
			.andAnswer(new IAnswer<Integer>() {

				@Override
				public Integer answer() throws Throwable {
					Object[] args = getCurrentArguments();
					for (Object arg: args) {
						if (arg instanceof GeneratedKeyHolder) {
							GeneratedKeyHolder generatedKeyHolder = (GeneratedKeyHolder) arg;
							List<Map<String, Object>> generatedKeys = generatedKeyHolder.getKeyList();
							generatedKeys.add(new HashMap<String, Object>());
							generatedKeys.get(0).put("", ID);
							return 1;
						}
					}
					fail("Can not find KeyHolder argument.");
					// make the compiler happy, return null
					return null;
				}
			})
		;
		
		replay(ex);
		int count = u.update(aa, UpdateType.INSERT_AUTO_ID);
		verify(ex);
		assertEquals(1, count);
		assertEquals(aa.id, ID);
		assertEquals(aa.name, c1.getValue());
		assertEquals(aa.x2Id, c2.getValue().intValue());
		assertEquals(aa.x1.id, c3.getValue().intValue());
		assertEquals(INSERT_AUTO_ID.replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "")
				.replace(jdbcFlavor.getSqlTemplates().getSqlForNextSequenceVal("seq"), SEQ_NEXT_VAL_PLACEHOLDER)
				.toLowerCase()
		);
	}

	
	@Test
	public void testInsert_IgnoreFK() {
		expect(ex.update(capture(cSql), capture(c0),
				capture(c1), capture(c2), capture(c3)))
				.andReturn(1);
		replay(ex);
		int count = u.update(aaa, UpdateType.INSERT);
		verify(ex);
		assertEquals(1, count);
		assertEquals(aaa.id, c0.getValue().intValue());
		assertEquals(aaa.name, c1.getValue());
		assertEquals(aaa.x2Id, c2.getValue().intValue());
		assertEquals(aaa.x1.id, c3.getValue().intValue());
		assertEquals(INSERT.replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "").toLowerCase()
		);
	}
	
	@Test
	public void testUpdate_IgnoreFk() {
		expect(ex.update(capture(cSql), capture(c0),
				capture(c1), capture(c2), capture(c3)))
				.andReturn(1);
		replay(ex);
		int count = u.update(aaa, UpdateType.UPDATE);
		verify(ex);
		assertEquals(1, count);
		assertEquals(aaa.name, c0.getValue());
		assertEquals(aaa.x2Id, c1.getValue().intValue());
		assertEquals(aaa.x1.id, c2.getValue().intValue());
		assertEquals(aaa.id, c3.getValue().intValue());
		assertEquals(UPDATE.replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "").toLowerCase()
		);
	}


	
	
	@Table("X")
	private static class X {
		@PkColumn("xId")
		int id;

		public X(int id) {
			this.id = id;
		}
	}
	
	@Table("A")
	private static class A {
		int id;
		
		@SuppressWarnings("unused")
		public int getId() {
			return id;
		}
		
		@PkColumn("aId")
		public void setId(int id) {
			this.id = id;
		}
		
		@Column("name")
		String name;
		
		// this has priority over the x2
		@Column("x2Id")
		int x2Id;
		
		@Child
		X x1;
		
		@Child(fkName = "X2ID")
		X x2;

		public A(int id, String name, int x2Id, X x1, X x2) {
			this.id = id;
			this.name = name;
			this.x2Id = x2Id;
			this.x1 = x1;
			this.x2 = x2;
		}
	}
	
	private static class AA extends A {

		public AA(int id, String name, int x2Id, X x1, X x2) {
			super(id, name, x2Id, x1, x2);
		}

		@PkColumn(value = "aId", sequence = "seq")
		public void setId(int id) {
			this.id = id;
		}

	}
	
	private static class AAA extends A {
		
		@Child(fkName = "XIDAAA", parentAvailableFk = false)
		X x3;

		public AAA(int id, String name, int x2Id, X x1, X x2) {
			super(id, name, x2Id, x1, x2);
		}

	}

}
