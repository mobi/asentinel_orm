package com.asentinel.common.orm;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.core.convert.Property;
import org.springframework.core.convert.TypeDescriptor;

import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.util.Assert;

/**
 * Framework class, stores information about a member annotated with
 * one of the framework annotations. 
 * 
 * @author Razvan Popian
 */
public class TargetMember {
	private final AnnotatedElement member;
	private final Annotation annotation;
	
	private final Method getMethod;
	private final Method setMethod;
	
	private final TypeDescriptor typeDescriptor;
	
	public TargetMember(AnnotatedElement member, Annotation annotation) {
		this(member, annotation, null, null);
	}
	
	public TargetMember(AnnotatedElement member, Annotation annotation, Method getMethod, Method setMethod) {
		Assert.assertNotNull(member, "member");
		Assert.assertNotNull(annotation, "annotation");
		if (member instanceof Method) {
			if (((Method) member).getParameterTypes().length != 1) {
				throw new IllegalArgumentException("The method " + member + " should have exactly one parameter.");
			}
		}
		if (getMethod != null 
				&& getMethod.getParameterTypes().length != 0) {
			throw new IllegalArgumentException("The method " + getMethod + " should have no parameters.");
		}
		if (setMethod != null 
				&& setMethod.getParameterTypes().length != 1) {
			throw new IllegalArgumentException("The method " + setMethod + " should have exactly one parameter.");
		}
		this.member = member;
		this.annotation = annotation;
		this.getMethod = getMethod;
		this.setMethod = setMethod;
		if (annotation instanceof Column 
				|| annotation instanceof PkColumn
				|| member instanceof Field) {
			/* 
			 * We create TypeDescriptors for :
			 * 	- any field (including @Child annotated fields)
			 *  - any @PkColumn annotated member
			 *  - any annotated @Column member
			 *  
			 *  but so far we only need type descriptors for @Column/@PkColumn annotated members
			 *  see ConversionSupport and/or SimpleUpdater
			 */
			this.typeDescriptor = getTypeDescriptor(member, getMethod);
		} else {
			this.typeDescriptor = null;
		}
	}

	public AnnotatedElement getAnnotatedElement() {
		return member;
	}

	public Annotation getAnnotation() {
		return annotation;
	}
	
	public PkColumn getPkColumnAnnotation() {
		if (annotation instanceof PkColumn) {
			return (PkColumn) annotation;
		}
		throw new IllegalStateException("The type of the annotation is not @PkColumn.");
	}
	
	
	public Column getColumnAnnotation() {
		if (annotation instanceof Column) {
			return (Column) annotation;
		}
		throw new IllegalStateException("The type of the annotation is not @Column.");
	}
	
	public Method getGetMethod() {
		return getMethod;
	}
	
	public Method getSetMethod() {
		return setMethod;
	}
	
	public Class<?> getMemberClass() {
		if (member instanceof Method) {
			Class<?>[] parameterTypes = ((Method) member).getParameterTypes();
			if (parameterTypes.length != 1) {
				throw new IllegalArgumentException("The method (" + annotation.getClass().getName() + " method) should have exactly one parameter.");
			}
			return parameterTypes[0];
		} else if (member instanceof Field) {
			return ((Field) member).getType();
		} else {
			throw new IllegalStateException("Expected Field or Method. Found " + member.getClass().getName());					
		}
	}
	
	public Class<?> getMemberDeclaringClass() {
		if (member instanceof Method) {
			return ((Method) member).getDeclaringClass();
		} else if (member instanceof Field) {
			return ((Field) member).getDeclaringClass();
		} else {
			throw new IllegalStateException("Expected Field or Method. Found " + member.getClass().getName());					
		}
	}
	
	// FIXME: can return null for @Child and so it arguably breaks the Liskov substitution principle so we need to refactor, possibly create a TargetColumnMember
	// although this seems to break backwards compatibility for the APIs of the TargetXxxx classes
	
	/**
	 * @return the {@code TypeDescriptor} for this member assuming one could be
	 *         created on construction. Otherwise it returns {@code null}. Always
	 *         returns {@code null} for {@code Child} annotated <b>methods</b>.
	 */
	public TypeDescriptor getTypeDescriptor() {
		return typeDescriptor;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [member=" + member + ", annotation="
				+ annotation.toString() + "]";
	}
	
	private static TypeDescriptor getTypeDescriptor(AnnotatedElement member, Method getter) {
		if (member instanceof Field) {
			return TypeDescriptor.nested((Field) member, 0);
		} else if (member instanceof Method) {
			Method method = (Method) member;
			if (method.getParameterTypes().length != 1) {
				throw new IllegalArgumentException("The method " + method + " should have exactly one parameter.");
			}			
			Class<?> cls = method.getParameterTypes()[0];
			return TypeDescriptor.nested(new Property(cls, getter, (Method) member), 0);
		} else {
			throw new IllegalArgumentException("The member " + member + " is neither a field nor a method.");
		}
	}

}
