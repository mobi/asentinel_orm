package com.asentinel.common.orm;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.persist.Updater;
import com.asentinel.common.orm.proxy.entity.ProxyFactory;
import com.asentinel.common.util.Assert;

/**
 * Central interface for performing ORM operations. This is not intended
 * to be implemented in the user code.
 * 
 * @see SqlBuilderFactory
 * @see Updater
 * @see OrmTemplate
 *  
 * @author Razvan Popian
 * @author horatiu.dan
 */
public interface OrmOperations extends SqlBuilderFactory, Updater {

	/**
	 * Creates a proxy for the target entity using the specified entity class and id. The loading of the 
	 * entity fields is delayed until they are actually needed (a method other than the id getter/setter or {@code toString} 
	 * is called on the proxy). The only field set in the proxy and immediately available over the entire life of the proxy 
	 * without triggering any database operations is the entity id.<br>
	 * Note that any SQL-related exceptions are delayed until the actual query is executed.
	 * 
	 * @param type the target class. 
	 * @param entityId the id of the target entity.
	 * @return proxy for the target entity.
	 * 
	 * @see #getProxy(Class, Object, Function) 
	 * @see #getEntity(Class, Object)
	 * @see AutoLazyLoader
	 */
	default <T> T getProxy(Class<T> type, Object entityId) {
		return this.getProxy(type, entityId, 
				ProxyEntityDescriptor.getLoader(this, type, null));
	}

	/**
	 * Creates a proxy for the target entity using the specified entity class and id. This version of the {@code getProxy}
	 * method should be used when the database call for loading the target entity has to be customized.
	 * <br> 
	 * The loading of the entity fields is delayed until they are actually needed (a method other than the id getter/setter or {@code toString} 
	 * is called on the proxy). The only field set in the proxy and immediately available over the entire life of the proxy 
	 * without triggering the {@code loader} function is the entity id.<br>
	 * The {@code loader} parameter is a function that will be used for loading the target instance from the database.
	 * 
	 * @param type the target class. 
	 * @param entityId the id of the target entity.
	 * @param loader function used for loading the target entity.
     *               The argument is the entity unique identifier.
     *               It should return the entity with the specified id.
     *
	 * @return proxy for the target entity.
	 * 
	 * @see #getProxy(Class, Object)
	 * @see #getEntity(Class, Object)
	 * @see AutoLazyLoader
	 */
	default <T> T getProxy(Class<T> type, Object entityId, Function<Object, T> loader) {
		Assert.assertNotNull(type, "type");
		Assert.assertNotNull(entityId, "entityId");
		Assert.assertNotNull(loader, "loader");
		T entity = ProxyFactory.getInstance().newProxy(type, loader);
		EntityUtils.setEntityId(entity, entityId);
		return entity;
	}
		
	/**
	 * Eagerly loads the entity with the specified class and id.
	 * 
	 * @param type the target class. 
	 * @param entityId the id of the target entity.
     *
	 * @return the entity for the specified type and id.
     *
	 * @throws IncorrectResultSizeDataAccessException if more than one entity is found for the specified id.
	 * @throws EmptyResultDataAccessException if the entity cannot be found.
	 * 
	 * @see #getProxy(Class, Object)
	 */
	default <T> T getEntity(Class<T> type, Object entityId) 
			throws IncorrectResultSizeDataAccessException, EmptyResultDataAccessException {
		Assert.assertNotNull(type, "type");
		Assert.assertNotNull(entityId, "entityId");
		return this.newSqlBuilder(type)
				.select().where().id().eq(entityId)
				.execForEntity();
	}

    /**
     * Loads the entity with the specified {@code class} and {@code entityId}
     * and uses any provided {@code nodeCallbacks} to customize the tree when the entity is loaded.
     * <p>
 *     For example:
 *     <p>
     * {@code orm.getEntity(CarModel.class, 1L,
     *          AutoEagerLoader.forPath(CarModel.class, CarManufacturer.class))}
     * <p>
     * allows loading a {@code CarModel} entity with id 1 while eagerly loading its {@code CarManufacturer}.
     *
     * @param type the target class.
     * @param entityId the unique identifier of the target entity.
     * @param nodeCallbacks {@link EntityDescriptorNodeCallback} chain to allow the customization of the tree
     *                      when the entity is loaded
     *
     * @return the entity for the specified type and id.
     *
     * @throws IncorrectResultSizeDataAccessException if more than one entity is found for the specified id.
     * @throws EmptyResultDataAccessException if the entity cannot be found.
     */
    default <T> T getEntity(Class<T> type, Object entityId,
                            EntityDescriptorNodeCallback ... nodeCallbacks)
            throws IncorrectResultSizeDataAccessException, EmptyResultDataAccessException {
        Assert.assertNotNull(type, "type");
        Assert.assertNotNull(entityId, "entityId");
        return this.newSqlBuilder(type)
                .select(nodeCallbacks).where().id().eq(entityId)
                .execForEntity();
    }

	/**
	 * Eagerly loads the entity with the specified class and id if found. 
	 * 
	 * @param type the target class.
	 * @param entityId the id of the target entity.
     *
	 * @return an {@link Optional} containing the entity if found or
	 * 		   an empty {@link Optional} otherwise.
     *
     * @throws IncorrectResultSizeDataAccessException if more than one entity is found for the specified id.
	 */
	default <T> Optional<T> getOptional(Class<T> type, Object entityId)
            throws IncorrectResultSizeDataAccessException {
		Assert.assertNotNull(type, "type");
		Assert.assertNotNull(entityId, "entityId");
		return this.newSqlBuilder(type)
				.select().where().id().eq(entityId)
				.execForOptional();
	}

    /**
     * Loads the entity with the specified {@code class} and {@code entityId}
     * and uses any provided {@code nodeCallbacks} to customize the tree when the entity is loaded.
     * <p>
     *     For example:
     *     <p>
     * {@code orm.getEntity(CarModel.class, 1L,
     *          AutoEagerLoader.forPath(CarModel.class, CarManufacturer.class))}
     * <p>
     * allows loading a {@code CarModel} entity with id 1 while eagerly loading its {@code CarManufacturer}.
     *
     * @param type the target class.
     * @param entityId the unique identifier of the target entity.
     * @param nodeCallbacks {@link EntityDescriptorNodeCallback} chain to allow the customization of the tree
     *                      when the entity is loaded
     *
     * @return an {@link Optional} containing the entity if found or
     * 	  		   an empty {@link Optional} otherwise.
     *
     * @throws IncorrectResultSizeDataAccessException if more than one entity is found for the specified id.
     */
    default <T> Optional<T> getOptional(Class<T> type, Object entityId,
                            EntityDescriptorNodeCallback ... nodeCallbacks)
            throws IncorrectResultSizeDataAccessException {
        Assert.assertNotNull(type, "type");
        Assert.assertNotNull(entityId, "entityId");
        return this.newSqlBuilder(type)
                .select(nodeCallbacks).where().id().eq(entityId)
                .execForOptional();
    }
}
