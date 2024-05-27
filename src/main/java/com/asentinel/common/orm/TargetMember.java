package com.asentinel.common.orm;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

	@Override
	public String toString() {
		return "TargetMember [member=" + member + ", annotation="
				+ annotation.toString() + "]";
	}

}
