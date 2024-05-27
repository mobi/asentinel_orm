package com.asentinel.common.orm.converter.entity;

import static com.asentinel.common.orm.converter.entity.IdToEntityConverter.getIdType;

import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

import com.asentinel.common.orm.EntityUtils;

/**
 * {@code Converter} implementation that can convert an annotated entity either to {@code String} by 
 * using the string representation of the entity id or to the actual type of the entity id.
 * 
 * @see IdToEntityConverter
 * 
 * @author Razvan Popian
 */
public class EntityToIdConverter implements ConditionalGenericConverter {
	
	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (!EntityUtils.isEntityClass(sourceType.getType())) {
			return false;
		}
		Class<?> idType = getIdType(sourceType);
		
		// TODO: relax the exact match rule for id conversion. We should be fine if source entity id is assignable to
		// the target id
		return String.class == targetType.getObjectType() || idType == targetType.getObjectType();
	}

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		return null;
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (source == null) {
			return null;
		} else if (targetType.getObjectType() == String.class) {
			return String.valueOf(EntityUtils.getEntityId(source));
		} else {
			// exact id type match
			return EntityUtils.getEntityId(source);
		}
	}
	
}

