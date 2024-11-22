package com.asentinel.common.orm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * Tests the {@link TargetMembers} cache under multiple thread access. We ensure that for a certain class
 * the {@link TargetMembers} instance is calculated exactly once.
 */
public class TargetMembersHolderThreadsTestCase {
	
	static final int CLIENT_COUNT = 50;
	
    ExecutorService pool;
    CyclicBarrier barrier = new CyclicBarrier(CLIENT_COUNT + 1);
    
    // thread safe list
	List<TargetMembers> calculatedTargetMembers = new Vector<TargetMembers>();
    
    @Before
    public void setup() {
    	pool = Executors.newCachedThreadPool();
    }
    
    @After
    public void tearDown() {
    	pool.shutdown();
    }
	
	
	@Test
	public void test() {
		for (int i = 0; i < CLIENT_COUNT; i++) {
			pool.execute(new TargetMembersRunnable());			
		}
		try {
			barrier.await();
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			fail("Barrier failed.");
		}
		
		assertEquals(CLIENT_COUNT, calculatedTargetMembers.size());
		// validate that all the threads got the same instance of TargetMembers
		for (int i = 0; i < calculatedTargetMembers.size() - 1; i++){
			assertSame(calculatedTargetMembers.get(i), calculatedTargetMembers.get(i + 1));
		}
	}
	
	
	class TargetMembersRunnable implements Runnable {

		@Override
		public void run() {
			try {				
				barrier.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				throw new RuntimeException(e);
			}
			try {
				calculatedTargetMembers.add(TargetMembersHolder.getInstance().getTargetMembers(Bean.class));
			} finally {
				try {				
					barrier.await();
				} catch (InterruptedException | BrokenBarrierException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
