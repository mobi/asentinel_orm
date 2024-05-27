package com.asentinel.common.orm.proxy.collection;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.proxy.AbstractLazyLoadInterceptor;
import com.asentinel.common.orm.proxy.AbstractToStringInterceptor;

/**
 * ByteBuddy interceptor that can perform lazy initialization of a {@code Collection} or {@code Map}. 
 * The lazy initialization is performed by loading
 * the collection and then adding all the elements of the loaded instance in the target instance.
 * <br><br>
 * This class should not be used directly by any client code. 
 * 
 * @see AbstractLazyLoadInterceptor
 * @see AbstractToStringInterceptor
 * @see CollectionToStringInterceptor
 * @see CollectionProxyFactory
 * 
 * @author Razvan Popian
 */
public final class CollectionLazyLoadInterceptor extends AbstractLazyLoadInterceptor<Collection<?>> {
	private final static Logger log = LoggerFactory.getLogger(CollectionLazyLoadInterceptor.class);
	
	private static final ThreadLocal<Boolean> localCall = new ThreadLocal<>();
	
	CollectionLazyLoadInterceptor() { }
	
	@Override
	protected boolean isRouteDirectlyToTarget() {
		return Boolean.TRUE == localCall.get();
	}
	
	@Override
	protected Collection<?> load(Function<Object, Collection<?>> loader, Object proxy, List<Field> toBeDiscarded) {
		Field fieldParentId = CollectionProxyFactory.findParentIdField(proxy.getClass());
		toBeDiscarded.add(fieldParentId);
		Object parentId = ReflectionUtils.getField(fieldParentId, proxy);
		Collection<?> target = loader.apply(parentId);
		if (log.isDebugEnabled()) {
			log.debug("load - Lazy loaded target collection with " + target.size() + " elements.");
		}
		return target;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void copyState(Collection<?> source, Object proxy) {
		localCall.set(Boolean.TRUE);
		try {
			if (proxy instanceof Collection) {
				((Collection) proxy).addAll(source);
			} else if (proxy instanceof Map) {
				for (Object entity: source) {
					((Map) proxy).put(EntityUtils.getEntityId(entity), entity);
				}
			} else {
				throw new IllegalArgumentException("Expected a Collection or Map implementation."); 
			}
		} finally {
			localCall.remove();
		}
	}
}
