package com.asentinel.common.orm.persist;

import com.asentinel.common.orm.EntityUtils;

/**
 * Default implementation of the {@link NewEntityDetector} strategy. This
 * implementation considers entities new if:
 * <li> their id is not <code>null</code>
 * <li> if their id is a {@link Number} the entity is new if
 * 		the id is strict positive.
 * <br><br>
 * Otherwise the entities are considered old.
 * 
 * @see NewEntityDetector
 *  
 * @author Razvan Popian
 */
public class SimpleNewEntityDetector implements NewEntityDetector {

	@Override
	public boolean isNewEntity(Object entity) {
		return isNew(EntityUtils.getEntityId(entity));
	}

	@Override
	public boolean isNew(Object entityId) {
		if (entityId == null) {
			return true;
		}
		if (entityId instanceof Number) {
			Number n = (Number) entityId;
			if (n.longValue() <= 0) {
				return true;
			}
		}
		return false;
	}

}
