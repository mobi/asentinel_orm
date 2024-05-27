package com.asentinel.common.orm;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.sql.ResultSet;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.orm.mappers.ColumnRowMapper;
import com.asentinel.common.orm.mappers.IntRowMapper;
import com.asentinel.common.orm.mappers.LongRowMapper;
import com.asentinel.common.orm.mappers.StringRowMapper;
import com.asentinel.common.util.Assert;

/**
 * This class describes an entity bean. The following information must be passed in 
 * on construction: <br><br>
 * 
 *	<li> {@code name} - the name of this entity descriptor, ideally it should be unique for each 
 * 		entity inside an entity descriptor tree, but this is not an absolute requirement.
 * 		<br> 
 * 		It is recommended that the name is at least unique among the children of a node so the name 
 * 		can be used as a unique identifier for each child. This is useful if the parent of the 
 * 		entity described by this node implements {@link ParentEntity} and in the {@code ParentEntity#addChild(Object, EntityDescriptor)}
 * 		method we need to determine for which child {@code EntityDescriptor} was the entity created.
 * <br><br>
 *	<li> {@code clazz} - the class of the entities that should be created for this descriptor.<br><br>
 *  <li> {@code entityIdRowMapper} - a {@link RowMapper} used for extracting the entity id (primary key) 
 *  					from the {@link ResultSet}. The primary key can be a composite key (multiple columns key).
 *  					This mapper must always return objects that correctly implement {@link Object#equals(Object)}
 *  					and {@link Object#hashCode()} because the entities will be cached in maps by the 
 *  					{@link EntityBuilder} class. The key of the map will be the entity primary key.
 *  					This mapper should not attempt to extract other fields than those that compose the primary key.
 *						This is to be consistent with the {@link ResultSet} java doc that states that a getXXX method 
 *						should not be called multiple times for the same column. <br><br>
 *  <li> {@code entityRowMapper} - a {@link RowMapper} used for extracting the entity fields, other than the primary
 *  					key fields. This mapper should not attempt to extract primary key fields. This is to be consistent 
 *  					with the {@link ResultSet} java doc that states that a getXXX method should not be called multiple 
 *  					times for the same column.<br><br>
 *  <li> {@code targetMember} - an optional field representing a member of the parent entity that will be used by the {@link EntityBuilder}
 *  					to add/set the entity created for this descriptor into its parent. If this member is not set the {@link EntityBuilder}
 *  					expects the parent entity to implement the {@link ParentEntity} interface.<br><br>
 *  
 * For a usage example see the {@link EntityBuilder} java doc.<br>
 * EntityDescriptor objects are reusable, assuming the {@code RowMappers} that they encapsulate are also reusable. They should be used as
 * effectively immutable objects.
 * 
 * @see EntityBuilder
 * @see Entity
 * @see ParentEntity
 * @see #forIntPk(Class, RowMapper, String)
 * @see #forLongPk(Class, RowMapper, String)
 * @see #forStringPk(Class, RowMapper, String)
 * @see #forColumnPk(Class, RowMapper, String)
 * 
 * @author Razvan Popian
 */
public class EntityDescriptor {
	
	private final Object name;
	private final Class<?> clazz;
	
	/** 
	 * The member (method or field) to be used for setting the entities created by this class.
	 * This field is optional, if it is null  the {@link EntityBuilder} will assume the target
	 * implements {@link Parent}.
	 */
	private final Member targetMember;
	
	private RowMapper<?> entityIdRowMapper;
	private RowMapper<?> entityRowMapper;
	

	/**
	 * See the class java doc. 
	 */
	public EntityDescriptor(Class<?> clazz, 
			RowMapper<?> entityIdRowMapper,
			RowMapper<?> entityRowMapper,
			Object name
			) {
		this(clazz, entityIdRowMapper, entityRowMapper, name, null);
	}
	
	
	/**
	 * See the class java doc. 
	 */
	public EntityDescriptor(Class<?> clazz, 
			RowMapper<?> entityIdRowMapper,
			RowMapper<?> entityRowMapper,
			Object name,
			Member targetMember
			) {
		this(clazz, name, targetMember);
		init(entityIdRowMapper, entityRowMapper);
	}

	/**
	 * Constructor to be used by sub classes that can not call one of the other
	 * constructors directly because the {@code RowMappers} are not yet built. Should be used
	 * with the {@link #init(RowMapper, RowMapper)} method.
	 */
	protected EntityDescriptor(Class<?> clazz, Object name, Member targetMember) {
		Assert.assertNotNull(name, "name");
		Assert.assertNotNull(clazz, "clazz");
		this.name = name;
		this.clazz = clazz;
		if (targetMember != null) {
			// allow access to the member regardless of its access modifier
			if (targetMember instanceof Field) {
				ReflectionUtils.makeAccessible((Field) targetMember);
			} else if (targetMember instanceof Method) {
				ReflectionUtils.makeAccessible((Method) targetMember);
			} else {
				throw new IllegalArgumentException("The member must be either a field or method.");
			}
		}
		this.targetMember = targetMember;
	}
	
	
	/**
	 * Init method to be called by subclasses , usually on construction.
	 */
	protected final void init(RowMapper<?> entityIdRowMapper, RowMapper<?> entityRowMapper) {
		Assert.assertNotNull(entityIdRowMapper, "entityIdRowMapper");
		Assert.assertNotNull(entityRowMapper, "entityRowMapper");
		this.entityIdRowMapper = entityIdRowMapper;
		this.entityRowMapper = entityRowMapper;
	}
	
	/**
	 * @return the name of this {@code EntityDescriptor}. It is recommended
	 * 		that this name is unique among the children of a certain node. The client
	 * 		is free to choose whatever type he wants for this field.
	 */
	public Object getName() {
		return name;
	}

	public Class<?> getEntityClass() {
		return clazz;
	}

	
	/**
	 * @return the {@link RowMapper} used to create primary key objects.
	 */
	public RowMapper<?> getEntityIdRowMapper() {
		return entityIdRowMapper;
	}

	/**
	 * @return the {@link RowMapper} used to create entity objects.
	 */
	public RowMapper<?> getEntityRowMapper() {
		return entityRowMapper;
	}
	
	/** 
	 * @return the member (method or field) to be used for adding the entities created by this descriptor
	 * 			to its parent. If it is {@code null}  the {@link EntityBuilder} will assume the target
	 * 			implements {@link Parent} and it will try to add the entities resulted for this descriptor
	 * 			using {@link ParentEntity#addChild(Object, EntityDescriptor)}.
	 */
	public final Member getTargetMember() {
		return targetMember;
	}

	@Override
	public String toString() {
		return "EntityDescriptor [name=" + name + ", class=" + getEntityClass().getName() + "]";
	}
	
	
	// static factory methods

	/**
	 * Factory method.
	 * @param clazz the class for which the descriptor contains meta info. 
	 * @param entityRowMapper the mapper for the entity. This mapper should not map the
	 * 			primary key field.
	 * @param pkColumnName the name of the primary key column.
	 * @return <code>EntityDescriptor</code> that describes an entity whose entityId (primary key) is
	 * 		an int table column. The entity id row mapper used by this class will call 
	 * 		{@link ResultSet#getInt(String)} with argument columnName to extract 
	 * 		the value of the column.   
	 */
	public static EntityDescriptor forIntPk(
			Class<?> clazz, 
			RowMapper<?> entityRowMapper,
			String pkColumnName
			) {
		return new EntityDescriptor(clazz, new IntRowMapper(pkColumnName, true), entityRowMapper, pkColumnName);
	}

	/**
	 * Factory method.
	 * @param clazz the class for which the descriptor contains meta info.
	 * @param entityRowMapper the mapper for the entity. This mapper should not map the
	 * 			primary key field.
	 * @param pkColumnName the name of the primary key column.
	 * @return EntityDescriptor that describes an entity whose entityId (primary key) is
	 * 		a long table column. The entity id row mapper used by this class will call 
	 * 		{@link ResultSet#getLong(String)} with argument columnName to extract	 
	 * 		the value of the column.
	 */
	public static EntityDescriptor forLongPk( 
			Class<?> clazz, 
			RowMapper<?> entityRowMapper,
			String pkColumnName
			) {
		return new EntityDescriptor(clazz, new LongRowMapper(pkColumnName, true), entityRowMapper, pkColumnName);
	}

	/**
	 * Factory method.
	 * @param clazz the class for which the descriptor contains meta info.
	 * @param entityRowMapper the mapper for the entity. This mapper should not map the
	 * 			primary key field.
	 * @param pkColumnName the name of the primary key column.
	 * @return EntityDescriptor that describes an entity whose entityId (primary key) is
	 * 		a string table column. The entity id row mapper used by this class will call 
	 * 		{@link ResultSet#getObject(String)} with argument columnName to extract	 
	 * 		the value of the column.
	 */
	public static EntityDescriptor forStringPk( 
			Class<?> clazz, 
			RowMapper<?> entityRowMapper,
			String pkColumnName
			) {
		return new EntityDescriptor(clazz, new StringRowMapper(pkColumnName, true), entityRowMapper, pkColumnName);
	}
	
	
	/**
	 * Factory method.
	 * @param clazz the class for which the descriptor contains meta info.
	 * @param entityRowMapper the mapper for the entity. This mapper should not map the
	 * 			primary key field.
	 * @param pkColumnName the name of the primary  key column. 
	 * @return EntityDescriptor that describes an entity whose entityId (primary key) is
	 * 		a single table column. The column type is not specified.The entity id row mapper 
	 * 		used by this class will call  {@link ResultSet#getObject(String)} with 
	 * 		argument columnName to extract the value of the column.
	 */
	public static EntityDescriptor forColumnPk( 
			Class<?> clazz, 
			RowMapper<?> entityRowMapper,
			String pkColumnName
			) {
		return new EntityDescriptor(clazz, new ColumnRowMapper(pkColumnName), entityRowMapper, pkColumnName);
	}

}
