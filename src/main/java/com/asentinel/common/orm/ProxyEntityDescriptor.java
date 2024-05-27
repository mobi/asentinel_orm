package com.asentinel.common.orm;

import java.lang.reflect.Member;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.orm.ed.tree.EntityDescriptorTreeRepository;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.proxy.entity.LazyLoadInterceptor;
import com.asentinel.common.orm.proxy.entity.ProxyFactory;
import com.asentinel.common.util.Assert;

/**
 * {@code EntityDescriptor} extension that will create a dynamic (ByteBuddy) proxy
 * for its target. This class is reusable and thread safe once configured.
 *
 * @see AutoLazyLoader
 * @see LazyLoadInterceptor
 * @see ProxyFactory
 * 
 * @author Razvan Popian
 */
public class ProxyEntityDescriptor<T> extends EntityDescriptor {
	private static final Logger log = LoggerFactory.getLogger(ProxyEntityDescriptor.class);
	
	public ProxyEntityDescriptor(SqlBuilderFactory sqlBuilderFactory, 
			Class<T> clazz, Member targetMember, String fkName, String tableName) {
		this(getLoader(sqlBuilderFactory, clazz, tableName),
				clazz, targetMember, fkName);
	}
	
	public ProxyEntityDescriptor(Function<Object, T> loader, 
			Class<T> clazz, Member targetMember, String fkName) {
		super(clazz, fkName, targetMember);
		Assert.assertNotNull(loader, "loader");
		
		RowMapper<?> entityRowMapper 
			= (rs, rowNum) -> ProxyFactory.getInstance().newProxy(clazz, loader);
			
		RowMapper<?> entityIdRowMapper = SimpleEntityDescriptor.Builder.getEntityIdRowMapper(clazz, fkName);
		
		init(entityIdRowMapper, entityRowMapper);
	}
	
	
	public static <T> Function<Object, T> getLoader(SqlBuilderFactory sqlBuilderFactory, 
			Class<T> clazz, String tableName) {
		Function<Object, T> loader;
		if (!StringUtils.hasText(tableName) 
				|| tableName.equalsIgnoreCase(SimpleEntityDescriptor.Builder.getTableName(clazz))) {
			loader = id -> sqlBuilderFactory.newSqlBuilder(clazz)
					.select().where().id().eq(id)
					.execForEntity();
		} else {
			if (log.isDebugEnabled()) {
				log.debug("getLoader - The table name is overridden in the @Child annotation for class " + clazz);
			}
			EntityDescriptorTreeRepository edtr = sqlBuilderFactory.getEntityDescriptorTreeRepository();
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(
				new SimpleNode(new SimpleEntityDescriptor.Builder(clazz).tableName(tableName).build())
			);
			loader = id -> sqlBuilderFactory.newSqlBuilder(clazz)
					.select(root).where().id().eq(id)
					.execForEntity();
		}
		return loader;
	}


	@Override
	public String toString() {
		return "ProxyEntityDescriptor [fk=" + getName() + ", class=" + getEntityClass().getName() + "]";
	}

}
