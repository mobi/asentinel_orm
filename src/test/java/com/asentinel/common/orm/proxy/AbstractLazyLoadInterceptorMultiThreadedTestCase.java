package com.asentinel.common.orm.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

/**
 * This test ensures that under concurrent condition the lazy loading 
 * occurs exactly once for a certain proxy instance.
 *  
 * @author Razvan Popian
 */
public class AbstractLazyLoadInterceptorMultiThreadedTestCase {
	static final int THREADS = 50;

	private final AtomicInteger counter = new AtomicInteger(0);
	private final TestLazyLoadInterceptor interceptor = new TestLazyLoadInterceptor();
	private final TestEntity entity = new TestEntity(10, "test");
	private  final TestEntityProxy proxy = new TestEntityProxy(entity);
	private final Callable<?> zuper = Mockito.mock(Callable.class);
	
	private ExecutorService executor;
	private final CyclicBarrier cb = new CyclicBarrier(THREADS + 1);
	
	
	@Before
	public void setup() {
		executor = Executors.newCachedThreadPool();
	}

	@After
	public void teardown() {
		executor.shutdown();
	}
	
	
	@Test
	public void test() throws Exception {
		
		for (int i = 0; i < THREADS; i++) {
			executor.execute(() -> {
				await();
				sleep();
				interceptor.loadProxy(proxy, zuper);
				sleep();
				await();
			});
		}
		
		await();
		await();
		
		// ensure the copy state method is called exactly once
		assertEquals(1, counter.get());
		assertNull(proxy.com$asentinel$common$orm$proxy$loader);
		assertEquals(entity.id, proxy.id);
		assertEquals(entity.name, proxy.name);
		
		// ensure the super method is called by each thread
		verify(zuper, times(THREADS)).call();
	}
	
	private class TestLazyLoadInterceptor extends AbstractLazyLoadInterceptor<TestEntity> {

		@Override
		protected TestEntity load(Function<Object, TestEntity> loader, Object proxy, List<Field> toBeDiscarded) {
			return loader.apply(EntityUtils.getEntityId(proxy));
		}

		@Override
		protected void copyState(TestEntity source, Object proxy) {
			((TestEntity) proxy).id = source.id;
			((TestEntity) proxy).name = source.name;
			counter.incrementAndGet();
		}
		
	}
	
	@Table("test")
	private static class TestEntity {
		
		// the volatile might not be needed since the CyclicBarrier should 
		// ensure visibility for this members according to its memory consistency effects
		// documented in the java docs
		
		@PkColumn("id")
		volatile int id;
		
		@Column("name")
		volatile String name;

		public TestEntity() {
			
		}
		
		public TestEntity(int id, String name) {
			this.id = id;
			this.name = name;
		}
	}
	
	public static class TestEntityProxy extends TestEntity {
		private TestEntity entity;
		
		public TestEntityProxy(TestEntity entity) {
			this.entity = entity;
		}

		
		// the variable name is critical
		public volatile Function<Object, TestEntity> com$asentinel$common$orm$proxy$loader = id -> entity;
	
	}
	
	private void await() {
		try {
			cb.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void sleep() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
