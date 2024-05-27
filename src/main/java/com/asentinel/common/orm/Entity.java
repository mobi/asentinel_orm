package com.asentinel.common.orm;

/**
 * Interface to be implemented by objects that have a unique identifier, 
 * a primary key. The entity id must implement {@link Object#equals(Object)} 
 * and {@link Object#hashCode()}, because Entity objects are likely to be cached
 * using their entity id as key.<br><br>
 * 
 * <b>Important: </b> 
 * This interface is only intended to be implemented by domain objects and to be used
 * by the {@link EntityBuilder} class. It should not be used in general application code
 * to reference domain objects.
 * 
 * @see Parent
 * @see ParentEntity
 * @see EntityBuilder
 * @see EntityUtils
 * @see EntityUtils#setEntityId(Object, Object)
 * 
 * @author Razvan Popian
 */
public interface Entity {
	
	/**
	 * @return the primary key as object.
	 */
	Object getEntityId();
	
	/**
	 * Set the primary key object.
	 */
	void setEntityId(Object entityId);

}
