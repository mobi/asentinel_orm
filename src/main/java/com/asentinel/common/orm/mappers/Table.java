package com.asentinel.common.orm.mappers;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.asentinel.common.orm.TargetMembers;
import com.asentinel.common.orm.TargetMembersHolder;
import com.asentinel.common.orm.ed.tree.EntityDescriptorTreeRepository;
import com.asentinel.common.orm.persist.Updater;

/**
 * Annotation that associates a table 
 * with a class. Used to generate automatic
 * sql queries.
 * <br><br>
 * 
 * <b>Important:</b><br>
 *
 * If a <code>Table</code> annotated class is extended the subclass inherits the 
 * annotation on the super class unless the subclass is also annotated with <code>Table</code>.
 * <br> 
 * This is a feature, it allows for example to override the <code>Table</code> annotation attributes in a subclass.
 * 
 * @see Column
 * @see EntityDescriptorTreeRepository
 * @see TargetMembers
 * @see TargetMembersHolder
 * 
 * @author Razvan Popian
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Table {
	
	/**
	 * @return the actual table of view name.  Can also be
	 * 			a SQL select statement.
	 */
	String value();
	
	/**
	 * @return {@code false} if the <code>value</code> of this annotation is a true
	 *         table, {@code true} if it is a view or a SQL select statement. The
	 *         default is {@code false}.<br>
	 *         The {@code Updater} implementation will not consider {@code Column}
	 *         annotated fields that belong to a class mapped to a view (ie. it has
	 *         this attribute set to {@code true}) even if they have their
	 *         {@link Column#insertable()} and/or {@link Column#updatable()}
	 *         attributes set to {@code true}. However the {@code Updater} will
	 *         consider any {@code Child} (foreign keys) annotated fields for
	 *         updating regardless of where they are declared.
	 * 
	 * @see Column#insertable()
	 * @see Column#updatable()
	 * @see Updater
	 * 
	 */
	boolean view() default false;
}
