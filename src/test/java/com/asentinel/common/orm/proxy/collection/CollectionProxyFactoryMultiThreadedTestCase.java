package com.asentinel.common.orm.proxy.collection;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Simple multithreaded test for CollectionProxyFactory, we do it because the CollectionLazyLoadInterceptor
 * has some state in the form of a ThreadLocal.
 *  
 * @author Razvan Popian
 */
public class CollectionProxyFactoryMultiThreadedTestCase {
	static final int THREADS = 50;
	
	final AtomicInteger counter = new AtomicInteger(0);
	
	ExecutorService executor;
	CyclicBarrier cb = new CyclicBarrier(THREADS + 1);
	
	@Before
	public void setup() {
		executor = Executors.newCachedThreadPool();
	}

	@After
	public void teardown() {
		executor.shutdown();
	}
	
	@Test
	public void test() {
		for (int i = 0; i < THREADS; i++) {
			executor.execute(new Task(createArray(i)));
		}
		
		await();
		await();
		
		// ensure all tasks completed succesfully
		assertEquals(THREADS, counter.get());
	}
	
	class Task implements Runnable {
		
		private final List<?> target;
		
		Task(Integer[] a) {
			this.target = Arrays.asList(a);
		}
		
		@Override
		public void run() {
			await();
			try {
				sleep();
				ArrayList<?> list = CollectionProxyFactory.getInstance().newProxy(ArrayList.class, 
						(id) -> {
							counter.incrementAndGet();
							return target; 
						},
						10);
				list.size();
				sleep();
				assertEquals(target, list);				
			} catch (Throwable e) {
				System.out.println(e.getMessage());
				throw e;
			} finally {
				await();
			}
		}
		
	}
	
	private Integer[] createArray(int n) {
		Integer[] a = new Integer[n];
		for (int i = 0; i < n; i++) {
			a[i] = i;
		}
		return a;
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
