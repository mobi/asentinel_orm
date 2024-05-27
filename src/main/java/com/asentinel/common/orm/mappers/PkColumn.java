package com.asentinel.common.orm.mappers;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.asentinel.common.orm.EntityBuilder;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.orm.TargetMembers;
import com.asentinel.common.orm.TargetMembersHolder;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumn;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumnsEntity;
import com.asentinel.common.orm.persist.Updater;

/**
 * Annotation that associates the table primary key
 * with a property or setter method. Should be used with
 * {@link AnnotationRowMapper}. Either fields or setter methods
 * should be annotated. Getter methods are not supported.
 * 
 * <br><br>
 * 
 * <b>Important:</b><br>
 *
 * <li> The annotated field/method parameter should properly implement
 * {@link Object#equals(Object)} and {@link Object#hashCode()} because the 
 * entity is likely to be cached by the {@link EntityBuilder} class in a map.
 *
 * <br><br>
 * 
 * <li>If a <code>PkColumn</code> annotated setter method is overridden in a subclass, any <code>PkColumn</code> 
 * annotation on the method in the super class is inherited unless the subclass method is also annotated with <code>PkColumn</code>.
 * 
 * <br> 
 * This is a feature, it allows for example to override the <code>PkColumn</code> annotation attributes in a subclass.
 * 
 * 
 * @see AnnotationRowMapper
 * @see TargetMembers
 * @see TargetMembersHolder
 * @see DynamicColumn
 * @see DynamicColumnsEntity 
 * 
 * @author Razvan Popian
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PkColumn {
	
	/**
	 * @return the name of the column.
	 */
	String value();	
	
	/**
	 * @return the name of the database sequence to use for generating new
	 * 			ids. If this is empty the updater framework assumes that the id column
	 * 			is of type autonumber.
	 * @see Updater
	 */
	String sequence() default "";
}
