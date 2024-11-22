package com.asentinel.common.orm.persist;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;
import com.asentinel.common.orm.proxy.ProxyFactorySupport;
import com.asentinel.common.orm.proxy.entity.ProxyFactory;

public class SimpleUpdaterCollectionWithProxiesTestCase {
	
	JdbcFlavor jdbcFlavor = new PostgresJdbcFlavor(); // should be mocked, for now since PG is our target we are ok with it

	JdbcOperations jdbcOps = mock(JdbcOperations.class);
	SqlQuery ex = mock(SqlQuery.class);
	Updater u = new SimpleUpdater(jdbcFlavor, ex);
	
	@Captor
	ArgumentCaptor<BatchPreparedStatementSetter> captor;

    @Before
    public void init(){
       MockitoAnnotations.initMocks(this);
       when(ex.getJdbcOperations()).thenReturn(jdbcOps);
    }

	@Test
	public void testWithUnloadedProxy() {
		SimpleEntity entity1 = new SimpleEntity(1);
		SimpleEntity entity2 = new SimpleEntity(2);
		SimpleEntity entity3 = new SimpleEntity(3);
		SimpleEntity proxy = ProxyFactory.getInstance().getProxyObjectFactory(SimpleEntity.class).newObject();
		Function<Object, SimpleEntity> loader = id -> new SimpleEntity(300);
		ReflectionUtils.setField(ProxyFactorySupport.findLoaderField(proxy.getClass()), proxy, loader);
		EntityUtils.setEntityId(proxy, 300);

		List<SimpleEntity> list = Arrays.asList(entity1, proxy, entity2, entity3);
		when(jdbcOps.batchUpdate(anyString(), captor.capture()))
			.thenReturn(new int[]{Statement.SUCCESS_NO_INFO});
		u.update(list);
		
		BatchPreparedStatementSetter setter = captor.getValue();
		
		@SuppressWarnings("unchecked")
		List<SimpleEntity> actual = (List<SimpleEntity>) ReflectionTestUtils.getField(setter, "entities");
		assertEquals(Arrays.asList(entity1, entity2, entity3), actual);
	}
	
	@Test
	public void testWithLoadedProxy() {
		SimpleEntity entity1 = new SimpleEntity(1);
		SimpleEntity entity2 = new SimpleEntity(2);
		SimpleEntity entity3 = new SimpleEntity(3);
		SimpleEntity proxy = ProxyFactory.getInstance().getProxyObjectFactory(SimpleEntity.class).newObject();
		Function<Object, SimpleEntity> loader = id -> new SimpleEntity(300);
		ReflectionUtils.setField(ProxyFactorySupport.findLoaderField(proxy.getClass()), proxy, loader);
		EntityUtils.setEntityId(proxy, 300);
		// load the proxy
		proxy.forceLoad();

		List<SimpleEntity> list = Arrays.asList(entity1, proxy, entity2, entity3);
		when(jdbcOps.batchUpdate(anyString(), captor.capture()))
			.thenReturn(new int[]{Statement.SUCCESS_NO_INFO});
		u.update(list);
		
		BatchPreparedStatementSetter setter = captor.getValue();
		
		@SuppressWarnings("unchecked")
		List<SimpleEntity> actual = (List<SimpleEntity>) ReflectionTestUtils.getField(setter, "entities");
		assertEquals(Arrays.asList(entity1, proxy, entity2, entity3), actual);
	}
	
	// FYI: The following classes need to be public for ByteBuddy 1.9.2 if the class loading strategy is not ClassLoadingStrategy.Default.INJECTION. 
	// For 1.5.10 they can be declared as package private (this is ideal, because I don't want them to be visible outside the package). 
	// I opened a stackoverflow question on this issue: 
	// https://stackoverflow.com/questions/52829988/behavior-change-on-bytebuddy-version-1-9-1-compared-to-1-5-10
	
	@Table("table")
	public static class SimpleEntity {
		@PkColumn("id")
		int id;
		
		@Column("string")
		String s;
		
		@SuppressWarnings("unused")
		public SimpleEntity() {
			
		}
		
		public SimpleEntity(int id) {
			this.id = id;
		}
		
		public void forceLoad() {
			
		}
		
		@Override
		public String toString() {
			return "SimpleEntity [id=" + id + "]";
		}
	}
}
