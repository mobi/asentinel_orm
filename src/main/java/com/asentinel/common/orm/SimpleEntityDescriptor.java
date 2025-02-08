package com.asentinel.common.orm;

import static com.asentinel.common.jdbc.ConversionSupport.isPreparedForProxyingInputStreams;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.StringUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.jdbc.ConversionSupport;
import com.asentinel.common.jdbc.ObjectFactory;
import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.orm.mappers.AnnotationRowMapper;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;
import com.asentinel.common.orm.query.SqlFactory;
import com.asentinel.common.util.Assert;

/**
 * <code>EntityDescriptor</code> subclass that adds meta information to what
 * the <code>EntityDescriptor</code> contains, so that sql queries can be constructed
 * from a tree (root node) of <code>SimpleEntityDescriptor</code>. To create
 * SQL queries the class referenced by this descriptor must be annotated with
 * {@link Table}, the primary key column (only single column PKs are supported) must be
 * annotated with {@link PkColumn} and the fields that need to be fetched must be annotated
 * with {@link Column}. <br> 
 * 
 * To build the SQL query use the query building methods found in the {@link SqlFactory} interface.
 * 
 * @see SqlFactory#buildQuery(Node)
 * @see SqlFactory#buildQuery(QueryCriteria)
 * @see SqlFactory#buildPaginatedQuery(QueryCriteria)
 * @see QueryReady
 * @see AnnotationRowMapper
 * @see EntityBuilder
 * @see RelationType
 * @see JoinType 
 * 
 * @author Razvan Popian
 */
public class SimpleEntityDescriptor extends EntityDescriptor implements QueryReady {
	private final static Logger log = LoggerFactory.getLogger(SimpleEntityDescriptor.class);
	
	final static String DEFAULT_COLUMN_ALIAS_SEPARATOR = "_";
	
	/** Used for query generation only */
	private final String pkName;
	
	/** Used for query generation only */
	private final String fkName;
	
	/** Used for query generation only */
	private final String tableName;
	
	/** Used BOTH for query generation and for mapping columns to fields. */
	private final String tableAlias;
	
	/** Used BOTH for query generation and for mapping columns to fields. */
	private final String columnAliasSeparator;
	
	/** Used for query generation only */
	private final RelationType parentRelationType;
	
	/** Used for query generation only */
	private final JoinType parentJoinType;
	
	/** Factory for the entity, used if the entity can not be instantiated using a no-args contructor */
	private final ObjectFactory<?> entityFactory;
	
	private final String joinConditionsOverride; 
	private final List<Object> joinConditionsOverrideParams;
	
	private final boolean forceManyAsOneInPaginatedQueries;
	
	private final Map<String, UnaryOperator<String>> formulas;
	
	
	/**
	 * Constructor for this class, it has only the entity 
	 * class as argument. The relation to the parent is defaulted to 
	 * {@link RelationType#ONE_TO_MANY} and a left or inner join will be used 
	 * to join this descriptor to its parent. Use the {@link Builder} for this 
	 * class if you want to override default values.
	 *   
	 * @param clazz the class of the <code>Entity</code>.
	 */
	public SimpleEntityDescriptor(Class<?> clazz) {
		this(new Builder(clazz).preBuild());
	}
	
	/**
	 * Builder constructor.
	 */
	protected SimpleEntityDescriptor(Builder builder) {
		super(builder.clazz, builder.entityIdRowMapper, builder.mapper, builder.name, 
				builder.targetMember);
		
		this.parentRelationType = builder.parentRelationType;
		this.parentJoinType = builder.parentJoinType;
		this.tableName = builder.tableName;
		this.tableAlias = builder.tableAlias;
		this.pkName = builder.pkName;
		this.fkName = builder.fkName;
		this.columnAliasSeparator = builder.columnAliasSeparator;
		this.entityFactory = builder.entityFactory;
		
		// set join condition override and additional override params
		this.joinConditionsOverride = builder.joinConditionsOverride;
		this.joinConditionsOverrideParams = builder.joinConditionsOverrideParams;
		
		this.forceManyAsOneInPaginatedQueries = builder.forceManyAsOneInPaginatedQueries;
		
		this.formulas = builder.formulas;
	}
	
	

	/**
	 * @return the name of the primary key, this is extracted
	 * 			in the constructor from the <code>Entity</code> object
	 * 			by searching for the {@link PkColumn} annotation.
	 * 
	 * @see SqlFactory#buildQuery(Node)
	 */
	@Override
	public String getPkName() {
		return pkName;
	}
	

	/**
	 * @return the name of the foreign key, it can be specified on
	 * 		construction, if left {@code null} the {@link SqlFactory#buildQuery(Node)}
	 * 		method will assume it is the same as the primary key.
	 * 
	 * @see SqlFactory#buildQuery(Node)
	 */
	@Override
	public String getFkName() {
		return fkName;
	}

	/**
	 * @return the name of the table this descriptor
	 * 			is associated with.
	 * 
	 * @see SqlFactory#buildQuery(Node)
	 */
	@Override
	public String getTableName() {
		return tableName;
	}

	/**
	 * @return the alias of the table from which
	 * 			data will be fetched for this descriptor. It is
	 * 			used by the {@link SqlFactory#buildQuery(Node)} method to create
	 * 			the sql query and also by the entity row mapper encapsulated 
	 * 			in this class to create the column aliases.
	 * 
	 * @see SqlFactory#buildQuery(Node)
	 */
	@Override
	public String getTableAlias() {
		return tableAlias;
	}
	
	/**
	 * @return the column alias separator, the default is {@value #DEFAULT_COLUMN_ALIAS_SEPARATOR}.
	 */
	@Override
	public String getColumnAliasSeparator() {
		return columnAliasSeparator;
	}

	/**
 	 * @return relation type of this descriptor to its parent. By default the
 	 * 			value of this field is {@link RelationType#ONE_TO_MANY}.
 	 * 
 	 * @see #getParentJoinType()
 	 * @see RelationType
 	 * @see SqlFactory
	 */
	@Override
	public RelationType getParentRelationType() {
		return parentRelationType;
	}

	/**
 	 * @return the type of join to link the table represented
 	 * 			by this descriptor to the table represented by its
 	 * 			parent. By default the value of this field is {@link JoinType#LEFT}.<br>
 	 * 			Note the following:
 	 * 			<li> if this method returns {@link JoinType#RIGHT} the default will be used (right join is not
 	 * 			supported).
 	 * 			<li> if the parent relation type is  is many to one ({@link RelationType#MANY_TO_ONE}) or 
 	 * 			many to many ({@link RelationType#MANY_TO_MANY}) the query building methods in {@link SqlFactory} 
 	 * 			will ignore the value returned by this method and will default to left join.
 	 * 			<li> if the parent relation type is one to many ({@link RelationType#ONE_TO_MANY}) or  one to one
 	 * 			{@link RelationType#ONE_TO_ONE} the query building methods in {@link SqlFactory} will treat the value 
 	 * 			returned by this method as a hint. They will determine based on the current <code>EntityDescriptor</code> 
 	 * 			hierarchy, whether applying the requested join type is appropriate. 
 	 * 
 	 * @see #getParentRelationType()
 	 * @see RelationType
 	 * @see SqlFactory
	 */
	@Override
	public JoinType getParentJoinType() {
		return parentJoinType;
	}
	
	
	
	/**
	 * @return the list of column names extracted from the target class 
	 * 			based on annotated setters or fields.
	 */
	@Override
	public Collection<String> getColumnNames() {
		// TODO 01: the collection of columns should pe precalculated ?
		// TODO 02: This method has more or less the same logic as the
		// method TargetMembers#getUpdatableColumns. We should have only one place for column
		// lists calculations.
		Class<?> entityClass = getEntityClass();
		TargetMembers targetMembers = TargetMembersHolder.getInstance()
				.getTargetMembers(entityClass);
		List<TargetMember> columnMembers = targetMembers.getColumnMembers();
		List<TargetChildMember> childMembers = targetMembers.getChildMembers();
		Map<String, String> columnNames = new LinkedHashMap<>();
		columnNames.put(getPkName().toLowerCase(), 
				getPkName());
		// @Column fields have priority over the @Child fields for foreign keys
		for (TargetMember member: columnMembers) {
			String colName = ((Column) member.getAnnotation()).value(); 
			if (colName == null) {
				continue;
			}
			if (isPreparedForProxyingInputStreams(member.getMemberClass(), getEntityRowMapper())) {
				// InputStreams should be created as proxies, so we don't select the column
				// The above condition has to work together with the ConversionSupport InputStream
				// conversion code.
				continue;
			}
			if (columnNames.putIfAbsent(colName.toLowerCase(), colName) != null) {
				if (log.isDebugEnabled()) {
					log.debug("getColumnNames - Column '" + colName  + "' already mapped to another field in class " + entityClass.getName() + ". "
							+ "Only the first field will be considered a selectable column.");
				}
			}
		}
		// we need any fk column from the @Child members that have ONE_TO_MANY parent relation
		// for lazy loading. See AutoLazyLoader
		for (TargetChildMember member: childMembers) {
			Child childAnn = (Child) member.getAnnotation();
			if (childAnn.parentRelationType() == RelationType.ONE_TO_MANY) {
				if (!childAnn.parentAvailableFk()) {
					continue;
				}
				String fkName = member.getFkNameForOneToMany();
				if (StringUtils.hasText(fkName)) {
					if (columnNames.putIfAbsent(fkName.toLowerCase(), fkName) != null) {
						if (log.isDebugEnabled()) {
							log.debug("getColumnNames - Column '" + fkName  + "' already mapped to another field in class " + entityClass.getName() + ". "
									+ "Only the first field will be considered a selectable column.");
						}
					}
				}
			}
		}
		return columnNames.values();
	}
	
	@Override
	public UnaryOperator<String> getFormula(String columnName) {
		if (formulas == null) {
			return null;
		}
		return formulas.get(columnName.toLowerCase());
	}
	
	@Override
	public String getJoinConditionsOverride() {
		return joinConditionsOverride;
	}

	@Override
	public List<Object> getJoinConditionsOverrideParams() {
		return joinConditionsOverrideParams;
	}
	
	@Override
	public boolean isForceManyAsOneInPaginatedQueries() {
		return forceManyAsOneInPaginatedQueries;
	}
	
	/**
	 * @return the factory used to create each entity resulted from
	 * 			this entity descriptor. This method can return <code>null</code>,
	 * 			in this case a default object factory is used when
	 * 			entities are constructed.
	 */
	public ObjectFactory<?> getEntityFactory() {
		return entityFactory;
	}

	@Override
	public String toString() {
		return "SimpleEntityDescriptor ["
				+ "fk=" + getFkName()
				+ ", pk=" + getPkName()
				+ ", class=" + getEntityClass().getName() 
				+ ", parentRelationType=" + getParentRelationType()
				+ "]";
	}

	
	
	// builder for instances of this class
	
	public static class Builder implements Cloneable {
		protected Class<?> clazz;
		protected Object name;
		protected String pkName;
		protected String fkName;
		protected String tableName;
		protected String tableAlias;
		protected String columnAliasSeparator;
		protected RelationType parentRelationType;
		protected JoinType parentJoinType;
		
		protected ObjectFactory<?> entityFactory;
		
		protected Member targetMember;
		
		protected String joinConditionsOverride; 
		protected List<Object> joinConditionsOverrideParams;
		protected boolean forceManyAsOneInPaginatedQueries;
		
		protected Map<String, UnaryOperator<String>> formulas = emptyMap();		
		
		private SqlQuery queryEx;
		private LobHandler lobHandler;
		private RowMapper<?> entityIdRowMapper;
		private AnnotationRowMapper<?> mapper;

		/**
		 * Builder constructor.
		 * @param entityClass the class of the objects that will be created
		 * 			for this <code>EntityDescriptor</code>
		 */
		public Builder(Class<?> entityClass) {
			entityClass(entityClass);
		}
		
		/**
		 * Creates a shallow copy of this builder. Useful for scenarios where we don't
		 * want to commit to calling {@link #build()} on the original object as
		 * {@code build()} may affect the state of this builder and we don't want that.
		 * Instead we can clone the builder and call {@code build()} on the clone.
		 * 
		 * @see Cloneable
		 */
		@Override
		public Builder clone() {
			try {
				return (Builder) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException("Unexpected clone failure.", e);
			}
		}
		
		/**
		 * @see EntityDescriptor#getEntityClass()
		 */
		public final Builder entityClass(Class<?> entityClass) {
			Assert.assertNotNull(entityClass, "entityClass");
			this.clazz = entityClass;
			
			// we clear the table name so that it is picked up from the entityClass parameter,
			// see #preBuild()
			this.tableName = null;
			return this;
		}

		// builder methods
		
		/**
		 * @see SimpleEntityDescriptor#getName()
		 */
		public Builder name(Object name) {
			this.name = name;
			return this;
		}

		/**
		 * @see SimpleEntityDescriptor#getPkName()
		 */
		public Builder pkName(String pkName) {
			this.pkName = pkName;
			return this;
		}

		/**
		 * @see SimpleEntityDescriptor#getFkName()
		 */
		public Builder fkName(String fkName) {
			this.fkName = fkName;
			return this;
		}
		
		/**
		 * @see SimpleEntityDescriptor#getTableName()
		 */
		public Builder tableName(String tableName) {
			this.tableName = tableName;
			return this;
		}
		
		/**
		 * @see SimpleEntityDescriptor#getTableAlias()
		 */
		public Builder tableAlias(String tableAlias) {
			this.tableAlias = tableAlias;
			return this;
		}

		/**
		 * @see SimpleEntityDescriptor#getColumnAliasSeparator()
		 */
		public Builder columnAliasSeparator(String columnAliasSeparator) {
			this.columnAliasSeparator = columnAliasSeparator;
			return this;
		}
		
		/**
		 * @see SimpleEntityDescriptor#getParentRelationType()
		 */
		public Builder parentRelationType(RelationType parentRelationType) {
			this.parentRelationType = parentRelationType;
			return this;
		}

		/**
		 * @see SimpleEntityDescriptor#getParentJoinType()
		 */
		public Builder parentJoinType(JoinType parentJoinType) {
			this.parentJoinType = parentJoinType;
			return this;
		}

		/**
		 * @see SimpleEntityDescriptor#getParentJoinType()
		 */
		public Builder parentInnerJoin() {
			return parentJoinType(JoinType.INNER);
		}
		
		
		/**
		 * Adds an entity factory for the objects created by this <code>EntityDescriptor</code>.
		 * The objects created by the factory MUST have the class used for the construction of
		 * this builder. If a custom {@code AnnotationRowMapper} is provided the factory is ignored.
		 */
		public Builder entityFactory(ObjectFactory<?> entityFactory) {
			this.entityFactory = entityFactory;
			return this;
		}
		
		/**
		 * Sets a custom {@code AnnotationRowMapper}, useful if we want to extend the
		 * {@code AnnotationRowMapper} and add some custom behavior like support for
		 * columns not known at compile time (dynamic columns, custom columns etc)
		 */
		public Builder mapper(AnnotationRowMapper<?> mapper) {
			this.mapper = mapper;
			return this;
		}

		public Builder entityIdRowMapper(RowMapper<?> entityIdRowMapper) {
			this.entityIdRowMapper = entityIdRowMapper;
			return this;
		}

		
		/**
		 * Sets the member (method or field) to be used for setting the entities
		 * generated by this {@link SimpleEntityDescriptor}.
		 */
		public Builder targetMember(Member member) {
			this.targetMember = member;
			return this;
		}
		
		/**
		 * @see QueryReady#getJoinConditionsOverride()
		 */
		public Builder joinConditionsOverride(String joinConditionsOverride) {
			this.joinConditionsOverride = joinConditionsOverride;
			return this;
		}
		

		/**
		 * @see QueryReady#getJoinConditionsOverrideParams()
		 */
		public Builder joinConditionsOverrideParam(Object joinConditionsOverrideParam) {
			if (joinConditionsOverrideParams == null) {
				joinConditionsOverrideParams = new ArrayList<>();
			}
			joinConditionsOverrideParams.add(joinConditionsOverrideParam);
			return this;
		}
		
		/**
		 * @see QueryReady#isForceManyAsOneInPaginatedQueries()
		 */
		public Builder forceManyAsOneInPaginatedQueries(boolean forceManyAsOneInPaginatedQueries) {
			this.forceManyAsOneInPaginatedQueries = forceManyAsOneInPaginatedQueries;
			return this;
		}

		/**
		 * Adds a map of formulas. The map maps column names to {@code UnaryOperators}
		 * ({@link Formula}s). This allows rendering of SQL code that includes formulas
		 * in the columns list. For example the following snippet built and included in
		 * an entity descriptor tree:
		 * 
		 * <pre>
		 * SimpleEntityDescriptor.Builder b = ....;
		 * b.formulas("Total", new Formula("%s + 10"));
		 * </pre>
		 * 
		 * will render a SQL query like {@code select ..., Total + 10, ... from ...}
		 * 
		 * @param columnNamesToFormulas the map of formulas
		 * @return this builder.
		 * 
		 * @see Formula
		 * @see QueryReady#getFormula(String)
		 */
		public Builder formulas(Map<String, ? extends UnaryOperator<String>> columnNamesToFormulas) {
			if (columnNamesToFormulas == null) {
				return this;
			}
			this.formulas = columnNamesToFormulas.entrySet().stream()
					.collect(toMap(e -> e.getKey().toLowerCase(), Entry::getValue));
			return this;
		}
		
		/**
		 * Sets the {@code SqlQuery} that will be used for proxying
		 * {@code InputStreams}. If this is {@code null} input streams will be eagerly
		 * loaded.
		 * @see ConversionSupport
		 * @see ConversionSupport#isPreparedForProxyingInputStreams(Class, RowMapper)
		 */
		public Builder queryExecutor(SqlQuery queryEx) {
			this.queryEx = queryEx;
			return this;
		}
		
		public Builder lobHandler(LobHandler lobHandler) {
			this.lobHandler = lobHandler;
			return this;
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Builder preBuild() {
			if (parentRelationType == null) {
				// TODO: the default parent relation type should be determined
				// based on the annotated target element. However this is not an easy
				// job because we have code that uses the @Child parentRelationType value.
				// See SimpleEntityDescriptor#getColumnNames. The logic for determining the 
				// default parent relation type should be moved in TargetChildMember, we should
				// no longer have code like childAnn.parentRelationType() == RelationType.ONE_TO_MANY
				this.parentRelationType = RelationType.ONE_TO_MANY;
			}
			if (parentJoinType == null) {
				this.parentJoinType = JoinType.LEFT;
			}
			if (!StringUtils.hasText(tableName)) {
				this.tableName = getTableName(clazz);
			}
			if (!StringUtils.hasText(tableAlias)) {
				this.tableAlias = QueryUtils.nextTableAlias();
			}
			if (!StringUtils.hasText(pkName)) {
				this.pkName = getPkName(clazz);
			}
			if (!StringUtils.hasText(fkName)) {
				// default is null, should be interpreted as the same name as the primary key
				// of the relationship
				this.fkName = null;
			}
			if (name == null 
					|| (name instanceof String && !StringUtils.hasText((String) name))) {
				this.name = pkName; 
			}
			if (!StringUtils.hasText(columnAliasSeparator)) {
				this.columnAliasSeparator = DEFAULT_COLUMN_ALIAS_SEPARATOR;
			}
			
			// init mandatory super class fields
			if (entityIdRowMapper == null) {
				String mapperPkName = tableAlias + columnAliasSeparator + pkName;
				this.entityIdRowMapper = getEntityIdRowMapper(clazz, mapperPkName);
			}
			
			if (mapper == null) {
				if (entityFactory != null) {
					this.mapper = new AnnotationRowMapper(entityFactory, true, tableAlias + columnAliasSeparator);
				} else {
					this.mapper = new AnnotationRowMapper(clazz, true, tableAlias + columnAliasSeparator);	
				}
				this.mapper.setQueryExecutor(queryEx);
				this.mapper.setLobHandler(lobHandler);
			}
			
			return this;
		}
		
		/**
		 * @return a {@code SimpleEntityDescriptor}. Note that prior to creating the
		 *         {@code SimpleEntityDescriptor} this method will initialize any
		 *         uninitialized fields in this builder with default values. This has
		 *         side effects on the state of the builder.
		 * @see #clone()
		 */
		public SimpleEntityDescriptor build() {
			return new SimpleEntityDescriptor(preBuild());
		}
		
		// static helper methods
		
		static String getTableName(Class<?> clazz) {
			Table tableAnn = TargetMembersHolder.getInstance().getTargetMembers(clazz).getTableAnnotation();
			if (tableAnn == null) {
				throw new IllegalArgumentException("Can not find the table name for class  " + clazz.getName() 
						+ ". It does not have the @Table annotation ?");
			}
			return tableAnn.value();
		}
		
		private static String getPkName(Class<?> cls) {
			TargetMembers targetMembers = TargetMembersHolder.getInstance().getTargetMembers(cls);
			TargetMember targetMember = targetMembers.getPkColumnMember();
			if (targetMember == null) {
				throw new IllegalArgumentException("No member is annotated with @PkColumn in class " + cls.getName() + ".");
			}
			PkColumn pkColumnAnn = (PkColumn) targetMember.getAnnotation();
			return pkColumnAnn.value();
		}
		
		static Class<?> getPkClass(Class<?> cls) {
			TargetMembers targetMembers = TargetMembersHolder.getInstance().getTargetMembers(cls);
			TargetMember targetMember = targetMembers.getPkColumnMember();
			if (targetMember == null) {
				throw new IllegalArgumentException("No member is annotated with @PkColumn in class " + cls.getName() + ".");
			}
			return targetMember.getMemberClass();
		}
		
		static RowMapper<?> getEntityIdRowMapper(Class<?> entityClass, String mapperPkName) {
			Class<?> pkClass = getPkClass(entityClass);
			return EntityUtils.getEntityIdRowMapper(pkClass, mapperPkName);
		}
		

		// getters to inspect the current status of the properties
		
		public Class<?> getEntityClass() {
			return clazz;
		}

		public Object getName() {
			return name;
		}

		public String getPkName() {
			return pkName;
		}

		public String getFkName() {
			return fkName;
		}

		public String getTableName() {
			return tableName;
		}

		public String getTableAlias() {
			return tableAlias;
		}

		public String getColumnAliasSeparator() {
			return columnAliasSeparator;
		}

		public RelationType getParentRelationType() {
			return parentRelationType;
		}

		public JoinType getParentJoinType() {
			return parentJoinType;
		}

		public ObjectFactory<?> getEntityFactory() {
			return entityFactory;
		}

		public Member getTargetMember() {
			return targetMember;
		}

		public String getJoinConditionsOverride() {
			return joinConditionsOverride;
		}

		public List<Object> getJoinConditionsOverrideParams() {
			return Collections.unmodifiableList(joinConditionsOverrideParams);
		}

		public LobHandler getLobHandler() {
			return lobHandler;
		}

		public SqlQuery getQueryEx() {
			return queryEx;
		}
		
		@Override
		public String toString() {
			return "Builder [clazz=" + (clazz == null ? null : clazz.getName()) + "]";
		}
	}

}
