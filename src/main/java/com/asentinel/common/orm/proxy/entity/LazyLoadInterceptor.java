package com.asentinel.common.orm.proxy.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.proxy.AbstractLazyLoadInterceptor;

/**
 * ByteBuddy interceptor that can perfom lazy initialization of
 * the fields of the target instance. The lazy initialization is performed by loading
 * an instance and than copying the fields of the loaded instance in the target instance.
 * <br><br>
 * This class should not be used directly by any client code. 
 * 
 * @see ToStringInterceptor
 * @see ProxyFactory
 * 
 * @author Razvan Popian
 */
public final class LazyLoadInterceptor extends AbstractLazyLoadInterceptor<Object> {
	private final static Logger log = LoggerFactory.getLogger(LazyLoadInterceptor.class);
	
	LazyLoadInterceptor() { }
	
	@Override
	protected Object load(Function<Object, Object> loader, Object proxy, List<Field> toBeDiscarded) {
		Object target = loader.apply(EntityUtils.getEntityId(proxy));
		if (log.isDebugEnabled()) {
			log.debug("load - Lazy loaded target instance: " + target);
		}
		return target;
	}
	
	@Override
	protected void copyState(Object source, Object proxy) {
		ReflectionUtils.doWithFields(proxy.getClass().getSuperclass(), 
			new ReflectionUtils.FieldCallback() {
				@Override
				public void doWith(Field field) {
					if (Modifier.isStatic(field.getModifiers())) {
						return;
					}
					
					// Note that even the final fields will be copied, not sure why this works
					// but this is the behavior we prefer anyway
					
					ReflectionUtils.makeAccessible(field);
					Object value = ReflectionUtils.getField(field, source);
					ReflectionUtils.setField(field, proxy, value);
				}
			}
		);
	}
	
}
