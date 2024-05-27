package com.asentinel.common.orm;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.asentinel.common.orm.mappers.Child;

/**
 * Framework class, a specialization of {@code TargetMember} dedicated to
 * {@code Child} annotations.
 * 
 * @see TargetMember
 * 
 * @author Razvan Popian
 */
public class TargetChildMember extends TargetMember {
	
	private final Class<?> type;

	public TargetChildMember(AnnotatedElement member, Child annotation) {
		this(member, annotation, null, null);
	}
	
	public TargetChildMember(AnnotatedElement member, Child annotation, Method getMethod, Method setMethod) {
		super(member, annotation, getMethod, setMethod);
		this.type = calculateType(member, annotation);
	}
	
	@Override
	public Child getAnnotation() {
		return (Child) super.getAnnotation();
	}
	
	public Class<?> getChildType() {
		return type;
	}
	
	/**
	 * @return the name of the foreign key for a {@code RelationType#ONE_TO_MANY}
	 * 		parent relation. Can return {@code null} if the type is not an entity. 
	 * 		(See {@link EntityUtils#isEntityClass(Class)}).
	 * @throws IllegalStateException if the parent relation is not {@code RelationType#ONE_TO_MANY}
	 * 		and no explicit fk name was defined in the {@code Child} annotation.
	 */
	public String getFkNameForOneToMany() {
		Child childAnn = getAnnotation();
		String fkName = childAnn.fkName();
		if (!StringUtils.hasText(fkName)) {
			if (childAnn.parentRelationType() == RelationType.ONE_TO_MANY) {
				// the convention is that if the fkName is not specified at the
				// annotation level then it is the same name as the pk
				if (StringUtils.hasText(childAnn.pkName())) {
					return childAnn.pkName();
				}
				Class<?> type = getChildType();
				TargetMember pkTargetMember = TargetMembersHolder.getInstance()
						.getTargetMembers(type).getPkColumnMember();
				if (pkTargetMember == null) {
					return null;
				}
				fkName = pkTargetMember.getPkColumnAnnotation().value();
				return fkName;
			}
			throw new IllegalStateException("The parent relation is not ONE_TO_MANY. The method works only for ONE_TO_MANY"
					+ " or for explicitly defined fk name in the @Child annotation.");
		} else {
			return fkName;
		}
	}

	
	private static final Class<?> calculateType(AnnotatedElement element, Child annotation) {
		// annotations validation
		if (!(element instanceof Member)) {
			throw new IllegalArgumentException("Expected Member.");
		}
		
		// determine the class of the child
		Class<?> finalClazz = annotation.type();
		if (Entity.class.equals(finalClazz)) {
			// get the type from the element
			if (element instanceof Field) {
				Field field = (Field) element;
				Class<?> fieldClass = field.getType();
				if (Collection.class.isAssignableFrom(fieldClass)
						|| Map.class.isAssignableFrom(fieldClass)) {
					fieldClass = detectCollectionMemberType(field);
				} // TODO: else test if the class actually represents an entity ? 
				finalClazz = (Class<?>) fieldClass;
			} else if (element instanceof Method) {
				Method method = (Method) element;
				if (method.getParameterTypes().length != 1) {
					throw new IllegalArgumentException("The method " + method + " should have exactly one parameter.");
				}
				Class<?> fieldClass = method.getParameterTypes()[0];
				// TODO: test if the class actually represents an entity ?
				finalClazz = (Class<?>) fieldClass;
			} else {
				throw new IllegalArgumentException("Expected Field or Method. Found " + element.getClass().getName() + ".");
			}
		}
		if (finalClazz == null) {
			throw new IllegalArgumentException("Unable to detect the type of entity.");
		}
		return finalClazz;

	}
	
	private static Class<?> detectCollectionMemberType(Field field) {
		Class<?> fieldClass = null;
		Type type = field.getGenericType();
		if (type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) type;
			Type[] types = pType.getActualTypeArguments();
			if (types != null
					&& types.length > 0) {
				if (types.length == 1
						&& types[0] instanceof Class) {
					// Collection
					fieldClass = (Class<?>) types[0];
				} else if (types.length == 2
						&& types[1] instanceof Class) {
					// Map
					fieldClass = (Class<?>) types[1];
				}
				// TODO: test if the class actually represents an entity ?
			}
		}
		if (fieldClass == null) {
			throw new IllegalStateException("A collection is annotated with @Child, "
					+ "but the class of the entities to be stored in the collection can not be autodetected. Field: " + field);
		}
		return (Class<?>) fieldClass;
	}


}
