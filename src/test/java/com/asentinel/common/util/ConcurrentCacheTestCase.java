package com.asentinel.common.util;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class ConcurrentCacheTestCase {

	static final int CLIENT_COUNT = 50;
	
    ExecutorService pool = Executors.newCachedThreadPool();
    CyclicBarrier barrier = new CyclicBarrier(CLIENT_COUNT + 1);
    
    ConcurrentCache<Integer, String> cache = new ConcurrentCache<>();
    
	TestCallable c = new TestCallable(100);
    
    private void await() {
		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			throw new RuntimeException(e);
		}
    }
    
    @Test
    public void test() {
    	for (int t = 0; t < CLIENT_COUNT; t++) {
    		pool.execute(() -> {
                await();
                cache.get(100, c);
                await();
            });
    	}
    	await();
    	await();
    	
    	// validate that the value is calculated exactly once
    	assertEquals(1, c.callCount.get());
    	assertEquals(1, cache.getSize());
    }
    
    
    static class TestCallable implements Callable<String> {
    	final int key;
    	AtomicInteger callCount = new AtomicInteger(0);
    	
    	TestCallable(int key) {
    		this.key = key;
    	}

		@Override
		public String call() {
			callCount.incrementAndGet();
			return String.valueOf(key);
		}
    	
    }
    
}
