package com.asentinel.common.orm;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.orm.mappers.ColumnRowMapper;
import com.asentinel.common.orm.mappers.IntRowMapper;
import com.asentinel.common.orm.mappers.LongRowMapper;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.StringRowMapper;
import com.asentinel.common.orm.proxy.Proxy;
import com.asentinel.common.orm.proxy.ProxyFactorySupport;
import com.asentinel.common.util.Assert;

/**
 * @see EntityBuilder
 * 
 * @author Razvan Popian
 */
public final class EntityUtils {
	
	private EntityUtils() {
	
	}
	
	// TODO: add method isAnnotatedEntityClass that will not consider an entity an
	// implementation of the Entity interface
	
	/**
	 * Checks the class to see if it is an entity (either annotated or 
	 * an {@code Entity} implementation).
	 * 
	 * @param cls the class to test.
	 * @return {@code true} if the class implements {@link Entity} or 
	 * 			has a member (field or method) annotated with {@link PkColumn}.
	 * 			{@code false} otherwise.
	 */
	public static boolean isEntityClass(Class<?> cls) {
		if (cls == null) {
			return false;
		}
		if (Entity.class.isAssignableFrom(cls)) {
			return true;
		}
		TargetMembers targetMembers = TargetMembersHolder.getInstance().getTargetMembers(cls);
		if (targetMembers.getPkColumnMember() != null) {
			return true;
		}
		return false;
	}	
	
	/**
	 * Returns the type of the id (primary key) for the specified entity class. Throws an
	 * exception if the class is not an annotated entity (including if the {@code class} parameter
	 * is an implementation of the {@link Entity} interface).
	 * @param cls the class get the type of the entity id.
	 * @return the type of the entity id for the specified entity class
	 * @throws IllegalArgumentException if the {@code class} parameter is not an annotated entity.
	 */
	public static Class<?> getEntityIdClass(Class<?> cls) throws IllegalArgumentException {
		Assert.assertNotNull(cls, "cls");
		TargetMembers targetMembers = TargetMembersHolder.getInstance().getTargetMembers(cls);
		if (targetMembers.getPkColumnMember() == null) {
			throw new IllegalArgumentException("Class " + cls.getName() + " is not an annotated entity.");
		}
		return targetMembers.getPkColumnMember().getMemberClass();
	}

	/**
	 * This method returns the entity id of the specified entity. It checks if the
	 * target is an {@link Entity} implementation and if it is it uses the {@link Entity#getEntityId()}
	 * method to get the id. Otherwise it looks for the {@link PkColumn} annotation inside the target
	 * and uses reflection to get the id.
	 * 
	 * @param entity the target entity from which to extract the id.
	 * @return the entity id.
	 * 
	 * @throws IllegalArgumentException if the id can not be extracted from the target.
	 */
	public static Object getEntityId(Object entity) {
		Assert.assertNotNull(entity, "entity");
		if (entity instanceof Entity) {
			return ((Entity) entity).getEntityId();
		}
		TargetMember targetMember = TargetMembersHolder.getInstance()
				.getTargetMembers(entity.getClass()).getPkColumnMember();
		if (targetMember != null) {
			AnnotatedElement member = targetMember.getAnnotatedElement();
			if (member instanceof Field) {
				ReflectionUtils.makeAccessible((Field) member);
				return ReflectionUtils.getField((Field) member, entity);
			} else if (member instanceof Method) {
				Method getMethod = targetMember.getGetMethod();
				ReflectionUtils.makeAccessible(getMethod);
				return ReflectionUtils.invokeMethod(getMethod, entity);
			} else {
				throw new IllegalStateException("Expected Field or Method. Found " + member.getClass().getName());
			}
		} else {
			throw new IllegalArgumentException("The target object is not an entity.");
		}
	}

	/**
	 * This method sets the entity id of the specified entity. It checks if the
	 * target is an {@link Entity} implementation and if it is it uses the {@link Entity#setEntityId(Object)}
	 * method to set the id. Otherwise it looks for the {@link PkColumn} annotation inside the target
	 * and uses reflection to set the id.
	 * 
	 * @param entity the target entity from which to extract the id.
	 * 
	 * @throws IllegalArgumentException if the id can not be set.
	 */
	public static void setEntityId(Object entity, Object entityId){
		Assert.assertNotNull(entity, "entity");
		if (entity instanceof Entity) {
			((Entity) entity).setEntityId(entityId);
			return;
		}
		TargetMember targetMember = TargetMembersHolder.getInstance()
				.getTargetMembers(entity.getClass()).getPkColumnMember();
		if (targetMember != null) {
			AnnotatedElement member = targetMember.getAnnotatedElement();
			if (member instanceof Field) {
				ReflectionUtils.makeAccessible((Field) member);
				ReflectionUtils.setField((Field) member, entity, entityId);
			} else if (member instanceof Method) {
				ReflectionUtils.makeAccessible((Method) member);
				ReflectionUtils.invokeMethod((Method) member, entity, entityId);
			} else {
				throw new IllegalStateException("Expected Field or Method. Found " + member.getClass().getName());
			}
		} else {
			throw new IllegalArgumentException("The target object is not an entity.");
		}
	}
	
	public static RowMapper<?> getEntityIdRowMapper(Class<?> pkClass, String mapperPkName){
		Assert.assertNotNull(pkClass, "pkClass");
		Assert.assertNotEmpty(mapperPkName, "mapperPkName");
		if (pkClass == int.class || pkClass == Integer.class) {
			// we need a mapper that can return null values (ie: does not default to 0)
			return new IntRowMapper(mapperPkName, true);
		} else if (pkClass == long.class || pkClass == Long.class) {
			// we need a mapper that can return null values (ie: does not default to 0)
			return new LongRowMapper(mapperPkName, true);
		} else if (pkClass == String.class) {
			return new StringRowMapper(mapperPkName, true);
		} else {
			// FIXME: we need to deal better with generic objects
			return new ColumnRowMapper(mapperPkName);
		}
	}
	
	/**
	 * Checks if an object (normally an entity or a collection) is a proxy or not.
	 * @param target the target object to test.
	 * @return {@code true} if the target is a proxy.
	 * 
	 * @see #isLoadedProxy(Object)
	 */
	public static boolean isProxy(Object target) {
		return target instanceof Proxy;
	}

	/**
	 * Checks if a proxy is loaded (the proxy loader member is {@code null}). 
	 * Before calling this method you need to make sure that the argument is indeed 
	 * a proxy, use {@link #isProxy(Object)} for that. 
	 * 
	 * @param proxy the target proxy to test.
	 * @return {@code true} if the target proxy is a loaded proxy.
	 * 
	 * @throws IllegalArgumentException if the target entity is not a proxy.
	 * 
	 * @see #isProxy(Object)
	 */
	public static boolean isLoadedProxy(Object proxy) {
		Assert.assertTrue(proxy instanceof Proxy, "The argument is not a proxy.");
		Field loaderField = ProxyFactorySupport.findLoaderField(proxy.getClass());
		Object loader = ReflectionUtils.getField(loaderField, proxy);
		return loader == null;
	}
	
	/**
	 * Forces the load of the proxy received as argument. Note that this
	 * method uses the {@code Object#equals(Object)} method to load the proxy. If the
	 * {@code Object#equals(Object)} method is overridden and marked {@code final} the
	 * loading will not happen.
	 *  
	 * @param entity the target proxy to force load.
	 */
	public static void loadProxy(Object entity) {
		if (!isProxy(entity)) {
			return;
		}
		// force the loading of the proxy, note that if equals is declared final
		// the loading will not happen
		// TODO: should we add a default load method in the Proxy interface and use that here ??
		entity.equals(entity);
	}
}
