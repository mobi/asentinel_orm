package com.asentinel.common.orm;

import java.lang.reflect.Member;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import com.asentinel.common.orm.mappers.ColumnRowMapper;
import com.asentinel.common.orm.mappers.IntRowMapper;
import com.asentinel.common.orm.mappers.LongRowMapper;
import com.asentinel.common.orm.mappers.StringRowMapper;
import com.asentinel.common.orm.query.SqlFactory;
import com.asentinel.common.util.Assert;

import static com.asentinel.common.orm.EntityUtils.*;

/**
 * <code>EntityDescriptor</code> subclass that extracts entities from an entity list
 * provided on construction instead of the <code>ResultSet</code>. This descriptor will only
 * read the id of the entity from the <code>ResultSet</code> and use that to extract the actual
 * object from its internal cache.<br>
 * 
 * This object is reusable, assuming that the entity id {@code RowMapper} provided on construction
 * is reusable. Instances of this class should be used as effectively immutable objects.
 * 
 * @see EntityBuilder
 * @see SqlFactory
 * @see #forIntPk(Class, RowMapper, String)
 * @see #forLongPk(Class, RowMapper, String)
 * @see #forStringPk(Class, RowMapper, String)
 * @see #forColumnPk(Class, RowMapper, String)
 * 
 * @author Razvan Popian
 */
public class CacheEntityDescriptor extends EntityDescriptor {
	
	// Implementation note: The entity builder will read twice the id of the cached entities from the resultset.
	// This goes against the ResultSet contract that requires that a column must be read only once for each row. 
	// However this double read approach works on most databases.
	
	/**
	 * Constructor.
	 * @param clasz the class of the entity that will be built for this descriptor.
	 * @param entityIdRowMapper the mapper for the primary key.
	 * @param name the name of this entity descriptor
	 * @param entities the collection of entities that will be cached internally.
	 */
	public CacheEntityDescriptor(
			Class<?> clasz, 
			RowMapper<?> entityIdRowMapper,
			Object name,
			Collection<?> entities
		) {
		this(clasz, entityIdRowMapper, name, entities, null);
	}

	/**
	 * Constructor.
	 * @param clasz the class of the entity that will be built for this descriptor.
	 * @param entityIdRowMapper the mapper for the primary key.
	 * @param name the name of this entity descriptor
	 * @param entities the collection of entities that will be cached internally.
	 * @param targetMember the member of the parent entity that will be used to add/set the
	 * 			entity resulted from this entity descriptor.
	 */
	public CacheEntityDescriptor(
			Class<?> clasz, 
			RowMapper<?> entityIdRowMapper,
			Object name,
			Collection<?> entities,
			Member targetMember
		) {
		super(clasz, entityIdRowMapper, new CacheEntityRowMapper(entityIdRowMapper, entities), name, targetMember);
	}
	
	
	@Override
	public String toString() {
		return "CacheEntityDescriptor [name=" + getName() + ", class=" + getEntityClass().getName() + "]";
	}
	
	
	// static factory methods

	/**
	 * @see EntityDescriptor#forIntPk(Class, RowMapper, String)
	 */
	public static CacheEntityDescriptor forIntPk(Class<?> clazz, String pkColumnName, Collection<?> entities) {
		return new CacheEntityDescriptor(clazz, new IntRowMapper(pkColumnName, true), pkColumnName, entities);
	}
	
	/**
	 * @see EntityDescriptor#forIntPk(Class, RowMapper, String)
	 */
	public static CacheEntityDescriptor forIntPk(Class<?> clazz, String pkColumnName, Collection<?> entities, Member targetMember) {
		return new CacheEntityDescriptor(clazz, new IntRowMapper(pkColumnName, true), pkColumnName, entities, targetMember);
	}

	/**
	 * @see EntityDescriptor#forLongPk(Class, RowMapper, String)
	 */
	public static CacheEntityDescriptor forLongPk(Class<?> clazz, String pkColumnName, Collection<?> entities) {
		return new CacheEntityDescriptor(clazz, new LongRowMapper(pkColumnName, true), pkColumnName, entities);
	}

	/**
	 * @see EntityDescriptor#forLongPk(Class, RowMapper, String)
	 */
	public static CacheEntityDescriptor forLongPk(Class<?> clazz, String pkColumnName, Collection<?> entities, Member targetMember) {
		return new CacheEntityDescriptor(clazz, new LongRowMapper(pkColumnName, true), pkColumnName, entities, targetMember);
	}

	/**
	 * @see EntityDescriptor#forStringPk(Class, RowMapper, String)
	 */
	public static CacheEntityDescriptor forStringPk(Class<?> clazz, String pkColumnName, Collection<?> entities) {
		return new CacheEntityDescriptor(clazz, new StringRowMapper(pkColumnName, true), pkColumnName, entities);
	}

	/**
	 * @see EntityDescriptor#forStringPk(Class, RowMapper, String)
	 */
	public static CacheEntityDescriptor forStringPk(Class<?> clazz, String pkColumnName, Collection<?> entities, Member targetMember) {
		return new CacheEntityDescriptor(clazz, new StringRowMapper(pkColumnName, true), pkColumnName, entities, targetMember);
	}

	/**
	 * @see EntityDescriptor#forColumnPk(Class, RowMapper, String)
	 */
	public static CacheEntityDescriptor forColumnPk(Class<?> clazz, String pkColumnName, Collection<?> entities) {
		return new CacheEntityDescriptor(clazz, new ColumnRowMapper(pkColumnName), pkColumnName,  entities);
	}
	
	/**
	 * @see EntityDescriptor#forColumnPk(Class, RowMapper, String)
	 */
	public static CacheEntityDescriptor forColumnPk(Class<?> clazz, String pkColumnName, Collection<?> entities, Member targetMember) {
		return new CacheEntityDescriptor(clazz, new ColumnRowMapper(pkColumnName), pkColumnName,  entities, targetMember);
	}
	
	
	// helper inner classes
	
	private static class CacheEntityRowMapper implements RowMapper<Object> {
		private final RowMapper<?> idRowMapper;
		private final Map<Object, Object> entities = new HashMap<Object, Object>();
		
		
		public CacheEntityRowMapper(RowMapper<?> idRowMapper, Collection<?> entities) {
			Assert.assertNotNull(idRowMapper, "idRowMapper");
			Assert.assertNotNull(entities, "entities");
			this.idRowMapper = idRowMapper;
			for (Object entity: entities) {
				this.entities.put(getEntityId(entity), entity);
			}
		}
		
		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			Object id = idRowMapper.mapRow(rs, rowNum);
			return entities.get(id);
		}
	}

}
