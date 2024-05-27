package com.asentinel.common.orm.mappers;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.asentinel.common.jdbc.ConversionSupport;
import com.asentinel.common.jdbc.ResultSetUtils;
import com.asentinel.common.orm.TargetMembers;
import com.asentinel.common.orm.TargetMembersHolder;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumn;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumnsEntity;

/**
 * Annotation that associates a table column
 * with a property or setter method. Should be used with
 * {@link AnnotationRowMapper}. Either fields or setter methods
 * should be annotated. Getter methods are not supported.
 * 
 * <br><br>
 * 
 * <b>Important:</b><br>
 * 
 * If a <code>Column</code> annotated setter method is overridden in a subclass, any <code>Column</code> 
 * annotation on the method in the super class is inherited unless the subclass method is also annotated with <code>Column</code>.
 * <br> 
 * This is a feature, it allows for example to override the <code>Column</code> annotation attributes in a subclass.
 * 
 * @see Table
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
public @interface Column {

	/**
	 * @return the name of the column.
	 */
	String value();
	
	/**
	 * @return whether to set the target member to <code>null</code> if the corresponding resultset column
	 * 		is <code>null</code> or to set it to a default value. For example for numeric fields the default
	 * 		value is <code>0</code>. See {@link ConversionSupport} for details. Please note that for primitive
	 * 		types setting this flag to <code>true</code> will have no effect (ie. if the value in the resultset is <code>null</code>
	 * 		the value set in the primitive target member will still be the default value for that type).
	 * 
	 * @see ConversionSupport
	 * @see ResultSetUtils
	 */
	boolean allowNull() default false;
	
	/**
	 * @return whether this column should be used in insert statements. The
	 *         default is {@code true}.
	 */
	boolean insertable() default true;

	/**
	 * @return whether this column should be used in update statements. The
	 *         default is {@code true}.
	 */
	boolean updatable() default true;
}
