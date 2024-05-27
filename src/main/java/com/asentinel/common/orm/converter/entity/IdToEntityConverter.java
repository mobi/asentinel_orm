package com.asentinel.common.orm.converter.entity;

import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.OrmOperations;
import com.asentinel.common.util.Assert;

/**
 * {@code Converter} implementation that can convert from an id to an annotated entity object.
 * The source id must either match the type of the id in the entity class or it should be a {@code String}.
 * If the source id is a {@code String}  the following id types are supported:
 * <li> Long.class
 * <li> long.class
 * <li> Integer.class
 * <li> int.class
 * <li> String.class
 * 
 * <br><br>
 * Note that the resulted entity will be a proxy of the real object.
 * 
 * @see #getEntity(Class, Object)
 * @see EntityToIdConverter
 * 
 * @author Razvan Popian
 */
public class IdToEntityConverter implements ConditionalGenericConverter {

	private final OrmOperations ormOps;
	private final boolean numericIdStrictlyGreaterThanZero;
	
	/**
	 * @see #IdToEntityConverter(OrmOperations, boolean)
	 */
	public IdToEntityConverter(OrmOperations ormOps) {
		this(ormOps, true);
	}
	
	/**
	 * Constructor.
	 * @param ormOps {@link OrmOperations} implementation used to pull the target entities
	 * 			from the database (the {@link OrmOperations#getProxy(Class, Object)} method is used).
	 * @param numericIdStrictlyGreaterThanZero determines how negative or zero entity ids are handled.
	 * 
	 * @see #isNumericIdStrictlyGreaterThanZero()
	 */
	public IdToEntityConverter(OrmOperations ormOps, boolean numericIdStrictlyGreaterThanZero) {
		Assert.assertNotNull(ormOps, "ormOps");
		this.ormOps = ormOps;
		this.numericIdStrictlyGreaterThanZero = numericIdStrictlyGreaterThanZero;
	}
	
	
	static Class<?> getIdType(TypeDescriptor typeDescriptor) {
		return ClassUtils.resolvePrimitiveIfNecessary(
				EntityUtils.getEntityIdClass(typeDescriptor.getType())
			);
	}

	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (!EntityUtils.isEntityClass(targetType.getType())) {
			return false;
		}
		Class<?> idType = getIdType(targetType);
		
		// TODO: relax the exact match rule for id conversion. We should be fine if the source id is assignable to
		// the target entity id
		return String.class == sourceType.getType() 
				|| idType == sourceType.getObjectType();
	}

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		return null;
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (source == null) {
			return null;
		}
		Class<?> idType = getIdType(targetType);
		Object id;
		if (source instanceof String) {
			if (idType == Long.class) {
				if (!StringUtils.hasLength((String) source)) {
					return null;
				}
				id = Long.parseLong((String) source);
			} else if (idType == Integer.class) {
				if (!StringUtils.hasLength((String) source)) {
					return null;
				}
				id = Integer.parseInt((String) source);
			} else if (idType == String.class) {
				id = source.toString();
			} else {
				throw new IllegalArgumentException("Unsupported id type: " + source.getClass().getName());
			}
		} else {
			// exact id type match
			id = source;
		}
		// TODO: in the future we may need some kind of injectable strategy to determine
		// when an entity id should be converted to an entity and when the id should be converted to null
		if (numericIdStrictlyGreaterThanZero && id instanceof Number) {
			Number nId = (Number) id;
			if (nId.longValue() <= 0) {
				return null;
			}
		}
		return getEntity(targetType.getType(), id);
	}

	/**
	 * Creates or loads an entity of the specified type with the specified id. By default
	 * this method creates a proxy for the specified type, but subclasses may override this
	 * method to customize the creation/loading of the entity.
	 * 
	 * @param type the type of entity to load/create.
	 * @param entityId the id of the entity to load
	 * @return the entity, by default a proxy.
	 */
	protected <T> T getEntity(Class<T> type, Object entityId) {
		return ormOps.getProxy(type, entityId);
	}

	/**
	 * @return internal {@code OrmOperations}.
	 */
	public OrmOperations getOrmOperations() {
		return ormOps;
	}

	/**
	 * Configuration property. If {@code true} a negative or zero id will be converted to {@code null}. 
	 * Otherwise the id will be converted to a proxy of the target entity.
	 */
	public boolean isNumericIdStrictlyGreaterThanZero() {
		return numericIdStrictlyGreaterThanZero;
	}


}

