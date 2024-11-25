package com.asentinel.common.orm.persist;

import static java.util.Collections.singleton;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.easymock.Capture;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;
import com.asentinel.common.orm.mappers.dynamic.DefaultDynamicColumn;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumn;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumnsEntity;
import com.asentinel.common.orm.proxy.InputStreamProxy;
import com.asentinel.common.orm.proxy.ProxyFactorySupport;
import com.asentinel.common.orm.proxy.entity.ProxyFactory;

/**
 * Tests the single entity insert/update operations. Validates that the SQL statements
 * are properly created.
 * 
 * @see SimpleUpdaterCollectionTestCase
 *  
 * @author Razvan Popian
 */
public class SimpleUpdaterTestCase {
	
	// we use this constant because the sequence next val instruction varies depending on the selected
	// JdbcFlavor
	private static final String SEQ_NEXT_VAL_PLACEHOLDER = "[SEQ]";
	
	private static final String INSERT = "insert into table(id, insertable, field1, field2, dynamicField) values(?, ?, ?, ?, ?)";
	private static final String INSERT_AUTO_ID = "insert into table(id, insertable, field1, field2, dynamicField) values(" + SEQ_NEXT_VAL_PLACEHOLDER+ ", ?, ?, ?, ?)";
	private static final String INSERT_AUTO_ID_NO_SEQ = "insert into table(field1, field2, insertable, dynamicField) values(?, ?, ?, ?)";
	private static final String UPDATE = "update table set updatable = ?, field1 = ?, field2 = ?, dynamicField = ? where id = ?";
	private static final String UPDATE_INPUT_STREAMS = "update table set name = ?, bytes = ?, updatableBytes = ?, dynamicBytes = ? where id = ?";
	private static final String UPDATE_INPUT_STREAMS_PROXIES = "update table set name = ? where id = ?";
	private static final String INSERT_INPUT_STREAMS = "insert into table(name, bytes, insertableBytes, dynamicBytes) values(?, ?, ?, ?)";
	
	private static final Number ID = (Number) 10;
	
	private final JdbcFlavor jdbcFlavor = new PostgresJdbcFlavor();
	
	private SqlQuery ex;
	private Updater u;
	
	private final Set<DynamicColumn> dynamicColumns = singleton(new DefaultDynamicColumn("dynamicField", Integer.class));
	
	private final Capture<String> cSql = Capture.newInstance();
	private final Capture<KeyHolder> cKeyHolder = Capture.newInstance();
	
	
	@Before
	public void setup() {
		ex = createMock(SqlQuery.class);
		u = new SimpleUpdater(jdbcFlavor, ex);
	}
	
	@Test
	public void testInsertAutoIdForTableMappedBean() {
		Bean b = new Bean(0, 20, 30, 40, 50, 60);
		
		expect(ex.update((String) capture(cSql), aryEq(new String[] {"id"}), capture(cKeyHolder),
				eq(b.insertable), eq(b.field1), eq(b.field2), eq(b.dynamicField)))
			.andAnswer(new IAnswer<Integer>() {

				@Override
				public Integer answer() {
					Object[] args = getCurrentArguments();
					for (Object arg: args) {
						if (arg instanceof GeneratedKeyHolder) {
							GeneratedKeyHolder generatedKeyHolder = (GeneratedKeyHolder) arg;
							List<Map<String, Object>> generatedKeys = generatedKeyHolder.getKeyList();
							generatedKeys.add(new HashMap<>());
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
		int count = u.update(b, new UpdateSettings<>(dynamicColumns));
		verify(ex);
		assertEquals(1, count);
		assertEquals(ID, (Number) b.id);
		assertEquals(INSERT_AUTO_ID.replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "")
				.replace(jdbcFlavor.getSqlTemplates().getSqlForNextSequenceVal("seq"), SEQ_NEXT_VAL_PLACEHOLDER)
				.toLowerCase()
		);
	}
	
	
	@Test
	public void testInsertAutoIdForTableMappedBean_NoSeq() {
		BeanNoSeq b = new BeanNoSeq(0, 20, 30, 40, 50, 60);
		
		expect(ex.update(capture(cSql), aryEq(new String[] {"id"}), capture(cKeyHolder),
				eq(b.field1), eq(b.field2), eq(b.insertable), eq(b.dynamicField)))
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
		int count = u.update(b, new UpdateSettings<>(dynamicColumns));
		verify(ex);
		assertEquals(1, count);
		assertEquals(ID, b.id);		
		assertEquals(INSERT_AUTO_ID_NO_SEQ.replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "")
				.replace(jdbcFlavor.getSqlTemplates().getSqlForNextSequenceVal("seq"), SEQ_NEXT_VAL_PLACEHOLDER)
				.toLowerCase()
		);
	}
	
	
	@Test
	public void testInsertAutoIdForViewMappedBean() {
		ExtBean b = new ExtBean(0, 20, 30, 40, 50, 60);

		expect(ex.update(capture(cSql), aryEq(new String[] {"id"}), capture(cKeyHolder),
				eq(b.insertable), eq(b.field1), eq(b.field2), eq(b.dynamicField)))
			.andAnswer(new IAnswer<Integer>() {

				@Override
				public Integer answer() {
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
		int count = u.update(b, new UpdateSettings<>(dynamicColumns));
		verify(ex);
		assertEquals(1, count);
		assertEquals(ID, b.id);
		assertEquals(INSERT_AUTO_ID.replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "")
				.replace(jdbcFlavor.getSqlTemplates().getSqlForNextSequenceVal("seq"), SEQ_NEXT_VAL_PLACEHOLDER)
				.toLowerCase()
		);
	}
	

	@Test
	public void testInsertForTableMappedBean() {
		Bean b = new Bean(ID.intValue(), 20, 30, 40, 50, 60);
		
		expect(ex.update(capture(cSql), eq(b.id),
				eq(b.insertable),
				eq(b.field1), eq(b.field2), eq(b.dynamicField)))
				.andReturn(1);
		
		
		replay(ex);
		int count = u.update(b, new UpdateSettings<>(UpdateType.INSERT, dynamicColumns));
		verify(ex);
		assertEquals(1, count);
		assertEquals(INSERT.replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "").toLowerCase()
		);
	}
	
	@Test
	public void testInsertForViewMappedBean() {
		ExtBean b = new ExtBean(ID.intValue(), 20, 30, 40, 50, 60);
		
		expect(ex.update(capture(cSql), eq(b.id),
				eq(b.insertable), eq(b.field1), eq(b.field2), eq(b.dynamicField)))
				.andReturn(1);
		
		replay(ex);
		int count = u.update(b, new UpdateSettings<DynamicColumn>(UpdateType.INSERT, dynamicColumns));
		verify(ex);
		assertEquals(1, count);
		assertEquals(INSERT.replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "").toLowerCase()
		);
	}
	
	

	@Test
	public void testUpdateForTableMappedBean() {
		Bean b = new Bean(10, 20, 30, 40, 50, 60);
		
		expect(ex.update(capture(cSql), eq(b.updatable), eq(b.field1), eq(b.field2), eq(b.dynamicField), eq(b.id)))
			.andReturn(1);
		
		replay(ex);
		int count = u.update(b, new UpdateSettings<DynamicColumn>(dynamicColumns));
		verify(ex);
		assertEquals(1, count);
		assertEquals(UPDATE.replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "").toLowerCase()
		);
	}
	
	@Test
	public void testUpdateForTableMappedUnloadedProxy() {
		Bean b = ProxyFactory.getInstance().getProxyObjectFactory(Bean.class).newObject();
		Function<Object, Bean> loader = id -> new Bean(10, 20, 30, 40, 50, 60);
		ReflectionUtils.setField(ProxyFactorySupport.findLoaderField(b.getClass()), b, loader);
		replay(ex);
		int count = u.update(b);
		verify(ex);
		assertEquals(0, count);
	}
	
	@Test
	public void testUpdateForTableMappedLoadedProxy() {
		Bean b = ProxyFactory.getInstance().getProxyObjectFactory(Bean.class).newObject();
		Bean tb = new Bean(10, 20, 30, 40, 50, 60);
		Function<Object, Bean> loader = id -> tb;
		ReflectionUtils.setField(ProxyFactorySupport.findLoaderField(b.getClass()), b, loader);
		
		expect(ex.update(capture(cSql), eq(tb.updatable), eq(tb.field1), eq(tb.field2), eq(tb.dynamicField), eq(tb.id)))
			.andReturn(1);
		
		// trigger load
		b.getField2();
		
		replay(ex);
		int count = u.update(b, new UpdateSettings<>(dynamicColumns));
		verify(ex);
		assertEquals(1, count);
		assertEquals(UPDATE.replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "").toLowerCase()
		);
	}
	
	
	@Test
	public void testUpdateForViewMappedBean() {
		ExtBean b = new ExtBean(10, 20, 30, 40, 50, 60);
		
		expect(ex.update(capture(cSql), eq(b.updatable), eq(b.field1), eq(b.field2), eq(b.dynamicField), eq(b.id)))
			.andReturn(1);
		
		replay(ex);
		int count = u.update(b, new UpdateSettings<>(dynamicColumns));
		verify(ex);
		assertEquals(1, count);
		assertEquals(UPDATE.replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "").toLowerCase()
		);
	}
	
	
	@Test
	public void testUpdateForInputStreamBean() {
		InputStreamBean b = new InputStreamBean(1, "test", 
				new ByteArrayInputStream(new byte[] {1, 2}), 
				new ByteArrayInputStream(new byte[] {10, 20}),
				new ByteArrayInputStream(new byte[] {11, 22}),
				new ByteArrayInputStream(new byte[] {44, 55})
				);
		expect(ex.update(eq(UPDATE_INPUT_STREAMS), eq(b.name), eq(b.bytes), eq(b.updatableBytes), eq(b.dynamicBytes), eq(b.id)))
			.andReturn(1);
		
		replay(ex);
		int count = u.update(b, new UpdateSettings<DynamicColumn>(singleton(new DefaultDynamicColumn("dynamicBytes", InputStream.class))));
		verify(ex);
		assertEquals(1, count);
	}


	@Test
	public void testUpdateForInputStreamBeanWithProxies() {
		InputStreamBean b = new InputStreamBean(1, "test", 
				createMock(InputStreamProxy.class), createMock(InputStreamProxy.class),
				createMock(InputStreamProxy.class), createMock(InputStreamProxy.class));
		expect(ex.update(eq(UPDATE_INPUT_STREAMS_PROXIES), eq(b.name), eq(b.id)))
			.andReturn(1);
		
		replay(ex);
		int count = u.update(b, new UpdateSettings<DynamicColumn>(singleton(new DefaultDynamicColumn("dynamicBytes", InputStream.class))));
		verify(ex);
		assertEquals(1, count);
	}
	
	
	@Test
	public void testInsertForInputStreamBean() {
		InputStreamBean b = new InputStreamBean(0, "test", 
				new ByteArrayInputStream(new byte[] {1, 2}), 
				new ByteArrayInputStream(new byte[] {10, 20}),
				new ByteArrayInputStream(new byte[] {11, 22}),
				new ByteArrayInputStream(new byte[] {33, 44})
			);
		expect(ex.update(eq(INSERT_INPUT_STREAMS), aryEq(new String[] {"id"}), anyObject(), eq(b.name), eq(b.bytes), eq(b.insertableBytes), eq(b.dynamicBytes)))
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
		int count = u.update(b, new UpdateSettings<DynamicColumn>(singleton(new DefaultDynamicColumn("dynamicBytes", InputStream.class))));
		verify(ex);
		assertEquals(ID, (Number) b.id);
		assertEquals(1, count);
	}
	
	@Test
	public void testInsertForInputStreamBeanWithProxies() {
		InputStreamBean b = new InputStreamBean(0, "test", 
				createMock(InputStreamProxy.class), createMock(InputStreamProxy.class),
				createMock(InputStreamProxy.class), createMock(InputStreamProxy.class));
		expect(ex.update(eq(INSERT_INPUT_STREAMS), anyObject(String[].class), anyObject(), eq(b.name), eq(b.bytes), eq(b.insertableBytes), eq(b.dynamicBytes)))
			.andAnswer(new IAnswer<Integer>() {
	
				@Override
				public Integer answer() throws Throwable {
					Object[] args = getCurrentArguments();
					for (Object arg: args) {
						if (arg instanceof GeneratedKeyHolder) {
							GeneratedKeyHolder generatedKeyHolder = (GeneratedKeyHolder) arg;
							List<Map<String, Object>> generatedKeys = generatedKeyHolder.getKeyList();
							generatedKeys.add(new HashMap<>());
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
		int count = u.update(b, new UpdateSettings<DynamicColumn>(singleton(new DefaultDynamicColumn("dynamicBytes", InputStream.class))));
		verify(ex);
		assertEquals(ID, (Number) b.id);
		assertEquals(1, count);
	}
	
	// FYI: The following classes need to be public for ByteBuddy 1.9.2 if the class loading strategy is not ClassLoadingStrategy.Default.INJECTION. 
	// For 1.5.10 they can be declared as package private (this is ideal, because I don't want them to be visible outside the package). 
	// I opened a stackoverflow question on this issue: 
	// https://stackoverflow.com/questions/52829988/behavior-change-on-bytebuddy-version-1-9-1-compared-to-1-5-10
	
	public static class Parent {
		@Column("field1")
		int field1;
	}

	@SuppressWarnings("rawtypes")
	@Table("table")
	public static class Bean extends Parent implements DynamicColumnsEntity {
		@PkColumn(value = "id", sequence="seq")
		int id;
		
		int field2;
		
		@Column(value = "insertable", updatable = false)
		int insertable;
		
		@Column(value = "updatable", insertable = false)
		int updatable;

		
		int dynamicField;

		public Bean() {
			
		}
		
		public Bean(int id, int field1, int field2, int insertable, int updatable, int dynamicField) {
			this.id = id;
			this.field1 = field1;
			this.field2 = field2;
			this.insertable = insertable;
			this.updatable = updatable;
			this.dynamicField = dynamicField;
		}

		public int getField2() {
			return field2;
		}

		@Column("field2")
		public void setField2(int field2) {
			this.field2 = field2;
		}

		@Override
		public void setValue(DynamicColumn column, Object value) {
			this.dynamicField = (int) value;
			
		}

		@Override
		public Object getValue(DynamicColumn column) {
			return dynamicField;
		}
	}

	@Table(value = "view", view = true)
	public static class ExtBean extends Bean {
		
		@Column("field3")
		private int field3;

		public ExtBean(int id, int field1, int field2, int insertable, int updatable, int dynamicField) {
			super(id, field1, field2, insertable, updatable, dynamicField);
		}
		
	}

	@SuppressWarnings("rawtypes")
	@Table("table")
	public static class BeanNoSeq implements DynamicColumnsEntity {
		@PkColumn(value = "id")
		int id;
		
		@Column("field1")
		int field1;
		
		@Column("field2")
		int field2;
		
		@Column(value = "insertable", updatable = false)
		int insertable;
		
		@Column(value = "updatable", insertable = false)
		int updatable;
		
		int dynamicField;

		public BeanNoSeq() {
			
		}
		
		public BeanNoSeq(int id, int field1, int field2, int insertable, int updatable, int dynamicField) {
			this.id = id;
			this.field1 = field1;
			this.field2 = field2;
			this.insertable = insertable;
			this.updatable = updatable;
			this.dynamicField = dynamicField;
		}

		@Override
		public void setValue(DynamicColumn column, Object value) {
			this.dynamicField = (int) value;
			
		}

		@Override
		public Object getValue(DynamicColumn column) {
			return dynamicField;
		}
	}
	
	@Table("table")
	private static class InputStreamBean implements DynamicColumnsEntity<DefaultDynamicColumn> {
		@PkColumn(value = "id")
		int id;
		
		@Column("name")
		String name;
		
		@Column("bytes")
		InputStream bytes;

		@Column(value= "insertableBytes", updatable = false)
		InputStream insertableBytes;

		@Column(value= "updatableBytes", insertable = false)
		InputStream updatableBytes;

		
		InputStream dynamicBytes;
		
		
		

		public InputStreamBean(int id, String name, InputStream bytes, InputStream insertableBytes, InputStream updatableBytes, InputStream dynamicBytes) {
			this.id = id;
			this.name = name;
			this.bytes = bytes;
			this.insertableBytes = insertableBytes;
			this.updatableBytes = updatableBytes;
			this.dynamicBytes = dynamicBytes;
		}

		@Override
		public void setValue(DefaultDynamicColumn column, Object value) {
			fail("Should not be called.");
		}

		@Override
		public Object getValue(DefaultDynamicColumn column) {
			return dynamicBytes;
		}
	}

}

