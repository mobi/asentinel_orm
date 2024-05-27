package com.asentinel.common.orm.mappers;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.asentinel.common.orm.AutoEagerLoader;
import com.asentinel.common.orm.AutoLazyLoader;
import com.asentinel.common.orm.Entity;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.FetchType;
import com.asentinel.common.orm.JoinType;
import com.asentinel.common.orm.ManyToManyQueryReady;
import com.asentinel.common.orm.QueryReady;
import com.asentinel.common.orm.RelationType;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.orm.TargetMembers;
import com.asentinel.common.orm.TargetMembersHolder;
import com.asentinel.common.orm.ed.tree.EntityDescriptorTreeRepository;

/**
 * Annotation used to mark an entity member of a class (either a field or a method
 * with one parameter) as being a child of that class. The methods in {@link EntityDescriptorTreeRepository}
 * class can be used to create {@link EntityDescriptor} trees by recursively traversing the class hierarchy
 * denoted by the <code>Child</code> annotations.
 * 
 * <br><br>
 * 
 * <b>Important:</b><br>
 *
 * If a <code>Child</code> annotated method is overridden in a subclass, any <code>Child</code> 
 * annotation on the method in the super class is inherited unless the subclass method is also annotated with <code>Child</code>.
 * <br> 
 * This is a feature, it allows for example to override the <code>Child</code> annotation attributes in a subclass.
 * 
 * @see EntityDescriptorTreeRepository
 * @see TargetMembers
 * @see TargetMembersHolder
 * 
 * @author Razvan Popian
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Child {
	
	/**
	 * @see EntityDescriptor#getName()
	 */
	String name() default "";
	
	/**
	 * @see EntityDescriptor#getEntityClass()
	 */
	Class<?> type() default Entity.class;
	
	/**
	 * @see SimpleEntityDescriptor#getPkName()
	 */
	String pkName() default "";
	
	/**
	 * Sets the name of the foreign key, if left empty the ORM
	 * will assume it is the same as the name of the primary key.
	 * 
	 * @see {@link SimpleEntityDescriptor#getFkName()}
	 */
	String fkName() default "";
	
	/**
	 * @see SimpleEntityDescriptor#getTableName()
	 */
	String tableName() default "";
	
	/**
	 * @see SimpleEntityDescriptor#getTableAlias()
	 * 
	 * @deprecated the use of explicit table aliases is strongly discouraged. You should
	 * 			let the ORM framework assign an alias for this child's table and reference this alias 
	 * 			using {@code SqlBuilder#alias(Object...)} or {@code SqlBuilder#rootAlias()}.
	 */
	String tableAlias() default "";
	
	/**
	 * @see SimpleEntityDescriptor#getColumnAliasSeparator()
	 */
	String columnAliasSeparator() default "";
	
	/**
	 * @see SimpleEntityDescriptor#getParentRelationType()
	 */
	RelationType parentRelationType() default RelationType.ONE_TO_MANY;
	
	/**
	 * @see SimpleEntityDescriptor#getParentJoinType()
	 */
	JoinType parentJoinType() default JoinType.LEFT;
	
	/**
	 * This property determines how the annotated member will be loaded. There are
	 * 2 options: 
	 * <li> {@code FetchType#EAGER} will cause a SQL join to the table mapped to the field and the field
	 * 		will be populated as soon as the SQL is executed and the results are processed. This is the default.
	 * <li> {@code FetchType#LAZY} will create a proxy that will be assigned to the annotated field. A 
	 * 		SQL query to load the field will be triggered when a method other than the entity id getter/setter
	 * 		or {@code Object#toString()} is called on the proxy.
	 * <br><br>
	 * The fetch behavior can be overridden at runtime using the {@link AutoLazyLoader} or {@link AutoEagerLoader}
	 * classes.
	 * 
	 * @return the kind of fetch to use for the annotated field, by default
	 * 			the field is eagerly loaded with a SQL join to the parent.
	 * 
	 * @see AutoLazyLoader
	 * @see AutoEagerLoader
	 */
	FetchType fetchType() default FetchType.EAGER;
	
	/**
	 * This property is taken into account only if the parent relation is
	 * {@link RelationType#ONE_TO_MANY}.
	 * 
	 * @return {@code true} if for a one to many parent relation the 
	 * 			foreign key of the child is available in the parent, {@code false}
	 * 			otherwise. The foreign key may not be available in some special cases,
	 * 			like for example if the parent-child join is not standard by id. This 
	 * 			property may be set to {@code false} if {@link #joinConditionsOverride()}
	 * 			is used.<br>
	 * 			{@code true} is the default.
	 * 
	 */
	boolean parentAvailableFk() default true;
	
	String manyToManyTable() default "";
		
	String manyToManyTableAlias() default "";
		
	String manyToManyLeftFkName() default "";
	
	String manyToManyRightFkName() default "";
	
	/**
	 * @see QueryReady#getJoinConditionsOverride()
	 * @see QueryReady
	 * @see QueryReady#PLACEHOLDER_DEFAULT_JOIN_CONDITION
	 * @see QueryReady#PLACEHOLDER_PARENT_TABLE_ALIAS
	 * @see QueryReady#PLACEHOLDER_CHILD_TABLE_ALIAS
	 */
	String joinConditionsOverride() default "";
	
	/**
	 * @see ManyToManyQueryReady#getManyToManyLeftJoinConditionsOverride()
	 * @see ManyToManyQueryReady
	 * @see QueryReady#PLACEHOLDER_DEFAULT_JOIN_CONDITION
	 * @see QueryReady#PLACEHOLDER_PARENT_TABLE_ALIAS
	 * @see ManyToManyQueryReady#PLACEHOLDER_MTM_TABLE_ALIAS
	 */
	String manyToManyLeftJoinConditionsOverride() default "";
	
	/**
	 * @see ManyToManyQueryReady#getManyToManyRightJoinConditionsOverride()
	 * @see ManyToManyQueryReady
	 * @see QueryReady#PLACEHOLDER_DEFAULT_JOIN_CONDITION
	 * @see QueryReady#PLACEHOLDER_CHILD_TABLE_ALIAS
	 * @see ManyToManyQueryReady#PLACEHOLDER_MTM_TABLE_ALIAS
	 */
	String manyToManyRightJoinConditionsOverride() default "";
	
	/**
	 * @see QueryReady#isForceManyAsOneInPaginatedQueries() 
	 */
	boolean forceManyAsOneInPaginatedQueries() default false;
	
}
