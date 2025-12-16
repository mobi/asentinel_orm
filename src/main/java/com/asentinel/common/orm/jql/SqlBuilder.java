package com.asentinel.common.orm.jql;

import static com.asentinel.common.orm.jql.InstructionType.*;
import static com.asentinel.common.orm.jql.InstructionType.COLUMN;
import static com.asentinel.common.orm.jql.InstructionType.COLUMN_ALIAS;
import static com.asentinel.common.orm.jql.InstructionType.FROM_QUERY;
import static com.asentinel.common.orm.jql.InstructionType.ID_COLUMN;
import static com.asentinel.common.orm.jql.InstructionType.INITIAL_QUERY;
import static com.asentinel.common.orm.jql.InstructionType.PAGED_HAVING;
import static com.asentinel.common.orm.jql.InstructionType.PAGED_INITIAL_QUERY;
import static com.asentinel.common.orm.jql.InstructionType.PAGED_MAIN_ORDER_BY;
import static com.asentinel.common.orm.jql.InstructionType.PAGED_MAIN_WHERE;
import static com.asentinel.common.orm.jql.InstructionType.PAGED_SECONDARY_WHERE;
import static com.asentinel.common.orm.jql.InstructionType.PAGED_USE_GROUP_BY;
import static com.asentinel.common.orm.jql.InstructionType.PARAM;
import static com.asentinel.common.orm.jql.InstructionType.PATH;
import static com.asentinel.common.orm.jql.InstructionType.PATH_ROOT;
import static com.asentinel.common.orm.jql.InstructionType.SEPARATOR;
import static com.asentinel.common.orm.jql.InstructionType.SQL;
import static com.asentinel.common.orm.jql.InstructionType.STRING;

import java.lang.annotation.Annotation;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.jdbc.RowAsArrayRowMapper;
import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.arrays.Array;
import com.asentinel.common.orm.EntityBuilder;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.EntityDescriptorNodeCallback;
import com.asentinel.common.orm.EntityDescriptorNodeMatcher;
import com.asentinel.common.orm.EntityDescriptorUtils;
import com.asentinel.common.orm.QueryCriteria;
import com.asentinel.common.orm.QueryReady;
import com.asentinel.common.orm.QueryUtils;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.orm.ed.tree.EntityDescriptorTreeRepository;
import com.asentinel.common.orm.query.SqlFactory;
import com.asentinel.common.util.Assert;

/**
 * Builder class that is able to compile a set of instructions into pure SQL. Instances of this class
 * are not reusable. Also make sure you understand that <b>it is up to you to call the builder methods in the
 * proper order</b>. No validation of the methods call order is performed.
 * <br> 
 * There are 4 ways to work with the builder:
 * <li>
 * The final result of the build process can be a {@link CompiledSql} object, to get that you have to call the various builder
 * methods and then call the {@link #compileAsIs(String)} method. Here is an example:
 * <pre>
 * SqlBuilderFactory sqlBuilderFactory = ....;
 * SqlBuilder sqlBuilder = sqlBuilderFactory.newSqlBuilder(Invoice.class);
 * CompiledSql cSql = sqlBuilder
 * 			.where()
 * 			.id().eq().param(17031977)
 * 			.compileAsIs("root_alias");
 * </pre>
 * Assuming the <code>Invoice</code> class is properly annotated, the {@link CompiledSql} instance in the above example will hold
 * a SQL string like the following <code>where t0.InvoiceId = ?</code> and also a numeric parameter <code>17031977</code>. The resulting
 * SQL is not valid in this case, but it can be appended to a valid <code>select</code> statement.
 * 
 * <br><br>
 * 
 * <li>
 * The final result of the build process can be a list of objects resulted from actually executing the compiled SQL query. See below:
 * <pre>
 * SqlBuilderFactory sqlBuilderFactory = ....;
 * SqlBuilder sqlBuilder = sqlBuilderFactory.newSqlBuilder(Invoice.class);
 * List<Invoice> invoices = sqlBuilder
 * 			.select()
 * 			.where()
 * 			.id().eq().param(17031977)
 * 			.exec();
 * </pre>
 * 
 * Assuming the <code>Invoice</code> class is properly annotated, the invoice list in the above example will hold
 * an <code>Invoice</code> instance resulted from executing a query similar to the following <code>select * from where t0.InvoiceId = ?</code>.
 * 
 * <br><br>
 * 
 * <li>
 * The final result of the build process can be a {@link Page} object resulted from executing a paginated query built using the <code>paged</code>
 * prefixed methods:
 * 
 * <pre>
 * 		SqlBuilderFactory sqlBuilderFactory = ....;
 * 		SqlBuilder sqlBuilder = sqlBuilderFactory.newSqlBuilder(Invoice.class);
 * 		Page<Invoice> page = sqlBuilderFactory.pagedSelect(0, 10)
 *			.pagedEnableGroupBy()
 *			.pagedWhere().id().gt(17031977)
 *			.pagedOrderBy().id().desc()
 *			.execForPage();
 * </pre>
 * 
 * Assuming the <code>Invoice</code> class is properly annotated, the <code>Page</code> object in the above example will hold
 * a list containing maximum 10 elements and the number of invoices that have the id greater than <code>17031977</code>.
 * 
 * <br><br>
 * 
 * <li>
 * The final result of the build process can be a list of arbitrary objects (non-entity) resulted from actually executing the compiled SQL query using
 * the {@link #exec(RowMapper)} method and the other {@code exec} methods related to it. See the example below:
 * 
 * <pre>
 * 	SqlBuilderFactory sqlBuilderFactory = ....;
 * 	SqlBuilder sqlBuilder = sqlBuilderFactory.newSqlBuilder(Invoice.class);
 *	List<String> list = sqlBuilderFactory
 *		.selectK()
 *		.column("LocationNo")
 *		.from().where().id().gt(1000)
 *		.execForString();
 *
 * </pre>
 * 
 * 
 * @see CompiledSql
 * @see Page
 * @see EntityDescriptor
 * @see SimpleEntityDescriptor
 * @see QueryReady
 * @see EntityDescriptorTreeRepository
 * @see SqlFactory
 * @see EntityBuilder
 * 
 * @author Razvan Popian
 */
public class SqlBuilder<E> {
	private static final Logger log = LoggerFactory.getLogger(SqlBuilder.class);
	
	private static final Object[] NO_PARAMS = new Object[0];

    static final String NO_RESULTS = "1 = 0";

	private final EntityDescriptorTreeRepository entityDescriptorTreeRepository;
	private final SqlFactory sqlFactory;
	private final SqlQuery queryExecutor;
	
	private final Class<E> clasz;
	private final Instructions instructions;
	
	// TODO 01: add secondary order by method secondaryOrderBy, @see QueryCriteria
	
	/**
	 * Package private constructor.
	 * 
	 * @see SqlBuilderFactory
	 */
	SqlBuilder(Class<E> clasz,
			EntityDescriptorTreeRepository entityDescriptorTreeRepository,
			SqlFactory sqlFactory,
			SqlQuery queryExecutor) {
		Assert.assertNotNull(clasz, "clasz");
		this.clasz = clasz;
		this.entityDescriptorTreeRepository = entityDescriptorTreeRepository;
		this.sqlFactory = sqlFactory;
		this.queryExecutor = queryExecutor;
		this.instructions = new Instructions(sqlFactory);
	}

	/**
	 * Constructor.
	 * @param clasz the object type that will result from executing the built query.
	 * 
	 * @deprecated use the {@code SqlBuilderFactory} and its default implementation
	 * 			{@link DefaultSqlBuilderFactory} to create {@code SqlBuilder}s. 
	 */
	@Deprecated
	public SqlBuilder(Class<E> clasz) {
		this(clasz, 
				EntityDescriptorUtils.getEntityDescriptorTreeRepository(),
				QueryUtils.getSqlFactory(),
				null
		);
	}
	
	protected Instructions getInstructions() {
		return instructions;
	}
	
	// not used
	final void reset() {
		instructions.clear();
	}
	
	/**
	 * @see #select(EntityDescriptorNodeCallback...)
	 */
	public SqlBuilder<E> select() {
		return select((EntityDescriptorNodeCallback[]) null);
	}
	
	/**
	 * Creates the <code>select</code> part of the query using the provided parameters. It delegates to
	 * {@link EntityDescriptorTreeRepository} methods to create the {@link EntityDescriptor} tree.
	 */
	public SqlBuilder<E> select(EntityDescriptorNodeCallback ... nodeCallbacks) {
		select(
				entityDescriptorTreeRepository.getEntityDescriptorTree(clasz, nodeCallbacks)
		);
		return this;
	}
	
	/**
	 * Creates the <code>select</code> part of the query using the provided parameters. It delegates to
	 * {@link EntityDescriptorTreeRepository} methods to create the {@link EntityDescriptor} tree.
	 */
	public SqlBuilder<E> select(String rootTableAlias, EntityDescriptorNodeCallback ... nodeCallbacks) {
		select(
				entityDescriptorTreeRepository.getEntityDescriptorTree(clasz, rootTableAlias, nodeCallbacks)
		);
		return this;
	}

	/**
	 * Creates the <code>select</code> part of the query using the provided 
	 * {@link EntityDescriptor} tree.
	 */
	public SqlBuilder<E> select(Node<EntityDescriptor> root) {
		Assert.assertNotNull(root, "root");
		getInstructions().add(new Instruction(INITIAL_QUERY, root));		
		return this;
	}
	
	// -----------------------------------------------------------------------------------------------------
	
	public SqlBuilder<E> from() {
		return from((EntityDescriptorNodeCallback[]) null);
	}
	
	public SqlBuilder<E> from(EntityDescriptorNodeCallback ... nodeCallbacks) {
		Assert.assertNotNull(clasz, "clasz");
		from(
				entityDescriptorTreeRepository.getEntityDescriptorTree(clasz, nodeCallbacks)
		);
		return this;
	}
	
	public SqlBuilder<E> from(String rootTableAlias, EntityDescriptorNodeCallback ... nodeCallbacks) {
		Assert.assertNotNull(clasz, "clasz");
		from(
				entityDescriptorTreeRepository.getEntityDescriptorTree(clasz, rootTableAlias, nodeCallbacks)
		);
		return this;
	}

	public SqlBuilder<E> from(Node<EntityDescriptor> root) {
		Assert.assertNotNull(root, "root");
		getInstructions().add(new Instruction(FROM_QUERY, root));		
		return this;
	}
	
	// -----------------------------------------------------------------------------------------------------
	
	/**
	 * Compiles the instructions that were added to the builder. This method will work even if
	 * one of the {@code select} or {@code from} methods was not called.
	 * @param rootTableAlias the alias for the table corresponding to the root class.
	 * @return a {@link CompiledSql} instance that holds the compiled SQL query, any query parameters
	 * 			and the tree that was used for compilation.
	 * @see CompiledSql
	 */
	public CompiledSql compileAsIs(String rootTableAlias) {
		Node<EntityDescriptor> root = entityDescriptorTreeRepository.getEntityDescriptorTree(clasz, rootTableAlias);
		return compileAsIs(root);
	}

	/**
	 * Compiles the instructions that were added to the builder. This method will work even if
	 * one of the {@code select} or {@code from} methods was not called.
	 * @param root the <code>EntityDescriptor</code> tree to use for compilation.
	 * @return a {@link CompiledSql} instance that holds the compiled SQL query, any query parameters
	 * 			and the tree that was used for compilation.
	 * @see CompiledSql
	 */
	public CompiledSql compileAsIs(Node<EntityDescriptor> root) {
		return getInstructions().compile(root);
	}

	/**
	 * Compiles the instructions that were added to the builder. This method will only work if a 
	 * call was made to one of the {@code select} or {@code from} methods.
	 * @return a {@link CompiledSql} instance that holds the compiled SQL query, any query parameters
	 * 			and the tree that was used for compilation.
	 * @see CompiledSql
	 */
	public CompiledSql compile() {
		return getInstructions().compile();
	}
	
	/**
	 * Compiles and then executes the resulting query.
	 * @return the list of objects resulted from the SQL query.
	 */
	@Deprecated
	public List<E> exec(SqlQuery queryExecutor) {
		CompiledSql compiledSql = getInstructions().compile();
		EntityBuilder<E> eb = new EntityBuilder<>(compiledSql.getRootNode());
		queryExecutor.query(compiledSql.getSqlString(), eb, compiledSql.getParameters());
		return eb.getEntityList();
	}

	/**
	 * Compiles and then executes the resulting query.
	 * @return the list of objects resulted from the SQL query.
	 */
	public List<E> exec() {
		CompiledSql compiledSql = getInstructions().compile();
		EntityBuilder<E> eb = new EntityBuilder<>(compiledSql.getRootNode());
		queryExecutor.query(compiledSql.getSqlString(), eb, compiledSql.getParameters());
		return eb.getEntityList();
	}

	/**
	 * Compiles and then executes the resulting query.
	 * @return the map of objects resulted from the SQL query.
	 */
	@Deprecated
	public Map<Object, E> execForMap(SqlQuery queryExecutor) {
		CompiledSql compiledSql = getInstructions().compile();
		EntityBuilder<E> eb = new EntityBuilder<>(compiledSql.getRootNode());
		queryExecutor.query(compiledSql.getSqlString(), eb, compiledSql.getParameters());
		return eb.getEntityMap();
	}

	/**
	 * Compiles and then executes the resulting query.
	 * @return the map of objects resulted from the SQL query.
	 */
	public Map<Object, E> execForMap() {
		CompiledSql compiledSql = getInstructions().compile();
		EntityBuilder<E> eb = new EntityBuilder<>(compiledSql.getRootNode());
		queryExecutor.query(compiledSql.getSqlString(), eb, compiledSql.getParameters());
		return eb.getEntityMap();
	}

	/**
	 * Compiles and then executes the resulting query. If the query does not produce
	 * exactly 1 object, an exception is thrown.
	 * @return the object resulted from the SQL query. 
	 * @throws EmptyResultDataAccessException if no objects were produced
	 * @throws IncorrectResultSizeDataAccessException if more than 1 object
	 * 			was produced.
	 */
	@Deprecated	
	public E execForEntity(SqlQuery queryExecutor) {
		CompiledSql compiledSql = getInstructions().compile();
		EntityBuilder<E> eb = new EntityBuilder<>(compiledSql.getRootNode());
		queryExecutor.query(compiledSql.getSqlString(), eb, compiledSql.getParameters());
		return eb.getEntity();
	}

	/**
	 * Compiles and then executes the resulting query. If the query does not produce
	 * exactly 1 object an exception is thrown.
	 * @return the object resulted from the SQL query. 
	 * @throws EmptyResultDataAccessException if no objects were produced
	 * @throws IncorrectResultSizeDataAccessException if more than 1 object was produced.
	 */
	public E execForEntity() {
		CompiledSql compiledSql = getInstructions().compile();
		EntityBuilder<E> eb = new EntityBuilder<>(compiledSql.getRootNode());
		queryExecutor.query(compiledSql.getSqlString(), eb, compiledSql.getParameters());
		return eb.getEntity();
	}

	/**
	 * Compiles and then executes the resulting query. If the query does not produce
	 * any result, it will return an empty {@link Optional}. If the query produces more than 
	 * 1 object, an exception is thrown. 
	 * @return an {@link Optional} containing the object resulted from the SQL query, or
	 * 		   an empty {@link Optional} in case no object is produced
	 * @throws IncorrectResultSizeDataAccessException if more than 1 object was produced.
	 * 
	 * @see SqlBuilder#execForEntity()
	 */
	public Optional<E> execForOptional() {
		E entity;
		try {
			entity = execForEntity();
		} catch (EmptyResultDataAccessException e) {
			entity = null;
		}
		return Optional.ofNullable(entity);
	}
	
	// ----------------------- non entity result execs ----------------------------------------------
	
	/**
	 * Compiles and then executes the resulting query. Should be used with the {@code from } methods.
	 * @param queryExecutor the {@link SqlQuery} implementation to use for executing the encapsulated SQL string.
	 * @param mapper the {@link RowMapper} to use for processing the results.
	 * @return list of objects resulted from executing the query.
	 * 
	 * @see #from()
	 */
	@Deprecated
	public <T> List<T> exec(SqlQuery queryExecutor, RowMapper<T> mapper) {
		CompiledSql compiledSql = getInstructions().compile();
		return queryExecutor.query(compiledSql.getSqlString(), mapper, compiledSql.getParameters());
	}

	/**
	 * Compiles and then executes the resulting query. Should be used with the {@code from } methods.
	 * @param mapper the {@link RowMapper} to use for processing the results.
	 * @return list of objects resulted from executing the query.
	 * 
	 * @see #from()
	 */
	public <T> List<T> exec(RowMapper<T> mapper) {
		CompiledSql compiledSql = getInstructions().compile();
		return queryExecutor.query(compiledSql.getSqlString(), mapper, compiledSql.getParameters());
	}
	
	/**
	 * @see #exec(RowMapper)
	 * @see RowAsArrayRowMapper
	 */
	@Deprecated
	public List<Object[]> execForListOfArrays(SqlQuery queryExecutor) {
		CompiledSql compiledSql = getInstructions().compile();
		return queryExecutor.query(compiledSql.getSqlString(), new RowAsArrayRowMapper(), compiledSql.getParameters());
	}

	/**
	 * @see #exec(RowMapper)
	 * @see RowAsArrayRowMapper
	 */
	public List<Object[]> execForListOfArrays() {
		CompiledSql compiledSql = getInstructions().compile();
		return queryExecutor.query(compiledSql.getSqlString(), new RowAsArrayRowMapper(), compiledSql.getParameters());
	}

	/**
	 * @see #exec(SqlQuery, RowMapper)
	 */
	@Deprecated
	public int execForInt(SqlQuery queryExecutor) 
			throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException {
		CompiledSql compiledSql = getInstructions().compile();
		return queryExecutor.queryForInt(compiledSql.getSqlString(), compiledSql.getParameters());
	}

	/**
	 * @see #exec(RowMapper)
	 */
	public int execForInt() 
			throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException {
		CompiledSql compiledSql = getInstructions().compile();
		return queryExecutor.queryForInt(compiledSql.getSqlString(), compiledSql.getParameters());
	}
	
	/**
	 * @see #exec(SqlQuery, RowMapper)
	 */
	@Deprecated
	public long execForLong(SqlQuery queryExecutor) 
			throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException {
		CompiledSql compiledSql = getInstructions().compile();
		return queryExecutor.queryForLong(compiledSql.getSqlString(), compiledSql.getParameters());
	}

	/**
	 * @see #exec(RowMapper)
	 */
	public long execForLong() 
			throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException {
		CompiledSql compiledSql = getInstructions().compile();
		return queryExecutor.queryForLong(compiledSql.getSqlString(), compiledSql.getParameters());
	}
	
	/**
	 * @see #exec(SqlQuery, RowMapper)
	 */
	@Deprecated
	public String execForString(SqlQuery queryExecutor) 
			throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException {
		CompiledSql compiledSql = getInstructions().compile();
		return queryExecutor.queryForString(compiledSql.getSqlString(), compiledSql.getParameters());
	}

	/**
	 * @see #exec(RowMapper)
	 */
	public String execForString() 
			throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException {
		CompiledSql compiledSql = getInstructions().compile();
		return queryExecutor.queryForString(compiledSql.getSqlString(), compiledSql.getParameters());
	}
	
	/**
	 * @see #exec(SqlQuery, RowMapper)
	 */
	@Deprecated
	public <T> T execForObject(SqlQuery queryExecutor, RowMapper<T> mapper) 
			throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException {
		CompiledSql compiledSql = getInstructions().compile();
		return queryExecutor.queryForObject(compiledSql.getSqlString(), mapper, compiledSql.getParameters());
	}

	/**
	 * @see #exec(RowMapper)
	 */
	public <T> T execForObject(RowMapper<T> mapper) 
			throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException {
		CompiledSql compiledSql = getInstructions().compile();
		return queryExecutor.queryForObject(compiledSql.getSqlString(), mapper, compiledSql.getParameters());
	}
	
	/**
	 * @see #exec(SqlQuery, RowMapper)
	 */
	@Deprecated
	public <T> T execForObject(SqlQuery queryExecutor, Class<T> clasz) 
			throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException {
		CompiledSql compiledSql = getInstructions().compile();
		return queryExecutor.queryForObject(compiledSql.getSqlString(), clasz, compiledSql.getParameters());
	}

	/**
	 * @see #exec(RowMapper)
	 */
	public <T> T execForObject(Class<T> clasz) 
			throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException {
		CompiledSql compiledSql = getInstructions().compile();
		return queryExecutor.queryForObject(compiledSql.getSqlString(), clasz, compiledSql.getParameters());
	}
	
	/**
	 * @see #exec(SqlQuery, RowMapper)
	 */
	@Deprecated
	public Object[] execForArray(SqlQuery queryExecutor) 
			throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException {	
		CompiledSql compiledSql = getInstructions().compile();
		return queryExecutor.queryForObject(compiledSql.getSqlString(), new RowAsArrayRowMapper(), compiledSql.getParameters());
	}

	/**
	 * @see #exec(RowMapper)
	 */
	public Object[] execForArray() 
			throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException {	
		CompiledSql compiledSql = getInstructions().compile();
		return queryExecutor.queryForObject(compiledSql.getSqlString(), new RowAsArrayRowMapper(), compiledSql.getParameters());
	}
	
	
	// ------------------- alias/path selectors --------------------
	
	/**
	 * Sets the path to an {@link EntityDescriptor} node that will be used by any subsequent
	 * column related builder methods to determine the table alias for the specified column.
	 * <br>
	 * If the path argument is <code>null</code> or <code>0</code> length the alias is set to the 
	 * root (ie. the {@link #rootAlias()} method is called). Note that the first element in the {@code path}
	 * can be {@code null}, in this case the root will always be matched.
	 * 
	 * @param path array of objects of type {@link Class}, {@link Annotation} or {@link EntityDescriptorNodeMatcher}.
	 * 		<br> 
	 * 		Here are 3 path examples:
	 * 		<li>
	 * 		{Charge.class, Bill.class, Invoice.class}
	 * 		<li>
	 * 		{Charge.class, new EntityDescriptorNodeMatcher(Bill.class, "special_alias"), Invoice.class}
	 * 		<li>
	 * 		{null, Bill.class, Invoice.class}
	 * 
	 * @see #rootAlias()
	 * @see EntityDescriptorUtils#getEntityDescriptor(Node, Object...)
	 */
	public SqlBuilder<E> alias(Object ... path) {
		if (path == null || path.length == 0) {
			return rootAlias();
		}
		getInstructions().add(new Instruction(PATH, path));
		return this;
	}
	
	/**
	 * Sets the path to the root {@link EntityDescriptor} node. This path will be used by any subsequent 
	 * column related builder methods to determine the table alias for the specified column.
	 * 
	 * @see #alias(Object...)
	 */
	public SqlBuilder<E> rootAlias() {
		getInstructions().add(new Instruction(PATH_ROOT, null));
		return this;
	}
	
	/**
	 * Sets the separator between the table alias and the column name. By default
	 * this is the dot {@code "."}, but if we reference columns in the secondary
	 * part of a paginated query we will need to change the separator to
	 * {@code "_"}.
	 * <br>
	 * Note that when this method is called, the separator set is kept until another
	 * call is made to this method.
	 * 
	 * @param separator
	 *            the separator to use
	 *
	 * @see #pagedSecondaryWhere()
	 */
	public SqlBuilder<E> sep(String separator) {
		Assert.assertNotEmpty(separator, "separator");
		getInstructions().add(new Instruction(SEPARATOR, separator));
		return this;
	}

	/**
	 * Logs the methods calls received so far.
	 */
	public SqlBuilder<E> log() {
		if (log.isDebugEnabled()) {
			log.debug("log - " + getInstructions());
		}
		return this;
	}
	
	// -------------------- sql keywords -----------------------
	
	private void addString(String s) {
		getInstructions().add(new Instruction(STRING, s));
	}
	
	/**
	 * Simple method that adds the <code>select</code> 
	 * keyword to this builder.
	 */
	public SqlBuilder<E> selectK() {
		addString(SELECT);
		return this;
	}

	public SqlBuilder<E> star() {
		addString(STAR);
		return this;
	}

	/**
	 * Simple method that adds the <code>from</code> 
	 * keyword to this builder.
	 */
	public SqlBuilder<E> fromK() {
		addString(FROM);
		return this;
	}
	
	public SqlBuilder<E> where() {
		addString(WHERE);
		return this;
	}
	
	public SqlBuilder<E> orderBy() {
		addString(ORDER_BY);
		return this;
	}
	
	public SqlBuilder<E> asc() {
		addString(ASC);
		return this;
	}
	
	public SqlBuilder<E> asc(Nulls nulls) {
		addString(ASC);
		addString(" ");
		addString(nulls.getKeyword());
		return this;
	}
	
	
	public SqlBuilder<E> desc() {
		addString(DESC);
		return this;
	}

	public SqlBuilder<E> desc(Nulls nulls) {
		addString(DESC);
		addString(" ");
		addString(nulls.getKeyword());
		return this;
	}

	
	public SqlBuilder<E> and() {
		addString(AND);
		return this;
	}

	public SqlBuilder<E> or() {
		addString(OR);
		return this;
	}

	public SqlBuilder<E> not() {
		addString(NOT);
		return this;
	}

	public SqlBuilder<E> eq() {
		addString(EQ);
		return this;
	}

	/*
	 * Equal operator. If the parameter is null it should be rendering "is null" for 
	 * selects, but "= null" for updates. The problem is that it's hard to distinguish
	 * these 2 cases with the current design.
	 */
	public SqlBuilder<E> eq(Object param) {
		return eq().param(param);
	}
	
	
	public SqlBuilder<E> ne() {
		addString(NE);
		return this;
	}

	/*
	 * NotEqual operator. Note that it does not render "is not null" for selects if
	 * the parameter is null to keep it in sync with the eq(Object) method. 
	 */
	public SqlBuilder<E> ne(Object param) {
		return ne().param(param);
	}
	
	public SqlBuilder<E> gt() {
		addString(GT);
		return this;
	}
	
	public SqlBuilder<E> gt(Object param) {
		return gt().param(param);
	}
	

	public SqlBuilder<E> lt() {
		addString(LT);
		return this;
	}
	
	public SqlBuilder<E> lt(Object param) {
		return lt().param(param);
	}
	
	
	public SqlBuilder<E> gte() {
		addString(GTE);
		return this;
	}
	
	public SqlBuilder<E> gte(Object param) {
		return gte().param(param);
	}
	

	public SqlBuilder<E> lte() {
		addString(LTE);
		return this;
	}
	
	public SqlBuilder<E> lte(Object param) {
		return lte().param(param);
	}
	
	public SqlBuilder<E> like() {
		addString(LIKE);
		return this;
	}
	
	public SqlBuilder<E> like(String param) {
		return like().param(param);
	}

	/**
	 * Performs case sensitive like.
	 * @see #applyLike(String, String, boolean)
	 */
	public SqlBuilder<E> applyLike(String column, String param) {
		return applyLike(column, param, true);
	}

	/**
	 * Adds a like condition to the query.
	 * @param column the column to apply the condition on.
	 * @param param the value of the like filter.
	 * @return this builder.
	 */
	public SqlBuilder<E> applyLike(String column, String param, boolean caseSensitive) {
		if (caseSensitive) {
			return column(column).like().param(param);	
		} else {
			return upperColumn(column).like().upperParam(param);
		}
	}
	
	/**
	 * Performs case sensitive starts with. 
	 * @see #applyStartsWith(String, String, boolean)
	 */
	public SqlBuilder<E> applyStartsWith(String column, String param) {
		return applyStartsWith(column, param, true);
	}

	/**
	 * Appends the <code>%</code> character to the string <code>param</code> and adds a 
	 * like condition to the query.
	 * @param column the column to apply the condition on.
	 * @param param the value of the like filter, if <code>null</code> an empty string will be used.
	 * @return this builder.
	 */
	public SqlBuilder<E> applyStartsWith(String column, String param, boolean caseSensitive) {
		if (param == null) {
			param = "";
		}
		return applyLike(column, param + "%", caseSensitive);
	}

	/**
	 * Appends the {@code %} character to the string {@code param} and adds as many like (starts with)
	 * conditions to the query as {@code columns}. 
	 * <p>
	 * Conditions are {@code or} separated.
	 * 
	 * @param param
	 * 			the value of the like filter, if {@code null} an empty string will be used
	 * @param caseSensitive
	 * 			{@code true} case sensitive, {@code false} case-insensitive
	 * @param columns
	 * 			the columns to apply the condition on, if {@code null}, nothing is appended
	 * @return
	 * 			the builder
	 */
	public SqlBuilder<E> applyMultipleStartsWith(String param, boolean caseSensitive, String... columns) {
		if (columns.length == 0) {
			return this;
		}
		
		this.lp();
		for (int i = 0; i < columns.length; i++) {
			if (i > 0) {
				this.or();
			}
			this.applyStartsWith(columns[i], param, caseSensitive);
		}
		return this.rp();
	}
	
	/**
	 * @see SqlBuilder#applyMultipleStartsWith(String, boolean, String...)
	 */
	public SqlBuilder<E> applyMultipleStartsWith(String param, String... columns) {
		return applyMultipleStartsWith(param, true, columns);
	}
	
	/**
	 * Performs case sensitive contains. 
	 * @see #applyContains(String, String, boolean)
	 */
	public SqlBuilder<E> applyContains(String column, String param) {
		return applyContains(column, param, true);
	}

	/**
	 * Appends a leading and a trailing <code>%</code> character to the string <code>param</code> and adds a 
	 * like condition to the query.
	 * @param column the column to apply the condition on.
	 * @param param the value of the like filter, if <code>null</code> an empty string will be used.
	 * @return this builder.
	 */
	public SqlBuilder<E> applyContains(String column, String param, boolean caseSensitive) {
		if (param == null) {
			param = "";
		}
		return applyLike(column, "%" + param + "%", caseSensitive);
	}
	
	/**
	 * Appends a leading and a trailing {@code %} character to the string {@code param} and 
	 * adds as many like (contains) conditions to the query as {@code columns}. 
	 * <p>
	 * Conditions are {@code or} separated.
	 * 
	 * @param param
	 * 			the value of the like filter, if {@code null} an empty string will be used
	 * @param caseSensitive
	 * 			{@code true} case sensitive, {@code false} case-insensitive
	 * @param columns
	 * 			the columns to apply the condition on, if {@code null}, nothing is appended
	 * @return
	 * 		the builder
	 */
	public SqlBuilder<E> applyMultipleContains(String param, boolean caseSensitive, String... columns) {
		if (columns.length == 0) {
			return this;
		}
		
		this.lp();
		for (int i = 0; i < columns.length; i++) {
			if (i > 0) {
				this.or();
			}
			this.applyContains(columns[i], param, caseSensitive);
		}
		return this.rp();
	}

	/**
	 * @see SqlBuilder#applyMultipleContains(String, boolean, String...)
	 */
	public SqlBuilder<E> applyMultipleContains(String param, String... columns) {
		return applyMultipleContains(param, true, columns);
	}
	
	public SqlBuilder<E> in() {
		addString(IN);
		return this;
	}
	
	/**
	 * Appends an <code>IN</code> condition to the sql.
	 * The resulted SQL may not yield good performance under certain conditions and with certain databases (Oracle), 
	 * consider using {@link #inObjects(String, Object...)} methods if the resulted query is slow.
	 * @param param the database {@link Array} to use.
	 * @return this builder.
	 * 
	 * @see #inObjects(String, Object...)
	 * @see #in(String, int...)
	 * @see #in(String, long...)
	 * @see #in(String, Collection)
	 * @see #idIn(Object...)
	 * @see #idIn(int...)
	 * @see #idIn(long...)
	 * @see #idIn(Collection)
	 */
	public SqlBuilder<E> in(Array param) {
		return sql(sqlFactory.getSqlTemplates().getSqlForInArray(), param);
	}
	
	/**
	 * Simulates the SQL <code>IN</code> condition using <code>ORs</code>.
	 * You may prefer this method to the {@link #in(Array)} method
	 * because it may yield better-performing SQL with certain databases (Oracle). However, the resulting SQL may be
	 * very long if the <code>params</code> collection is large.
	 * @param column the column name. If this is <code>null</code> the id column will be used.
	 * @param params the parameters.
	 * @return this builder.
	 * 
	 * @see #idIn(Collection)
	 */
	public SqlBuilder<E> in(String column, Collection<?> params) {
		if (params == null || params.isEmpty()) {
			// we do not return any result if params is null or 0 length  
			return sql(NO_RESULTS);
		}
		boolean first = true;
		this.lp();
		for (Object param: params) {
			if (first) {
				this.inHelper(column, param);
				first = false;
			} else {
				this.or().inHelper(column, param);
			}
		}
		return this.rp();
	}
	
	/**
	 * Works just like {@link #in(String, Collection)}, but for the id column.
	 * @param params the parameters.
	 * @return this builder.
	 * 
	 * @see #in(String, Collection)
	 */
	public SqlBuilder<E> idIn(Collection<?> params) {
		return in(null, params);
	}
	
	// INFO for maintainers: inObjects(String, Object[]) is not called simply in(String, Object[])
	// because the compiler will complain that a call like new SqlBuilder(...).in("test", 1, 2, 3)
	// is ambiguous
	/**
	 * @see #in(String, Collection)
	 */
	public SqlBuilder<E> inObjects(String column, Object ... params) {
		if (params == null || params.length == 0) {
			return in(column, (Collection<?>) null);
		}
		return this.in(column, Arrays.asList(params));
	}
	
	/**
	 * @see #inObjects(String, Object[])
	 */
	public SqlBuilder<E> idIn(Object ... params) {
		return this.inObjects(null, params);
	}
	
	
	/**
	 * @see #in(String, Collection)
	 */
	public SqlBuilder<E> in(String column, int ... params) {
		if (params == null || params.length == 0) {
			// we do not return any result if params is null or 0 length  
			return sql(NO_RESULTS);
		}
		this.lp()
			.inHelper(column, params[0]);
		for (int i = 1; i < params.length; i++) {
			this.or().inHelper(column, params[i]);
		}
		return this.rp();
	}
	
	/**
	 * @see #in(String, Collection)
	 */
	public SqlBuilder<E> idIn(int ... params) {
		return this.in(null, params);
	}

	/**
	 * @see #in(String, Collection)
	 */
	public SqlBuilder<E> in(String column, long ... params) {
		if (params == null || params.length == 0) {
			// we do not return any result if params is null or 0 length  
			return sql(NO_RESULTS);
		}
		this.lp()
			.inHelper(column, params[0]);
		for (int i = 1; i < params.length; i++) {
			this.or().inHelper(column, params[i]);
		}
		return this.rp();
	}
	
	/**
	 * @see #in(String, Collection)
	 */
	public SqlBuilder<E> idIn(long ... params) {
		return this.in(null, params);
	}
	
	private SqlBuilder<E> inHelper(String column, Object param) {
		if (column == null) {
			this.id();
		} else {
			this.column(column);
		}
		if (param != null) {
			return this.eq(param);
		} else {
			return this.is().nul();
		}
	}
	
	public SqlBuilder<E> is() {
		addString(IS);
		return this;
	}
	
	
	public SqlBuilder<E> nul() {
		addString(NULL);
		return this;
	}
	
	public SqlBuilder<E> lp() {
		addString(LP);
		return this;
	}

	public SqlBuilder<E> rp() {
		addString(RP);
		return this;
	}
	
	public SqlBuilder<E> comma() {
		addString(COMMA);
		return this;
	}
	
	public SqlBuilder<E> upper() {
		addString(UPPER);
		return this;
	}
	
	public SqlBuilder<E> upperParam(String param) {
		return upper().lp().param(param).rp();
	}
	
	public SqlBuilder<E> upperColumn(String column) {
		return upper().lp().column(column).rp();
	}
	
	public SqlBuilder<E> lower() {
		addString(LOWER);
		return this;
	}
	
	public SqlBuilder<E> lowerParam(String param) {
		return lower().lp().param(param).rp();
	}
	
	public SqlBuilder<E> lowerColumn(String column) {
		return lower().lp().column(column).rp();
	}
	
	
	public SqlBuilder<E> between() {
		addString(BETWEEN);
		return this;
	}
	
	public SqlBuilder<E> between(Object lo, Object hi) {
		return between().param(lo).and().param(hi);
	} 

	public SqlBuilder<E> exists() {
		addString(EXISTS);
		return this;
	} 
	
	public SqlBuilder<E> exists(String sql, Object ... params) {
		Assert.assertNotEmpty(sql, "sql");
		return exists().lp().sql(sql, params).rp();
	}
	
	public SqlBuilder<E> count() {
		addString(COUNT);
		return this;
	} 

	/**
	 * Renders a count instruction in the SQL string having as parameter the id column for the current 
	 * {@link EntityDescriptor} node.
	 * 
	 * @see #alias(Object...)
	 * @see #rootAlias()
	 */
	public SqlBuilder<E> countId() {
		return count().lp().id().rp();
	} 

	/**
	 * Renders a count instruction in the SQL string having as parameter the column with the
	 * specified name for the current {@link EntityDescriptor} node.
	 * 
	 * @see #alias(Object...)
	 * @see #rootAlias()
	 */
	public SqlBuilder<E> count(String column) {
		return count().lp().column(column).rp();
	} 
	
	/**
	 * Inserts plain SQL into this builder.
	 * @param sql the plain SQL code.
	 * @param params any parameters that the plain SQL requires.
	 */
	public SqlBuilder<E> sql(String sql, Object ... params) {
		if (!StringUtils.hasLength(sql)) {
			return this;
		}
		if (params == null) {
			params = NO_PARAMS;
		}
		getInstructions().add(new Instruction(SQL, new Object[]{" " + sql.trim(), params}));		
		return this;
	}
	
	/**
	 * Inserts the compiled sql received as parameter into this builder, including
	 * any parameters the {@code CompiledSql} may have.
	 *  
	 * @param compiledSql the {@code CompiledSql} instance.
	 * 
	 * @see #sql(String, Object...)
	 * @see CompiledSql
	 */
	public SqlBuilder<E> sql(CompiledSql compiledSql) {
		return this.sql(compiledSql.getSqlString(), compiledSql.getParameters());
	}

	public SqlBuilder<E> update() {
		addString(UPDATE);
		return this;
	}

	public SqlBuilder<E> set() {
		addString(SET);
		return this;
	}

	public SqlBuilder<E> delete() {
		addString(DELETE);
		return this;
	}
	
	/**
	 * Renders a SQL {@code coalesce} instruction for a column.
	 * @see #coalesce(String, Object, boolean)
	 */
	public SqlBuilder<E> coalesce(String column, Object valueIfNull) {
		return coalesce(column, valueIfNull, false);
	}

	/**
	 * Renders a SQL {@code coalesce} instruction for a column. Could also wrap the {@code coalesce}
	 * instruction in a {@code upper} SQL instruction based on the {@code upper} parameter.
	 * @param column the target column name
	 * @param valueIfNull a value to be used if the target column is {@code null}
	 * @param upper whether to apply {@code upper} or not. The column type should be text 
	 * 			if this is {@code true}
	 * @return this {@code SqlBuilder}
	 */
	public SqlBuilder<E> coalesce(String column, Object valueIfNull, boolean upper) {
		Assert.assertNotNull(valueIfNull, "valueIfNull");
		if (upper) {
			this.upper().lp();
		}
		addString(COALESCE);
		this
			.lp()
				.column(column)
				.comma()
				.param(valueIfNull)
			.rp();
		if (upper) {
			this.rp();
		}
		return this;
	}
	
	// -------------------- columns -----------------------

	/**
	 * References the id column for the current {@link EntityDescriptor} node.
	 * 
	 * @see #alias(Object...)
	 * @see #rootAlias()
	 */
	public SqlBuilder<E> id() {
		return this.id(null);
	}

	/**
	 * References the id column for the current {@link EntityDescriptor} node. This method
	 * should be used after a {@link #orderBy()} was issued and for string columns only.
	 * @param caseSensitive sort/search case-sensitive or not. If <code>null</code> this method behaves like {@link #id()}.
	 * 
	 * @see #alias(Object...)
	 * @see #rootAlias()
	 */
	public SqlBuilder<E> id(Boolean caseSensitive) {
		getInstructions().add(new Instruction(ID_COLUMN, caseSensitive));
		return this;
	}
	
	/**
	 * Renders the alias for the id for the current {@link EntityDescriptor} node.
	 */
	public SqlBuilder<E> idAlias() {
		getInstructions().add(new Instruction(ID_ALIAS, null));
		return this;
	}

	/**
	 * Renders the id and its alias for the current {@link EntityDescriptor} node.
	 */
	public SqlBuilder<E> idWithAlias() {
		return this.id().idAlias();
	}


	/**
	 * References the column with the specified name for the current {@link EntityDescriptor} node.
	 * @param column the name of the column.
	 * 
	 * @see #alias(Object...)
	 * @see #rootAlias()
	 */
	public SqlBuilder<E> column(String column) {
		return this.column(column, null);
	}

	/**
	 * References the column with the specified name for the current {@link EntityDescriptor} node. This method
	 * should be used after a {@link #orderBy()} was issued and for string columns only.
	 * @param column the name of the column.
	 * @param caseSensitive sort/search case-sensitive or not. If <code>null</code> this method behaves like {@link #column(String)}.
	 * 
	 * @see #alias(Object...)
	 * @see #rootAlias()
	 */
	public SqlBuilder<E> column(String column, Boolean caseSensitive) {
		Assert.assertNotEmpty(column, "column");
		getInstructions().add(new Instruction(COLUMN, 
				new Object[]{column.trim(), caseSensitive}));
		return this;
	}
	
	/**
	 * Renders the alias for the specified {@code column} name for the current
	 * {@link EntityDescriptor} node.
	 * 
	 * @param column the column name
	 */
	public SqlBuilder<E> columnAlias(String column) {
		Assert.assertNotEmpty(column, "column");
		getInstructions().add(new Instruction(COLUMN_ALIAS, column.trim()));
		return this;
	}

	/**
	 * Renders the column and its alias for the specified {@code column} name for
	 * the current {@link EntityDescriptor} node.
	 * 
	 * @param column the column name
	 */
	public SqlBuilder<E> columnWithAlias(String column) {
		return this.column(column).columnAlias(column);
	}

	/**
	 * References the table for the current {@link EntityDescriptor} node.
	 */
	public SqlBuilder<E> table() {
		getInstructions().add(new Instruction(InstructionType.TABLE, null));
		return this;
	}

	/**
	 * Adds a parameter. This will be used in the final {@link PreparedStatement} execution.
	 * @param param the parameter. Arrays and Collections must be wrapped in an {@link Array} subclass.
	 * @throws IllegalArgumentException if a standard java array or Collection is passed.
	 */
	public SqlBuilder<E> param(Object param) {
		if (param == null) {
			getInstructions().add(new Instruction(PARAM, param));
		} else if (param.getClass().isArray() || param instanceof Collection) {
			throw new IllegalArgumentException("Arrays and collections must be wrapped in a com.asentinel.common.jdbc.arrays.Array subclass.");
		} else if (param instanceof Array) {
			getInstructions().add(new Instruction(ARRAY_PARAM, param));
		} else {
			getInstructions().add(new Instruction(PARAM, param));
		}
		return this;
	}

	// ------------------- pagination -----------------------
	
	/**
	 * @see #pagedSelect(long, long, String, EntityDescriptorNodeCallback...)
	 */
	public SqlBuilder<E> pagedSelect(long beginIndex, long endIndex, EntityDescriptorNodeCallback ... nodeCallbacks) {
		Assert.assertNotNull(clasz, "clasz");
		pagedSelect(beginIndex, endIndex, 
				entityDescriptorTreeRepository.getEntityDescriptorTree(clasz, nodeCallbacks));
		return this;
	}

	/**
	 * Creates a paginated select query. Under the hood it will call the {@link SqlFactory} pagination
	 * methods.
	 * @param beginIndex the first item to pull (inclusive, 0-based).
	 * @param endIndex the last item to pull (exclusive, 0-based).
	 * @param rootTableAlias the alias to be used for the root table.
	 * @param nodeCallbacks array of node callbacks.
	 * 
	 * @see SqlFactory#buildPaginatedQuery(com.asentinel.common.orm.QueryCriteria)
	 * @see SqlFactory#buildCountQuery(com.asentinel.common.orm.QueryCriteria)
	 * @see QueryCriteria
	 */
	public SqlBuilder<E> pagedSelect(long beginIndex, long endIndex, String rootTableAlias, EntityDescriptorNodeCallback ... nodeCallbacks) {
		Assert.assertNotNull(clasz, "clasz");
		pagedSelect(beginIndex, endIndex, 
				entityDescriptorTreeRepository.getEntityDescriptorTree(clasz, rootTableAlias, nodeCallbacks));
		return this;
	}
	
	/**
	 * Creates a paginated select query from the provided {@link EntityDescriptor} tree.
	 */
	public SqlBuilder<E> pagedSelect(long beginIndex, long endIndex, Node<EntityDescriptor> root) {	
		getInstructions().add(new Instruction(PAGED_INITIAL_QUERY, new Object[]{root, beginIndex, endIndex}));		
		return this;
	}	

	/**
	 * @see #pagedWhere()
	 */
	public SqlBuilder<E> pagedWhere() {
		return pagedWhere((String[]) null);
	}

	
	/**
	 * Switches the builder into the main where conditions mode for paginated queries. The
	 * table alias is set to the root {@link EntityDescriptor} node by calling {@link #rootAlias()}.  
	 * @param additionalColumns any additional columns that should be added to the <code>select</code> clause.
	 *
	 * @see SqlFactory#buildPaginatedQuery(com.asentinel.common.orm.QueryCriteria)
	 * @see SqlFactory#buildCountQuery(com.asentinel.common.orm.QueryCriteria)
	 * @see QueryCriteria
	 */
	public SqlBuilder<E> pagedWhere(String ... additionalColumns) {
		getInstructions().add(new Instruction(PAGED_MAIN_WHERE, additionalColumns));
		return rootAlias();
	}

	/**
	 * Switches the builder into the main order by mode for paginated queries. The
	 * table alias is set to the root {@link EntityDescriptor} node by calling {@link #rootAlias()}.  
	 *
	 * @see SqlFactory#buildPaginatedQuery(com.asentinel.common.orm.QueryCriteria)
	 * @see SqlFactory#buildCountQuery(com.asentinel.common.orm.QueryCriteria)
	 * @see QueryCriteria
	 */
	public SqlBuilder<E> pagedOrderBy() {
		getInstructions().add(new Instruction(PAGED_MAIN_ORDER_BY, null));
		return rootAlias();
	}
	
	/**
	 * @see #pagedEnableGroupBy(String...)
	 */
	public SqlBuilder<E> pagedEnableGroupBy() {
		return pagedEnableGroupBy((String[]) null);
	}

	/**
	 * Tells the builder to use group by for paginated queries. The
	 * table alias is set to the root {@link EntityDescriptor} node by calling {@link #rootAlias()}.
	 * @param additionalGroupByColumns any additional columns to add to the <code>group by</code> clause.  
	 *
	 * @see #pagedHaving()
	 * @see SqlFactory#buildPaginatedQuery(com.asentinel.common.orm.QueryCriteria)
	 * @see SqlFactory#buildCountQuery(com.asentinel.common.orm.QueryCriteria)
	 * @see QueryCriteria
	 */
	public SqlBuilder<E> pagedEnableGroupBy(String ... additionalGroupByColumns) {
		getInstructions().add(new Instruction(PAGED_USE_GROUP_BY, additionalGroupByColumns));
		return rootAlias();
	}

	/**
	 * Switches the builder into the main having mode for paginated queries. The
	 * table alias is set to the root {@link EntityDescriptor} node by calling {@link #rootAlias()}.
	 * This method should be used only if the {@link #pagedEnableGroupBy()} is also called on the builder.  
	 *
	 * @see SqlFactory#buildPaginatedQuery(com.asentinel.common.orm.QueryCriteria)
	 * @see SqlFactory#buildCountQuery(com.asentinel.common.orm.QueryCriteria)
	 * @see QueryCriteria
	 */
	public SqlBuilder<E> pagedHaving() {
		getInstructions().add(new Instruction(PAGED_HAVING, null));
		return rootAlias();
	}

	/**
	 * Switches the builder into the secondary where conditions mode for paginated
	 * queries. The table alias is set to the root {@link EntityDescriptor} node by
	 * calling {@link #rootAlias()}. Note that if we reference columns in the
	 * secondary where part of a paginated query we have to change the separator to
	 * {@code "_"} using the {@link #sep(String)} method.
	 *
	 * @see #sep(String)
	 * @see SqlFactory#buildPaginatedQuery(com.asentinel.common.orm.QueryCriteria)
	 * @see SqlFactory#buildCountQuery(com.asentinel.common.orm.QueryCriteria)
	 * @see QueryCriteria
	 */
	public SqlBuilder<E> pagedSecondaryWhere() {
		getInstructions().add(new Instruction(PAGED_SECONDARY_WHERE, null));
		return rootAlias();
	}
	
	/**
	 * Compiles the paginated instructions that were added to the builder. This method will only work if a 
	 * call was made to the <code>pagedSelect</code> method.
	 * @return a {@link PagedCompiledSql} instance that holds the compiled SQL query, any query parameters
	 * 			and the tree that was used for compilation.
	 * 
	 * @see PagedCompiledSql
	 */
	public PagedCompiledSql pagedCompile() {
		CompiledSql cSql = getInstructions().compile();
		if (!(cSql instanceof PagedCompiledSql)) {
			throw new IllegalStateException("The builder was not used to create a paginated query, but you attempted to compile it with pagination.");
		}
        return (PagedCompiledSql) cSql;
	}

	/**
	 * Compiles and then executes the resulting paginated and 
	 * count query.
	 * @return a {@link Page} object.
	 */
	@Deprecated
	public Page<E> execForPage(SqlQuery queryExecutor) {
		PagedCompiledSql compiledSql = pagedCompile();
		long count = queryExecutor.queryForLong(compiledSql.getSqlCountString(), compiledSql.getCountParameters());
		if (count > 0) {
			EntityBuilder<E> eb = new EntityBuilder<>(compiledSql.getRootNode());
			queryExecutor.query(compiledSql.getSqlString(), eb, compiledSql.getParameters());
			return new Page<>(eb.getEntityList(), count);
		} else {
			List<E> empty = Collections.emptyList();
			return new Page<>(empty, count);
		}
	}

	/**
	 * Compiles and then executes the resulting paginated and 
	 * count query.
	 * @return a {@link Page} object.
	 */
	public Page<E> execForPage() {
		PagedCompiledSql compiledSql = pagedCompile();
		long count = queryExecutor.queryForLong(compiledSql.getSqlCountString(), compiledSql.getCountParameters());
		if (count > 0) {
			EntityBuilder<E> eb = new EntityBuilder<>(compiledSql.getRootNode());
			queryExecutor.query(compiledSql.getSqlString(), eb, compiledSql.getParameters());
			return new Page<>(eb.getEntityList(), count);
		} else {
			List<E> empty = Collections.emptyList();
			return new Page<>(empty, count);
		}
	}
	
	/**
	 * Compiles and then executes ONLY the actual paginated query. Unlike
	 * {@link #execForPage(SqlQuery)} this method does not calculate the total
	 * number of records that satisfy the criteria.
	 * @return the list of objects resulted from the SQL query.
	 */
	@Deprecated
	public List<E> execForRange(SqlQuery queryExecutor) {
		PagedCompiledSql compiledSql = pagedCompile();
		EntityBuilder<E> eb = new EntityBuilder<>(compiledSql.getRootNode());
		queryExecutor.query(compiledSql.getSqlString(), eb, compiledSql.getParameters());
		return eb.getEntityList();
	}

	/**
	 * Compiles and then executes ONLY the actual paginated query. Unlike
	 * {@link #execForPage(SqlQuery)} this method does not calculate the total
	 * number of records that satisfy the criteria.
	 * @return the list of objects resulted from the SQL query.
	 * 
	 * @deprecated in favor of {@link #exec()} and/or {@link #execForEntity()}.
	 */
	@Deprecated
	public List<E> execForRange() {
		PagedCompiledSql compiledSql = pagedCompile();
		EntityBuilder<E> eb = new EntityBuilder<>(compiledSql.getRootNode());
		queryExecutor.query(compiledSql.getSqlString(), eb, compiledSql.getParameters());
		return eb.getEntityList();
	}
	
	/**
	 * Compiles and then executes ONLY the actual paginated query. It returns the index of the first 
	 * item in the current page that satisfies the SQL criteria. This method should be used in find/find next
	 * scenarios.
	 * @return the index of the first item in the current page that satisfies the SQL criteria or <code>-1</code>
	 * 			if no such item exists.
	 */
	@Deprecated
	public long execForIndex(SqlQuery queryExecutor) {
		PagedCompiledSql compiledSql = pagedCompile();
		String sqlFirstRowTemplate = sqlFactory.getSqlTemplates().getSqlForFirstRowOnly();
		String sql = String.format(sqlFirstRowTemplate, compiledSql.getSqlString());
		try {
			return queryExecutor.queryForLong(sql, compiledSql.getParameters());
		} catch(EmptyResultDataAccessException e) {
			return -1;
		}
	}

	/**
	 * Compiles and then executes ONLY the actual paginated query. It returns the index of the first 
	 * item in the current page that satisfies the SQL criteria. This method should be used in find/find next
	 * scenarios.
	 * @return the index of the first item in the current page that satisfies the SQL criteria or <code>-1</code>
	 * 			if no such item exists.
	 */
	public long execForIndex() {
		PagedCompiledSql compiledSql = pagedCompile();
		String sqlFirstRowTemplate = sqlFactory.getSqlTemplates().getSqlForFirstRowOnly();
		String sql = String.format(sqlFirstRowTemplate, compiledSql.getSqlString());
		try {
			return queryExecutor.queryForLong(sql, compiledSql.getParameters());
		} catch(EmptyResultDataAccessException e) {
			return -1;
		}
	}

	// -------------------- constants -----------------------	

    static final String SELECT = "select";
    static final String STAR = " *";
    static final String FROM = " from";
    static final String WHERE = " where";
    static final String ORDER_BY = " order by";
    static final String ASC = " asc";
    static final String DESC = " desc";
    static final String AND = " and";
    static final String OR = " or";
    static final String NOT = " not";
    static final String LP = " (";
    static final String RP = " )";
    static final String EQ = " =";
    static final String NE = " <>";
    static final String GT = " >";
    static final String LT = " <";
    static final String GTE = " >=";
    static final String LTE = " <=";
    static final String COMMA = ",";
    static final String LIKE = " like";
    static final String IN = " in";
    static final String IS = " is";
    static final String NULL = " null";
    static final String UPPER = " upper";
    static final String LOWER = " lower";
    static final String BETWEEN = " between";
    static final String EXISTS = " exists";
    static final String COUNT = " count";
    static final String UPDATE = "update";
    static final String SET = " set";
    static final String DELETE = "delete";
    static final String COALESCE = " coalesce";
}
