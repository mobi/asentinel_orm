package com.asentinel.common.orm;

/**
 * Entity that can have children - other entities associated with this one.<br>
 * <br>
 * 
 * <b>Important: </b> This interface is only intended to be implemented by
 * domain objects. It should not be used in general application code to
 * reference domain objects.
 * 
 * @see Entity
 * @see Parent
 * @see EntityBuilder
 * @see EntityUtils
 * @see SimpleEntityDescriptor
 * @see SimpleEntityDescriptor#getTargetMember()
 * 
 * @author Razvan Popian
 */
public interface ParentEntity extends Entity, Parent {

}
