package com.asentinel.common.orm;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;
import com.asentinel.common.util.Assert;
import com.asentinel.common.util.ConcurrentCache;

/**
 * Class that calculates and caches the {@link TargetMembers} for a {@link Class}. This is
 * a singleton, use {@link #getInstance()} to obtain an instance.
 * 
 * @see #getTargetMembers(Class)
 * @see TargetMembers
 * 
 * @author Razvan Popian
 *
 */
public final class TargetMembersHolder {
	
	private static final TargetMembersHolder instance = new TargetMembersHolder();
	
	public static TargetMembersHolder getInstance() {
		return instance;
	}
	
	private final ConcurrentCache<String, TargetMembers> cache = new ConcurrentCache<String, TargetMembers>();
	
	/** private constructor */
	private TargetMembersHolder() {
	
	}

	/**
	 * @param clazz the class to get the {@link TargetMembers} for.
	 * @return {@link TargetMembers} instance, it either calculates
	 * 			on the spot or pulls it from the internal cache.
	 */
	public TargetMembers getTargetMembers(final Class<?> clazz) {
		Assert.assertNotNull(clazz, "clazz");
		return cache.get(clazz.getName(),
			new Callable<TargetMembers>() {
				@Override
				public TargetMembers call() throws Exception {
					return getTargetMembersInternal(clazz);
				}
			}
		);
	}
	
	
	private static TargetMembers getTargetMembersInternal(final Class<?> clazz) {
		final TargetMembers members = new TargetMembers();
		
		Table tableAnnotation = AnnotationUtils.findAnnotation(clazz, Table.class);
		members.setTableAnnotation(tableAnnotation);
		
		TableAnnotationInfo firstNonView = findFirstNonViewTableAnnotation(clazz);
		members.setFirstNonViewTable(firstNonView);
		
		// fields - processed before methods, this is important
		ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
			
			@Override
			public void doWith(Field field) {
				process(field, members, clazz);
			}
		});
		
		// methods, we do not include overridden methods from super classes
		Method[] methods = ReflectionUtils.getUniqueDeclaredMethods(clazz);
		for (Method method: methods) {
			process(method, members, clazz);
		}
		
		return members;
	}
	
	private static final String SET_METHOD_PREFIX = "set";
	private static final String GET_METHOD_PREFIX = "get";
	private static final String IS_METHOD_PREFIX = "is";
	
	private static void process(AnnotatedElement member, TargetMembers members, Class<?> clazz) {
		// The processing order matters !
		PkColumn pkColumn = findAnnotation(member, PkColumn.class);
		if (pkColumn != null) {
			if (members.getPkColumnMember() != null) {
				throw new IllegalStateException("Multiple @PkColumn annotations found in class " + clazz.getName() + " .");
			}
			if (member instanceof Method) {
				// method
				members.setPkColumnMember(new TargetMember(member, pkColumn, 
						findGetterMethod(clazz, (Method) member), (Method) member)
				);
			} else {
				// field
				Method[] methods = findGetterAndSetterMethods(clazz, (Field) member);
				members.setPkColumnMember(new TargetMember(member, pkColumn, methods[0], methods[1]));				
			}
			return;
		}
		
		Column column = findAnnotation(member, Column.class);
		if (column != null) {
			if (member instanceof Method) {
				// method
				Method getter = null;
				try {
					getter = findGetterMethod(clazz, (Method) member);
				} catch(Exception e) { }
				members.addColumnMember(new TargetMember(member, column, getter, (Method) member));
			} else {
				// field
				Method[] methods = findGetterAndSetterMethods(clazz, (Field) member);
				members.addColumnMember(new TargetMember(member, column, methods[0], methods[1]));
			}
			return;
		}

		Child childAnn = findAnnotation(member, Child.class);
		if (childAnn != null) {
			if (member instanceof Method) {
				// method
				Method getter = null;
				try {
					getter = findGetterMethod(clazz, (Method) member);
				} catch(Exception e) { }
				members.addChildMember(new TargetChildMember(member, childAnn, getter, (Method) member));
			} else {
				// field
				members.addChildMember(new TargetChildMember(member, childAnn));
			}
			return;
		}
	}
	
	// Utility static methods
	
	private static <T extends Annotation> T findAnnotation(AnnotatedElement member, Class<T> annotationType) {
		if (member instanceof Method) {
			return AnnotationUtils.findAnnotation((Method) member, annotationType);
		}
		return AnnotationUtils.getAnnotation(member, annotationType);
	}
	
	
	static boolean isTableAnnView(Table tableAnn) {
		return tableAnn != null && tableAnn.view();
		
	}
	
	static TableAnnotationInfo findFirstNonViewTableAnnotation(Class<?> clasz) {
		List<Class<?>> hierarchy = new ArrayList<>();
		while (clasz != null) {
			hierarchy.add(0, clasz);
			clasz = clasz.getSuperclass();
		}
		
		Table tableAnnTable = null;
		int indexTypeTable = -1;
		int indexTypeView = -1;
		for (int i = 0; i < hierarchy.size(); i++) {
			
			// look at class level
			Class<?> type = hierarchy.get(i);
			Table tableAnn = AnnotationUtils.getAnnotation(type, Table.class);
			if (tableAnn == null) {
				// look into implemented interfaces
				// TODO: interface lookup should be further analysed/tested
				Class<?>[] interfaces = type.getInterfaces();
				for (Class<?> interfce: interfaces) {
					tableAnn = AnnotationUtils.findAnnotation(interfce, Table.class);
					if (tableAnn != null) {
						break;
					}
				}
				if (tableAnn == null) {
					continue;
				}
			}
			
			if (tableAnn.view()) {
				// find and log the first view @Table
				if (indexTypeView >= 0) {
					// if already found do not override
					continue;
				}
				indexTypeView = i;				
			} else {
				// find and log the first non view @Table
				tableAnnTable = tableAnn;
				indexTypeTable = i;
			}
		}
		
		if (indexTypeTable < 0) {
			// no non view table (real DB table)
			return null;
		}
		if (indexTypeView < 0) {
			// no view table in hierarchy, we return the last subclass
			return new TableAnnotationInfo(hierarchy.get(hierarchy.size() - 1), tableAnnTable);
		}
		if (indexTypeTable > indexTypeView) {
			// non view table defined in subclass of view table
			return null;
		}
		
		// we look for the first non abstract superclass in the hierarchy and we return that
		for (int i = indexTypeView - 1; i >= 0; i--) {
			if (!(Modifier.isAbstract(hierarchy.get(i).getModifiers()))) {
				return new TableAnnotationInfo(hierarchy.get(i), tableAnnTable);		
			}
		}
		
		// unable to find a qualifying class
		return null;
	}
	
	static Method findGetterMethod(Class<?> clazz, Method setter) {
		Assert.assertNotNull(clazz, "clazz");
		Assert.assertNotNull(setter, "setter");
		String sName = setter.getName();
		if (sName.toLowerCase().startsWith(SET_METHOD_PREFIX)) {
			if (setter.getParameters().length == 0) {
				throw new IllegalArgumentException("Invalid setter method " + setter);
			}
			final Class<?> setterParameterType = setter.getParameters()[0].getType();
			final String gName = GET_METHOD_PREFIX + sName.substring(SET_METHOD_PREFIX.length());
			final String isName = IS_METHOD_PREFIX + sName.substring(SET_METHOD_PREFIX.length());
			final List<Method> methods = new ArrayList<Method>(1);
			ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
				
				@Override
				public void doWith(Method method) {
					if (method.getParameterTypes().length == 0
							&& !method.getReturnType().equals(void.class)) {
						if (gName.equalsIgnoreCase(method.getName())) {
							// found a getter candidate
							methods.add(method);
						} 
						if ((Boolean.class.equals(setterParameterType) || boolean.class.equals(setterParameterType))
							&& isName.equalsIgnoreCase(method.getName())) {
							// found a IS getter candidate for a boolean field
							methods.add(method);
						}
					}
				}
			});
			if (methods.size() == 0) {
				throw new IllegalArgumentException("Can not determine the get method for the setter " + setter);
			} else if (methods.size() >= 1) {
				return selectGetMethod(methods);
			} else {
				throw new RuntimeException("Unexpected error.");
			}
		} else {
			throw new IllegalArgumentException("Can not determine the get method for the setter " + setter);
		}
	}

	static Method[] findGetterAndSetterMethods(Class<?> clazz, Field field) {
		final int G_IDX = 0;
		final int S_IDX = 1;
		Assert.assertNotNull(clazz, "clazz");
		Assert.assertNotNull(field, "field");
		String fName = field.getName();
		String gName = GET_METHOD_PREFIX + fName;
		String isName = IS_METHOD_PREFIX + fName;
		String sName = SET_METHOD_PREFIX + fName;	
		Method[] methods = new Method[2];
		List<Method> getMethods = new ArrayList<Method>(1);
		List<Method> setMethods = new ArrayList<Method>(1);
		ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
			
			@Override
			public void doWith(Method method) {
				if (method.getParameterTypes().length == 0
						&& !method.getReturnType().equals(void.class)) {
					if (gName.equalsIgnoreCase(method.getName())) {
						// found a getter candidate
						getMethods.add(method);
					} 
					if ((Boolean.class.equals(field.getType()) || boolean.class.equals(field.getType()))
						&& isName.equalsIgnoreCase(method.getName())) {
						// found a IS getter candidate for a boolean field
						getMethods.add(method);
					}
				}
				if (method.getParameterTypes().length == 1
						&& method.getReturnType().equals(void.class)
						&& sName.equalsIgnoreCase(method.getName())) {
					// found a setter candidate
					setMethods.add(method);
				}
			}
		});
		
		if (getMethods.size() == 0) {
			methods[G_IDX] = null;
		} else if (getMethods.size() >= 1) {
			methods[G_IDX] = selectGetMethod(getMethods);
		}
		
		if (setMethods.size() == 0) {
			methods[S_IDX] = null;
		} else if (setMethods.size() >= 1) {
			methods[S_IDX] = selectSetMethod(setMethods, field);
		}
		
		return methods;
	}
	
	// Usually we get to call these methods for bridge methods
	// implemented by the compiler for covariant return types.
	// The method with the most specific return type will be 
	// returned.
	private static Method selectGetMethod(List<Method> methods) {
		Collections.sort(methods, (m1, m2) -> {
			if (m1.getReturnType() == m2.getReturnType()) {
				return 0;
			}
			if (m1.getReturnType().isAssignableFrom(m2.getReturnType())) {
				return 1;
			}
			return -1;
		});
		return methods.get(0);
	}
	
	private static Method selectSetMethod(List<Method> methods, Field field) {
		for (Method method: methods) {
			if (method.getParameterTypes()[0] == field.getType()) {
				return method;
			}
		}
		// FIXME: we may want to further refine the selection algorhitm so that
		// if the field type is int and there is no set(int) method, but we have
		// a set(Integer) and a set(Long) the set(Integer) will be preferred.
		return methods.get(0);
	}
	
}
