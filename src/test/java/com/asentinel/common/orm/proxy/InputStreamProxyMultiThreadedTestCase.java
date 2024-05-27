package com.asentinel.common.orm.proxy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.support.lob.LobHandler;

import com.asentinel.common.jdbc.InputStreamRowMapper;
import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

/**
 * Ensures that the stream proxy is initialized exactly one in concurrent
 * conditions.
 * 
 * @author Razvan Popian
 *
 */
public class InputStreamProxyMultiThreadedTestCase {
	static final int THREADS = 50;

	private ExecutorService executor;
	private final CyclicBarrier cb = new CyclicBarrier(THREADS + 1);
	
	private final SqlQuery qEx = mock(SqlQuery.class);
	private final LobHandler lh = mock(LobHandler.class);
	
	
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
		StreamWrapper sw = new StreamWrapper();
		byte[] bytes = {1, 2, 3};
		@SuppressWarnings("resource")
		InputStreamProxy p = new InputStreamProxy(qEx, lh, sw, "bytes");
		
		when(qEx.queryForObject(anyString(), 
				any(InputStreamRowMapper.class), 
				eq(sw.id)))
			.thenReturn(new ByteArrayInputStream(bytes));
		
		for (int i = 0; i < THREADS; i++) {
			executor.execute(() -> {
				await();
				sleep();
				try {
					p.read();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				sleep();
				await();
			});
		}
		
		await();
		await();
		
		verify(qEx, times(1)).queryForObject(anyString(), 
				any(InputStreamRowMapper.class), 
				eq(sw.id));
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
	
	@Table("SW")
	private static class StreamWrapper {
		@PkColumn("id")
		int id = 10;
	}
}
