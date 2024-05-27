package com.asentinel.common.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Cache implementation that ensures that the value for a key will be calculated
 * exactly once under concurrent conditions.
 * <br><br>
 * The caching algorithm implemented in this class can be found
 * in the book <b>"Java Concurrency in Practice"</b> by Doug Lea. As mentioned above, it ensures
 * that a cached value will be calculated only once even if 2 or more client threads
 * call the {@link #get(Object, Callable)} method simultaneously for the same key and the value 
 * for that key is not yet cached. 
 */
public class ConcurrentCache<K, V> {

	private final ConcurrentMap<K, Future<V>> cache = new ConcurrentHashMap<>();
	
	/**
	 * If there is no value for the {@code key} in the cache, this method calculates the value for 
	 * the {@code key} using the {@code supplier} {@link Callable} implementation. 
	 * Otherwise gets the value from the cache.
	 * 
	 * @param key the key that is used to get the value from the cache.
	 * @param supplier a {@link Callable} implementation used to calculate the value for the
	 * 			specified key if that key is not yet present in the cache. The {@code supplier} can not
	 * 			be {@code null}.
	 * @return the value for the specified {@code key}.
	 */
	public final V get(K key, Callable<V> supplier) {
		Assert.assertNotNull(key, "key");
		Assert.assertNotNull(supplier, "supplier");
		Future<V> future = cache.get(key);
		if (future == null) {
			FutureTask<V> future0 = new FutureTask<V>(supplier);
			future = cache.putIfAbsent(key, future0);
			if (future == null) {
				future = future0;
				future0.run();
			}
		}
		try {
			return future.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			} else {
				throw new RuntimeException(e.getCause());
			}
		}
	}
	
	/**
	 * @return the number of entries in the cache.
	 */
	public int getSize() {
		return cache.size();
	}
}
