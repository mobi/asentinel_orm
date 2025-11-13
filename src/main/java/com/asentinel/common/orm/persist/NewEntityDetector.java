package com.asentinel.common.orm.persist;

/**
 * Strategy interface used by {@link Updater} implementations to detect 
 * if a bean is new  (should be inserted in the database) or if it corresponds
 * to an existing record (should be updated in the database).
 * 
 * @see Updater
 * 
 * @author Razvan Popian
 */
public interface NewEntityDetector {
	
	/**
	 * @param entity the target entity.
	 * @return true if the entity parameter is new, false
	 * 			if it is an entity that exists in the database.
	 * @see #isNew(Object)
	 */
	boolean isNewEntity(Object entity);
	
	/**
	 * @param entityId the target entity id.
	 * @return true if the entity id parameter is new, false
	 * 			if it is an entity id that exists in the database.
	 */
	boolean isNew(Object entityId);

}
