package com.asentinel.common.orm;

import java.lang.reflect.Member;
import java.util.Collection;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.orm.ed.tree.EntityDescriptorTreeRepository;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.proxy.collection.CollectionLazyLoadInterceptor;
import com.asentinel.common.orm.proxy.entity.ProxyFactory;

/**
 * {@code EntityDescriptor} extension that will create a dynamic (ByteBuddy) proxy
 * for its target {@code Collection} or {@code Map}. This class is reusable and 
 * thread safe once configured.
 * 
 * @see AutoLazyLoader
 * @see CollectionLazyLoadInterceptor
 * @see ProxyFactory
 * 
 * @author Razvan Popian
 */
public class CollectionProxyEntityDescriptor extends EntityDescriptor {
	private static final Logger log = LoggerFactory.getLogger(CollectionProxyEntityDescriptor.class);
	
	private final Function<Object, Collection<?>> loader;
	
	public CollectionProxyEntityDescriptor(SqlBuilderFactory sqlBuilderFactory, 
			Class<?> clazz, Member targetMember, String fkName, String tableName,
			String manyToManyRightFkName, String manyToManyTable) {
		this(getLoader(sqlBuilderFactory, clazz, fkName, tableName, manyToManyRightFkName, manyToManyTable),
				clazz, targetMember, fkName);
	}


	public CollectionProxyEntityDescriptor(Function<Object, Collection<?>> loader, 
			Class<?> clazz, Member targetMember, String fkName) {
		super(clazz, fkName, targetMember);
		// we do not initialize the super class row mappers, because we do not need them
		this.loader = loader;
	}
	
	public Function<Object, Collection<?>> getLoader() {
		return loader;
	}

	/*
	 * package private so that we can easily call it in tests.
	 */
	static Function<Object, Collection<?>> getLoader(SqlBuilderFactory sqlBuilderFactory, 
			Class<?> clazz, String fkName, String tableName) {
		return getLoader(sqlBuilderFactory, clazz, fkName, tableName, null, null);
	}

	
	/*
	 * package private so that we can easily call it in tests.
	 */
	static Function<Object, Collection<?>> getLoader(SqlBuilderFactory sqlBuilderFactory, 
			Class<?> clazz, String fkName, String tableName, 
			String manyToManyRightFkName, String manyToManyTable) {
		Function<Object, Collection<?>> loader;
		if (StringUtils.hasText(manyToManyTable)) {
			// MANY TO MANY
			if (!StringUtils.hasText(tableName) 
					|| tableName.equalsIgnoreCase(SimpleEntityDescriptor.Builder.getTableName(clazz))) {
				loader = id -> sqlBuilderFactory.newSqlBuilder(clazz)
						.select().where().id().in().lp()
							.sql("select " + manyToManyRightFkName + " from " + manyToManyTable
									+ " where " + fkName + " = ?",
							id)
						.rp()
						.exec();
			} else {
				if (log.isDebugEnabled()) {
					log.debug("getLoader - The table name is overridden in the @Child annotation for class " + clazz);
				}
				loader = (id) -> sqlBuilderFactory.newSqlBuilder(clazz)
						.select(getTableOverrideRootNode(sqlBuilderFactory, clazz, tableName))
						.where().id().in().lp()
							.sql("select " + manyToManyRightFkName + " from " + manyToManyTable
									+ " where " + fkName + " = ?",
							id)
						.rp()
						.exec();
			}
		} else {
			// MANY TO ONE
			if (!StringUtils.hasText(tableName) 
					|| tableName.equalsIgnoreCase(SimpleEntityDescriptor.Builder.getTableName(clazz))) {
				loader = id -> sqlBuilderFactory.newSqlBuilder(clazz)
						.select().where().column(fkName).eq(id)
						.exec();
			} else {
				if (log.isDebugEnabled()) {
					log.debug("getLoader - The table name is overridden in the @Child annotation for class " + clazz);
				}
				loader = id -> sqlBuilderFactory.newSqlBuilder(clazz)
						.select(getTableOverrideRootNode(sqlBuilderFactory, clazz, tableName))
						.where().column(fkName).eq(id)
						.exec();
			}
		}
		return loader;
	}
	
	private static Node<EntityDescriptor> getTableOverrideRootNode(SqlBuilderFactory sqlBuilderFactory, Class<?> clazz, String tableName) {
		EntityDescriptorTreeRepository edtr = sqlBuilderFactory.getEntityDescriptorTreeRepository();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(
			new SimpleNode(new SimpleEntityDescriptor.Builder(clazz).tableName(tableName).build())
		);
		return root;
	}
	
	@Override
	public String toString() {
		return "CollectionProxyEntityDescriptor [fk=" + getName() + ", class=" + getEntityClass().getName() + "]";
	}
	

}
