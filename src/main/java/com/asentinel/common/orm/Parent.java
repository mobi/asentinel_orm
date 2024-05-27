package com.asentinel.common.orm;

/**
 * Interface to be implemented by entities that have children and for which
 * annotating target members is not an option (maybe because of the dynamic
 * nature of the children).
 * 
 * <b>Important: </b> This interface is only intended to be implemented by
 * domain objects and to be used by the {@link EntityBuilder} class. It should
 * not be used in general application code to reference domain objects.
 *
 * @see Entity
 * @see ParentEntity
 * @see EntityBuilder
 * 
 * @since 1.66.0
 * @author Razvan Popian
 */
public interface Parent {

	/**
	 * Adds a child to this entity. The implementer can either
	 * assign the entity to a member variable or add it to a member
	 * collection.
	 * @param entity the child object to be added.
	 * @param entityDescriptor the descriptor for the <code>Entity</code>
	 * 			parameter. Can be used to differentiate between different
	 * 			entities having the same class.
	 */
	void addChild(Object entity, EntityDescriptor entityDescriptor);
}
