package com.asentinel.common.orm.persist;

import static com.asentinel.common.orm.mappers.SqlParameterTypeDescriptor.isCustomConversion;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.asentinel.common.jdbc.BooleanParameterConverter;
import com.asentinel.common.jdbc.ConversionSupport;
import com.asentinel.common.jdbc.DefaultBooleanParameterConverter;
import com.asentinel.common.jdbc.JdbcUtils;
import com.asentinel.common.jdbc.SimpleUser;
import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.ThreadLocalUser;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.JdbcFlavorConfig;
import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.TargetChildMember;
import com.asentinel.common.orm.TargetMember;
import com.asentinel.common.orm.TargetMembers;
import com.asentinel.common.orm.TargetMembersHolder;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.SqlParam;
import com.asentinel.common.orm.mappers.SqlParameterTypeDescriptor;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumn;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumnsEntity;
import com.asentinel.common.orm.proxy.InputStreamProxy;
import com.asentinel.common.util.Assert;

/**
 * <code>Updater</code> implementation that saves entities to the database.<br>
 * It performs a simple save - only the fields in the actual target entity are
 * saved, the fields in any child entities are ignored.<br>
 * <br>
 * The {@code update} methods and {@code upsert} methods analyze the target
 * entity class hierarchy from top to bottom (<code>Object</code> is at the
 * bottom) to find the first class that is mapped to a true table, not to a
 * view. Only the fields from that class and its super classes are considered
 * for saving. <br>
 * <br>
 * Note that only loaded entity proxies will be saved to the database. Unloaded
 * proxies will simply be ignored, we can tell for sure that if a proxy is not
 * loaded it was not modified in any way. <br>
 * <br>
 * This class is designed to be thread safe once it is initialized and it is
 * intended to be used as a bean in the Spring application context.
 * 
 * @author Razvan Popian
 */
public class SimpleUpdater implements Updater {

	private final static Logger log = LoggerFactory.getLogger(SimpleUpdater.class);

	// TODO 01: modify this class to perform deep saves
	// TODO 02: add support for insertable and updatable only columns for dynamic columns (see the DynamicColumn interface)

	private static final String INSERT = "insert into %s(%s) values(%s)";
	private static final String INSERT_AUTO_ID = "insert into %s(%s) values(%s, %s)";
	private static final String UPSERT_UPDATE = "update set %s";
	private static final String UPDATE = "update %s set %s where %s = ?";
	private static final String SEP = ", ";
	
	private final static ColumnMapRowMapper KEYS_ROW_MAPPER = new ColumnMapRowMapper();

	private final JdbcFlavor jdbcFlavor;
	private final SqlQuery queryEx;

	private NewEntityDetector newEntityDetector = new SimpleNewEntityDetector();
	
	@Deprecated(forRemoval = true, since = "1.71.0")
	private BooleanParameterConverter<?> booleanParameterConverter = new DefaultBooleanParameterConverter();
	
	private NewRowOnUpsertDetector newRowOnUpsertDetector;
	private ConversionService conversionService;

	public SimpleUpdater(JdbcFlavor jdbcFlavor, SqlQuery queryEx) {
		Assert.assertNotNull(jdbcFlavor, "jdbcFlavor");
		Assert.assertNotNull(queryEx, "queryEx");
		this.jdbcFlavor = jdbcFlavor;
		this.queryEx = queryEx;
	}

	@Deprecated
	public SimpleUpdater(SqlQuery queryEx) {
		this(JdbcFlavorConfig.getJdbcFlavor(), queryEx);
	}

	public NewEntityDetector getNewEntityDetector() {
		return newEntityDetector;
	}

	/**
	 * Sets the {@link NewEntityDetector} strategy to be used for identifying new
	 * entities vs. old entities.
	 */
	public void setNewEntityDetector(NewEntityDetector newEntityDetector) {
		Assert.assertNotNull(newEntityDetector, "newEntityDetector");
		this.newEntityDetector = newEntityDetector;
	}

	/**
	 * @deprecated in favor of the {@code ConversionService}. Booleans can be
	 *             converted to whatever the database supports using the
	 *             {@code ConversionService}.
	 */
	@Deprecated(forRemoval = true, since = "1.71.0")
	public BooleanParameterConverter<?> getBooleanParameterConverter() {
		return booleanParameterConverter;
	}

	/**
	 * Sets the {@link BooleanParameterConverter} strategy used for converting java
	 * booleans to the database boolean representation.
	 * 
	 * @deprecated in favor of the {@code ConversionService}. Booleans can be
	 *             converted to whatever the database supports using the
	 *             {@code ConversionService}.
	 */
	@Deprecated(forRemoval = true, since = "1.71.0")
	public void setBooleanParameterConverter(BooleanParameterConverter<?> booleanParameterConverter) {
		Assert.assertNotNull(booleanParameterConverter, "booleanParameterConverter");
		this.booleanParameterConverter = booleanParameterConverter;
	}


	public NewRowOnUpsertDetector getNewRowOnUpsertDetector() {
		return newRowOnUpsertDetector;
	}

	/**
	 * Sets the {@link NewRowOnUpsertDetector} strategy to be used for determining
	 * if an {@code upsert} statement created a new row or updated an existing row.
	 * 
	 * @see NewRowOnUpsertDetector
	 * @see NewRowOnUpsertAware
	 */
	public void setNewRowOnUpsertDetector(NewRowOnUpsertDetector newRowOnUpsertDetector) {
		this.newRowOnUpsertDetector = newRowOnUpsertDetector;
	}

	
	public ConversionService getConversionService() {
		return conversionService;
	}

	/**
	 * Sets a {@code ConversionService} to be used for {@code Column} annotated
	 * members that need special custom conversion to their corresponding database
	 * type.
	 * 
	 * @see Column#sqlParam()
	 * @see SqlParam
	 */
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}
	
	@Override
	public int update(Object entity, UpdateSettings<? extends DynamicColumn> settings) {
		if (EntityUtils.isProxy(entity) && !EntityUtils.isLoadedProxy(entity)) {
			log.trace("update - No operation is performed because the entity is a proxy that was not loaded yet.");
			return 0;
		}

		UpdateType updateType = settings.getUpdateType();
		if (updateType == UpdateType.AUTO) {
			Object id = EntityUtils.getEntityId(entity);
			if (newEntityDetector.isNew(id)) {
				return update(entity, settings.setUpdateType(UpdateType.INSERT_AUTO_ID));
			} else {
				return update(entity, settings.setUpdateType(UpdateType.UPDATE));
			}
		}

		int ret;
		TargetMembers targetMembers = TargetMembersHolder.getInstance().getTargetMembers(entity.getClass());

		String table = getUpdatableTable(targetMembers, settings);
		TargetMember pkMember = targetMembers.getPkColumnMember();
		Collection<? extends DynamicColumn> dynamicColumns = settings.getDynamicColumns();
		String sql;
		Object[] args;
		switch (updateType) {
		case INSERT:
			List<TargetMember> insertableMembers = targetMembers.getInsertableColumnMembers();
			sql = buildInsertStatement(updateType, table, pkMember, insertableMembers, settings);
			args = buildArguments(entity, updateType, insertableMembers, dynamicColumns, EnumSet.noneOf(StatementHints.class));
			ret = queryEx.update(sql, args);
			break;
		case INSERT_AUTO_ID:
			insertableMembers = targetMembers.getInsertableColumnMembers();
			sql = buildInsertStatement(updateType, table, pkMember, insertableMembers, settings);
			args = buildArguments(entity, updateType, insertableMembers, dynamicColumns, EnumSet.noneOf(StatementHints.class));

			KeyHolder keyHolder = new GeneratedKeyHolder();
			ret = queryEx.update(sql, new String[] { getPkColumnName(pkMember, settings.getPkDynamicColumn()) }, keyHolder, args);
			setEntityId(entity, keyHolder.getKey(), pkMember, settings.getPkDynamicColumn());
			break;
		case UPDATE:
			List<TargetMember> updatableMembers = targetMembers.getUpdatableColumnMembers();
			Optional<String> sqlOptional = buildUpdateStatement(entity, table, pkMember, updatableMembers, settings, EnumSet.of(StatementHints.EXCLUDE_INPUT_STREAM_PROXIES));
			if (sqlOptional.isEmpty()) {
				log.debug("update - Entity {} has no updatable columns so no update will be performed.", entity);
				return 0;
			}
			sql = sqlOptional.get();
			args = buildArguments(entity, updateType, updatableMembers, dynamicColumns, EnumSet.of(StatementHints.EXCLUDE_INPUT_STREAM_PROXIES));
			ret = queryEx.update(sql, args);
			break;
		default:
			throw new NullPointerException("Null update type.");
		}
		return ret;
	}

	@Override
	public int upsert(Object entity, UpdateSettings<? extends DynamicColumn> settings, Object... hints) {
		if (EntityUtils.isProxy(entity) && !EntityUtils.isLoadedProxy(entity)) {
			log.trace("upsert - No operation is performed because the entity is a proxy that was not loaded yet.");
			return 0;
		}
		
		UpdateType updateTypeInsert = settings.getUpdateType();
		if (updateTypeInsert != UpdateType.INSERT 
				&& updateTypeInsert != UpdateType.INSERT_AUTO_ID) {
			if (log.isTraceEnabled()) {
				log.trace("upsert - The requested UpdateType is " + updateTypeInsert 
						+ ". This is not supported for upserts, defaulting to " + UpdateType.INSERT_AUTO_ID + ".");
			}
			updateTypeInsert = UpdateType.INSERT_AUTO_ID;
		}
		if (hints == null) {
			hints = new Object[0];
		}
		if (hints.length % 2 != 0) {
			throw new IllegalArgumentException(
					"The hints must be a collection of key-value pairs, so the number of hints should be a multiple of 2.");
		}

		TargetMembers targetMembers = TargetMembersHolder.getInstance().getTargetMembers(entity.getClass());
		Collection<? extends DynamicColumn> dynamicColumns = settings.getDynamicColumns();

		String table = getUpdatableTable(targetMembers, settings);
		TargetMember pkMember = targetMembers.getPkColumnMember();
		List<TargetMember> insertableMembers = targetMembers.getInsertableColumnMembers();
		List<TargetMember> updatableMembers = targetMembers.getUpdatableColumnMembers();

		String sqlInsert = buildInsertStatement(updateTypeInsert, table, pkMember, insertableMembers, settings);
		Object[] argsInsert = buildArguments(entity, updateTypeInsert, insertableMembers, dynamicColumns, EnumSet.of(StatementHints.UPSERT));

		Optional<String> sqlUpdateOptional = buildUpdateStatement(entity, table, pkMember, updatableMembers, settings, EnumSet.of(StatementHints.UPSERT));
		if (sqlUpdateOptional.isEmpty()) {
			throw new IllegalArgumentException("Entity " + entity + " has no updatable columns so no upsert can be performed");
		}
		String sqlUpdate = sqlUpdateOptional.get();
		Object[] argsUpdate = buildArguments(entity, UpdateType.UPDATE, updatableMembers, dynamicColumns, EnumSet.of(StatementHints.UPSERT));
		
		String sql = concatSqlsForUpsert(sqlInsert, sqlUpdate, pkMember, updateTypeInsert, hints);
		Object[] args = concatArgumentsForUpsert(argsInsert, argsUpdate, updateTypeInsert, entity);

		KeyHolder keyHolder = new GeneratedKeyHolder();
		String pkColumn = getPkColumnName(pkMember, settings.getPkDynamicColumn());
		int ret = queryEx.update(sql, getKeyColumns(pkColumn), keyHolder, args);
		if (updateTypeInsert == UpdateType.INSERT_AUTO_ID) {
			setEntityId(entity, (Number) keyHolder.getKeys().get(pkColumn), pkMember, settings.getPkDynamicColumn());
		}
		setNewRowOnUpsert(entity, keyHolder.getKeys());
		return ret;
	}

	// TODO: add a return type, to inform the client about the number of rows
	// updated
	@Override
	public <E> void update(Collection<E> collection, UpdateSettings<? extends DynamicColumn> settings) {
		if (collection == null || collection.isEmpty()) {
			return;
		}

		UpdateType updateType = settings.getUpdateType();
		Collection<? extends DynamicColumn> dynamicColumns = settings.getDynamicColumns();
		
		if (updateType == UpdateType.AUTO) {
			Map<Boolean, List<Object>> partitions = collection.stream()
					.collect(partitioningBy(newEntityDetector::isNewEntity));

			// perform the inserts
			update(partitions.get(Boolean.TRUE), settings.setUpdateType(UpdateType.INSERT_AUTO_ID));

			// perform the updates
			update(partitions.get(Boolean.FALSE), settings.setUpdateType(UpdateType.UPDATE));

			return;
		}

		List<E> entities = removeUnloadedProxies(collection);
		if (entities.isEmpty()) {
			return;
		}

		// TODO: all objects in the collection should have the same type
		// because the generated SQL statements are based on the first object type.
		// Some enhancements may be needed here.
		Object probeEntity = entities.get(0);
		TargetMembers targetMembers = TargetMembersHolder.getInstance().getTargetMembers(probeEntity.getClass());
		String table = getUpdatableTable(targetMembers, settings);
		TargetMember pkMember = targetMembers.getPkColumnMember();
		JdbcOperations jdbcOps = queryEx.getJdbcOperations();
		String sql;
		switch (updateType) {
		case INSERT:
			List<TargetMember> insertableMembers = targetMembers.getInsertableColumnMembers();
			sql = buildInsertStatement(updateType, table, pkMember, insertableMembers, settings);
			log(sql);
			jdbcOps.batchUpdate(sql, new CustomBatchPreparedStatementSetter(entities,
					updateType, insertableMembers, dynamicColumns));
			return;
		case INSERT_AUTO_ID:
			insertableMembers = targetMembers.getInsertableColumnMembers();
			sql = buildInsertStatement(updateType, table, pkMember, insertableMembers, settings);
			log(sql);
			jdbcOps.execute(new CustomBatchPreparedStatementCreator(entities,
					insertableMembers, emptyList(), settings, pkMember, sql), new CustomBatchPreparedStatementCallback(entities, pkMember, settings));
			return;
		case UPDATE:
			List<TargetMember> updatableMembers = targetMembers.getUpdatableColumnMembers();
			Optional<String> sqlOptional = buildUpdateStatement(null, table, pkMember, updatableMembers, settings, EnumSet.noneOf(StatementHints.class));
			if (sqlOptional.isEmpty()) {
				log.debug("update - Entity {} has no updatable columns so no update will be performed.", probeEntity);
				return;
			}
			sql = sqlOptional.get();
			log(sql);
			jdbcOps.batchUpdate(sql, new CustomBatchPreparedStatementSetter(entities,
					updateType, updatableMembers, dynamicColumns));
			return;
		default:
			throw new NullPointerException("Null update type.");
		}
	}
	
	@Override
	public <E> void upsert(Collection<E> collection, UpdateSettings<? extends DynamicColumn> settings, Object... hints) {
		if (collection == null || collection.isEmpty()) {
			return;
		}
		
		UpdateType updateTypeInsert = settings.getUpdateType();
		if (updateTypeInsert != UpdateType.INSERT 
				&& updateTypeInsert != UpdateType.INSERT_AUTO_ID) {
			if (log.isDebugEnabled()) {
				log.debug("upsert - The requested UpdateType is " + updateTypeInsert 
						+ ". This is not supported for upserts, defaulting to " + UpdateType.INSERT_AUTO_ID + ".");
			}
			updateTypeInsert = UpdateType.INSERT_AUTO_ID;
		}
		
		if (hints == null) {
			hints = new Object[0];
		}
		if (hints.length % 2 != 0) {
			throw new IllegalArgumentException(
					"The hints must be a collection of key-value pairs, so the number of hints should be a multiple of 2.");
		}
		
		List<E> entities = removeUnloadedProxies(collection);
		if (entities.isEmpty()) {
			return;
		}
		
		// TODO: all objects in the collection should have the same type
		// because the generated SQL statements are based on the first object type.
		// Some enhancements may be needed here.

		Object probeEntity = entities.get(0);
		TargetMembers targetMembers = TargetMembersHolder.getInstance().getTargetMembers(probeEntity.getClass());

		String table = getUpdatableTable(targetMembers, settings);
		TargetMember pkMember = targetMembers.getPkColumnMember();
		List<TargetMember> insertableMembers = targetMembers.getInsertableColumnMembers();
		List<TargetMember> updatableMembers = targetMembers.getUpdatableColumnMembers();

		String sqlInsert = buildInsertStatement(updateTypeInsert, table, pkMember, insertableMembers, settings);
		Optional<String> sqlUpdateOptional = buildUpdateStatement(null, table, pkMember, updatableMembers, settings, EnumSet.of(StatementHints.UPSERT));
		if (sqlUpdateOptional.isEmpty()) {
			throw new IllegalArgumentException("Entity " + probeEntity + " has no updatable columns so no upsert can be performed");
		}
		String sqlUpdate = sqlUpdateOptional.get();
		String sql = concatSqlsForUpsert(sqlInsert, sqlUpdate, pkMember, updateTypeInsert, hints);
		log(sql);

		JdbcOperations jdbcOps = queryEx.getJdbcOperations();
		jdbcOps.execute(new CustomBatchPreparedStatementCreator(entities,
				insertableMembers, updatableMembers, settings, pkMember, sql, EnumSet.of(StatementHints.UPSERT), updateTypeInsert), 
				new CustomBatchPreparedStatementCallback(entities, pkMember, settings, true, updateTypeInsert));
	}
	
	@Override
	public int delete(Class<?> entityType, Object ... ids) {
		Assert.assertNotNull(entityType, "entityType");
		if (ids == null || ids.length == 0) {
			return 0;
		}
		TargetMembers targetMembers = TargetMembersHolder.getInstance()
				.getTargetMembers(entityType);
		String table = targetMembers.getUpdatableTable();
		String pkColumn = targetMembers.getPkColumnMember().getPkColumnAnnotation().value();
		
		// we could potentially pass a single array parameter to the query
		// and use JdbcFlavor.getSqlTemplates().getSqlForInArray() to render the Sql for that
		// but we would need to know the type of the array to pass. So for now
		// this implementation is good enough as it's very unlikely that we will ever have a large number
		// of ids passed here
		String sql = String.format("delete from %s where %s in (%s)",
				table, pkColumn, 
				Arrays.asList(ids).stream().map(id -> "?").collect(joining(",")));
		return queryEx.update(sql, ids);
	}


	

	// -------------------------------------------------------- //
	// utility methods and inner classes
	// -------------------------------------------------------- //
	
	private String getUpdatableTable(TargetMembers targetMembers, UpdateSettings<? extends DynamicColumn> settings) {
		if (StringUtils.hasText(settings.getTable())) {
			return settings.getTable();
		}
		return targetMembers.getUpdatableTable();
	}
	
	private static String getPkColumnName(TargetMember pkTargetMember, DynamicColumn pkDynamicColumn) {
		if (pkDynamicColumn != null) {
			return pkDynamicColumn.getDynamicColumnName();
		}
		return pkTargetMember.getPkColumnAnnotation().value();
	}
	
	private static Class<?> getPkColumnType(TargetMember pkTargetMember, DynamicColumn pkDynamicColumn) {
		if (pkDynamicColumn != null) {
			return pkDynamicColumn.getDynamicColumnType();
		}
		return pkTargetMember.getMemberClass();
	}
	
	private static String getPkSeqName(TargetMember pkTargetMember, DynamicColumn pkDynamicColumn) {
		if (pkDynamicColumn != null) {
			return ""; // TODO: the DynamicColumn should be able to provide a sequence name
		}
		return pkTargetMember.getPkColumnAnnotation().sequence();
	}
	
	private String[] getKeyColumns(String pkColumn) {
		String[] keyColumns;
		if (newRowOnUpsertDetector != null) {
			String[] columns = newRowOnUpsertDetector.getColumns();
			keyColumns = new String[1 + columns.length]; 
			keyColumns[0] = pkColumn;
			System.arraycopy(columns, 0, keyColumns, 1, columns.length);
		} else {
			keyColumns = new String[] { pkColumn };
		}
		return keyColumns;
	}
	
	private void setNewRowOnUpsert(Object entity, Map<String, Object> keyValues) {
		if (entity instanceof NewRowOnUpsertAware) {
			if (newRowOnUpsertDetector != null) {
				((NewRowOnUpsertAware) entity).setNewRow(newRowOnUpsertDetector.isNewRow(keyValues));
			} else {
				log.warn("setNewRowOnUpsert - The entity of type " + entity.getClass().getName() 
						+ " is a " + NewRowOnUpsertAware.class.getSimpleName() 
						+ ", but no " + NewRowOnUpsertDetector.class.getSimpleName()
						+ " was injected. So we can not tell if the upsert was an actual insert or an actual update.");
			}
		}
	}

	private static void setEntityId(Object entity, Number entityId, TargetMember pkMember, DynamicColumn pkDynamicColumn) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			// we are in a Spring managed transaction, so we make sure
			// there are no side effects in the entity state in case of rollback
			Object savedEntityId = EntityUtils.getEntityId(entity);
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCompletion(int status) {
					if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
						EntityUtils.setEntityId(entity, savedEntityId);
						if (log.isTraceEnabled()) {
							log.trace("setEntityId - entity id reset for entity {} after transaction rollback.", entity);
						}
					}
				}
			});
		}

		Class<?> idClass = getPkColumnType(pkMember, pkDynamicColumn);
		if (idClass == int.class || idClass == Integer.class) {
			EntityUtils.setEntityId(entity, entityId.intValue());
		} else if (idClass == long.class || idClass == Long.class) {
			EntityUtils.setEntityId(entity, entityId.longValue());
		} else {
			throw new IllegalArgumentException("Unsupported id class.");
		}
	}

	private static <E> List<E> removeUnloadedProxies(Collection<E> entities) {
		return entities.stream().filter(e -> !EntityUtils.isProxy(e) || EntityUtils.isLoadedProxy(e))
				.collect(toList());
	}

	private static void log(String sql) {
		if (log.isDebugEnabled()) {
			Object userId = -1;
			String userName = "UNKNOWN";
			SimpleUser user = ThreadLocalUser.getThreadLocalUser().get();
			if (user != null) {
				userId = user.getUserId();
				userName = user.getUsername();
			}
			if (user != null) {
				log.debug("batch update - user: " + userName + " (" + userId + "); sql: " + sql);
			} else {
				log.debug("batch update - sql: " + sql);
			}
		}
	}

	private Object[] buildArguments(Object entity, UpdateType updateType, List<TargetMember> updatableMembers, Collection<? extends DynamicColumn> dynamicColumns, 
			Set<StatementHints> statementHints) {
		List<Object> args;
		switch (updateType) {
		case INSERT:
			args = new ArrayList<>(updatableMembers.size() + 1);
			args.add(EntityUtils.getEntityId(entity));
			for (TargetMember targetMember : updatableMembers) {
				args.add(getArgument(targetMember, entity));
			}
			for (DynamicColumn dynamicColumn: dynamicColumns) {
				args.add(getArgument(dynamicColumn, entity));
			}
			break;
		case INSERT_AUTO_ID:
			args = new ArrayList<>(updatableMembers.size());
			for (TargetMember targetMember : updatableMembers) {
				args.add(getArgument(targetMember, entity));
			}
			for (DynamicColumn dynamicColumn: dynamicColumns) {
				args.add(getArgument(dynamicColumn, entity));
			}
			break;
		case UPDATE:
			args = new ArrayList<>(updatableMembers.size() + 1);
			for (TargetMember targetMember : updatableMembers) {
				Object value = getArgument(targetMember, entity);
				if (value instanceof InputStreamProxy
						&& statementHints.contains(StatementHints.EXCLUDE_INPUT_STREAM_PROXIES)) {
					continue;
				}
				args.add(value);
			}
			for (DynamicColumn dynamicColumn: dynamicColumns) {
				Object value = getArgument(dynamicColumn, entity);
				if (value instanceof InputStreamProxy
						&& statementHints.contains(StatementHints.EXCLUDE_INPUT_STREAM_PROXIES)) {
					continue;
				}
				args.add(value);
			}
			if (!statementHints.contains(StatementHints.UPSERT)) {
				args.add(EntityUtils.getEntityId(entity));
			}
			break;
		default:
			throw new NullPointerException("Null update type.");
		}
		return args.toArray(new Object[args.size()]);
	}
	
	private Object[] concatArgumentsForUpsert(Object[] argsInsert, Object[] argsUpdate, UpdateType updateTypeInsert, Object entity) {
		Object[] argsUpdateFinal;
		if (updateTypeInsert == UpdateType.INSERT) {
			// if the update type is INSERT we also try to update the primary key
			argsUpdateFinal = new Object[argsUpdate.length + 1];
			System.arraycopy(argsUpdate, 0, argsUpdateFinal, 0, argsUpdate.length);
			argsUpdateFinal[argsUpdateFinal.length - 1] = EntityUtils.getEntityId(entity);
		} else {
			// if the update type is INSERT_AUTO_ID we don't update the primary key,
			// we are good with the update statement built by the buildUpdateStatement method
			argsUpdateFinal = argsUpdate;
		}
		
		Object[] args = new Object[argsInsert.length + argsUpdateFinal.length];
		System.arraycopy(argsInsert, 0, args, 0, argsInsert.length);
		System.arraycopy(argsUpdateFinal, 0, args, argsInsert.length, argsUpdateFinal.length);

		return args;
	}

	private String buildInsertStatement(UpdateType updateType, String table, TargetMember pkMember,
			List<TargetMember> updatableMembers, UpdateSettings<? extends DynamicColumn> settings) {
		StringBuilder columns = new StringBuilder();
		StringBuilder qMarks = new StringBuilder();

		String seq = getPkSeqName(pkMember, settings.getPkDynamicColumn());
		boolean withSeq = StringUtils.hasText(seq);

		if (updateType == UpdateType.INSERT || (updateType == UpdateType.INSERT_AUTO_ID && withSeq)) {
			columns.append(pkMember.getPkColumnAnnotation().value()).append(SEP);
		}

		if (updateType == UpdateType.INSERT) {
			qMarks.append("?").append(SEP);
		}

		for (TargetMember targetMember : updatableMembers) {
			if (targetMember instanceof TargetChildMember) {
				String fkName = ((TargetChildMember) targetMember).getFkNameForOneToMany();
				if (StringUtils.hasText(fkName)) {
					columns.append(fkName).append(SEP);
				}
			} else {
				columns.append(targetMember.getColumnAnnotation().value()).append(SEP);
			}
			qMarks.append("?").append(SEP);
		}
		for (DynamicColumn dynamicColumn: settings.getDynamicColumns()) {
			columns.append(dynamicColumn.getDynamicColumnName()).append(SEP);
			qMarks.append("?").append(SEP);
		}
		if (columns.length() > 0) {
			columns.delete(columns.length() - SEP.length(), columns.length());
			if (qMarks.length() > 0) {
				qMarks.delete(qMarks.length() - SEP.length(), qMarks.length());
			}
		}

		if (updateType == UpdateType.INSERT) {
			return String.format(INSERT, table, columns, qMarks);
		} else if (updateType == UpdateType.INSERT_AUTO_ID) {
			if (withSeq) {
				String seqInstruction = jdbcFlavor.getSqlTemplates().getSqlForNextSequenceVal(seq);
				if (qMarks.length() > 0) {
					return String.format(INSERT_AUTO_ID, table, columns, seqInstruction, qMarks);
				} else {
					return String.format(INSERT, table, columns, seqInstruction);
				}
			} else {
				if (qMarks.length() > 0) {
					return String.format(INSERT, table, columns, qMarks);
				} else {
					throw new IllegalArgumentException("Can not build an auto id/no sequence insert statement "
							+ "for an entity with no columns mapped for insert.");
				}
			}
		} else {
			throw new IllegalArgumentException("Invalid update type.");
		}
	}

	/*
	 * the entity param is null for batch calls
	 */
	private Optional<String> buildUpdateStatement(Object entity, String table, TargetMember pkMember, List<TargetMember> updatableMembers, UpdateSettings<? extends DynamicColumn> settings, 
			Set<StatementHints> statementHints) {
		StringBuilder dbSets = new StringBuilder();
		for (TargetMember targetMember : updatableMembers) {
			if (targetMember instanceof TargetChildMember) {
				String fkName = ((TargetChildMember) targetMember).getFkNameForOneToMany();
				if (StringUtils.hasText(fkName)) {
					dbSets.append(fkName);
				}
			} else {
				if (ConversionSupport.INPUT_STREAM_TYPE == targetMember.getMemberClass()
						&& statementHints.contains(StatementHints.EXCLUDE_INPUT_STREAM_PROXIES)) {
					Object is = getArgument(targetMember, entity);
					if (is instanceof InputStreamProxy) {
						continue;
					}
				}				
				dbSets.append(targetMember.getColumnAnnotation().value());
			}
			dbSets.append(" = ?").append(SEP);
		}
		for (DynamicColumn dynamicColumn: settings.getDynamicColumns()) {
			if (ConversionSupport.INPUT_STREAM_TYPE == dynamicColumn.getDynamicColumnType()
					&& statementHints.contains(StatementHints.EXCLUDE_INPUT_STREAM_PROXIES)) {
				Object is = getArgument(dynamicColumn, entity);
				if (is instanceof InputStreamProxy) {
					continue;
				}
			}			
			dbSets.append(dynamicColumn.getDynamicColumnName());
			dbSets.append(" = ?").append(SEP);
		}
		if (dbSets.length() > 0) {
			dbSets.delete(dbSets.length() - SEP.length(), dbSets.length());
		} else {
			// no columns to update, so we signal this to
			// the caller with an empty optional
			return Optional.empty();
		}
		if (statementHints.contains(StatementHints.UPSERT)) {
			return Optional.of(String.format(UPSERT_UPDATE, dbSets));
		} else {
			return Optional.of(String.format(UPDATE, table, dbSets, getPkColumnName(pkMember, settings.getPkDynamicColumn())));
		}
	}
	
	private String concatSqlsForUpsert(String sqlInsert, String sqlUpdate, TargetMember pkMember, UpdateType updateTypeInsert, Object... hints) {
		if (updateTypeInsert == UpdateType.INSERT) {
			// if the update type is INSERT we also have to update the primary key
			sqlUpdate += SEP + pkMember.getPkColumnAnnotation().value() + " = ?";
		}

		return jdbcFlavor.getSqlTemplates().getSqlForUpsert(sqlInsert, sqlUpdate, hints);
	}

	private Object getArgument(TargetMember targetMember, Object entity) {
		AnnotatedElement member = targetMember.getAnnotatedElement();
		Object argument;
		if (member instanceof Field) {
			ReflectionUtils.makeAccessible((Field) member);
			argument = ReflectionUtils.getField((Field) member, entity);
		} else if (member instanceof Method) {
			Method getMethod = targetMember.getGetMethod();
			if (getMethod == null) {
				throw new IllegalArgumentException("No get method available for setter " + member
						+ ". Please define a corresponding getter method.");
			}
			ReflectionUtils.makeAccessible(getMethod);
			argument = ReflectionUtils.invokeMethod(getMethod, entity);
		} else {
			throw new IllegalStateException("Expected Field or Method. Found " + member.getClass().getName());
		}
		if (targetMember instanceof TargetChildMember) {
			if (argument != null) {
				argument = EntityUtils.getEntityId(argument);
			}
			// else the value to go to the db is null
		} else {
			// see if we need special conversion
			if (conversionService != null
					&& argument != null
					&& isCustomConversion(targetMember.getColumnAnnotation())) {
				TypeDescriptor sourceDescriptor = targetMember.getTypeDescriptor();
				TypeDescriptor targetDescriptor = new SqlParameterTypeDescriptor(targetMember.getColumnAnnotation().sqlParam());
				if (conversionService.canConvert(sourceDescriptor, targetDescriptor)) {
					argument = conversionService.convert(argument, sourceDescriptor, targetDescriptor);
				}
			}
		}
		if (argument instanceof Boolean) {
			argument = booleanParameterConverter.asObject((Boolean) argument);
		}
		return argument;
	}
	
	private Object getArgument(DynamicColumn dynamicColumn, Object entity) {
		if (!(entity instanceof DynamicColumnsEntity)) {
			throw new ClassCastException("The entity is not a " + DynamicColumnsEntity.class.getSimpleName() 
					+ ", but the update settings include dynamic columns whose values can not be extracted "
					+ "from a non " + DynamicColumnsEntity.class.getSimpleName() + " entity." ) ;
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Object argument = ((DynamicColumnsEntity) entity).getValue(dynamicColumn);
		if (argument != null && EntityUtils.isEntityClass(argument.getClass())) {
			argument = EntityUtils.getEntityId(argument);
		} else {
			if (conversionService != null
					&& argument != null
					&& isCustomConversion(dynamicColumn)) {
				TypeDescriptor sourceDescriptor = dynamicColumn.getTypeDescriptor();
				TypeDescriptor targetDescriptor = new SqlParameterTypeDescriptor(dynamicColumn.getSqlParameter());
				if (conversionService.canConvert(sourceDescriptor, targetDescriptor)) {
					argument = conversionService.convert(argument, sourceDescriptor, targetDescriptor);
				}
			}
		}
		if (argument instanceof Boolean) {
			argument = booleanParameterConverter.asObject((Boolean) argument);
		}
		return argument;
	}

	/**
	 * Used for {@link UpdateType#INSERT} and {@link UpdateType#UPDATE} updates (not used for upserts).
	 */
	class CustomBatchPreparedStatementSetter implements BatchPreparedStatementSetter, ParameterDisposer {

		private final List<?> entities;
		private final UpdateType updateType;
		private final List<TargetMember> updatableMembers;
		private final Collection<? extends DynamicColumn> dynamicColumns;
		private final Set<StatementHints> statementHints;

		private LobCreator lobCreator = null;
		private List<Object> allArgs;
		
		public CustomBatchPreparedStatementSetter(List<?> entities, UpdateType updateType,
				List<TargetMember> updatableMembers, Collection<? extends DynamicColumn> dynamicColumns) {
			this(entities, updateType, updatableMembers, dynamicColumns, EnumSet.noneOf(StatementHints.class));
		}

		public CustomBatchPreparedStatementSetter(List<?> entities, UpdateType updateType,
				List<TargetMember> updatableMembers, Collection<? extends DynamicColumn> dynamicColumns, Set<StatementHints> statementHints) {
			if (entities == null) {
				entities = Collections.emptyList();
			}
			if (updatableMembers == null) {
				updatableMembers = Collections.emptyList();
			}

			this.entities = entities;
			this.updateType = updateType;
			this.updatableMembers = updatableMembers;
			this.dynamicColumns = dynamicColumns;
			this.statementHints = statementHints;
		}

		@Override
		public void setValues(PreparedStatement ps, int i) throws SQLException {
			Object entity = entities.get(i);
			if (entity == null) {
				throw new NullPointerException("Null entity found in the update list.");
			}
			Object[] args;
			if (statementHints.contains(StatementHints.UPSERT)) {
				Object[] argsInsert = buildArguments(entity, updateType, updatableMembers, dynamicColumns, statementHints);
				Object[] argsUpdate = buildArguments(entity, UpdateType.UPDATE, updatableMembers, dynamicColumns, statementHints);
				args = concatArgumentsForUpsert(argsInsert, argsUpdate, updateType, entity);
			} else {
				args = buildArguments(entity, updateType, updatableMembers, dynamicColumns, statementHints);
			}
			if (allArgs == null) {
				allArgs = new ArrayList<Object>(args.length * entities.size());
			}
			allArgs.addAll(Arrays.asList(args));
			if (args.length > 0) {
				if (log.isDebugEnabled()) {
					log.debug("setValues - batch update with parameters: " + JdbcUtils.parametersToString(true, args));
				}
			}

			lobCreator = jdbcFlavor.getPreparedStatementParametersSetter().setParameters(ps, lobCreator, args);
		}

		@Override
		public int getBatchSize() {
			return entities.size();
		}

		@Override
		public void cleanupParameters() {
			if (lobCreator != null) {
				lobCreator.close();
				lobCreator = null;
				if (log.isTraceEnabled()) {
					log.trace("cleanupParameters - LobCreator closed.");
				}
			}

			// Cleanup for other parameters
			if (allArgs != null) {
				jdbcFlavor.getPreparedStatementParametersSetter().cleanupParameters(allArgs.toArray(new Object[allArgs.size()]));
			}
		}

		List<Object> getAllArgs() {
			return allArgs;
		}

	}

	/**
	 * Used for {@link UpdateType#INSERT_AUTO_ID} inserts and for all batch upserts.
	 */
	class CustomBatchPreparedStatementCreator implements PreparedStatementCreator, ParameterDisposer {
		private final List<?> entities;
		private final List<TargetMember> insertableMembers;
		private final List<TargetMember> updatableMembers;
		private final UpdateSettings<? extends DynamicColumn> settings;
		private final TargetMember pkMember;
		private final String sql;
		private final Set<StatementHints> statementHints;
		private final UpdateType upsertUpdateType;

		private LobCreator lobCreator = null;
		private List<Object> allArgs;
		
		public CustomBatchPreparedStatementCreator(List<?> entities,
				List<TargetMember> insertableMembers, List<TargetMember> updatableMembers, 
				UpdateSettings<? extends DynamicColumn> settings, TargetMember pkMember, String sql) {
			this(entities, insertableMembers, updatableMembers, settings, pkMember, sql, EnumSet.noneOf(StatementHints.class), null);
		}

		public CustomBatchPreparedStatementCreator(List<?> entities,
				List<TargetMember> insertableMembers, List<TargetMember> updatableMembers, 
				UpdateSettings<? extends DynamicColumn> settings, TargetMember pkMember, String sql, 
				Set<StatementHints> statementHints, UpdateType upsertUpdateType) {

			if (entities == null) {
				entities = Collections.emptyList();
			}
			if (updatableMembers == null) {
				updatableMembers = Collections.emptyList();
			}

			this.entities = entities;
			this.insertableMembers = insertableMembers;
			this.updatableMembers = updatableMembers;
			this.settings = settings;
			this.pkMember = pkMember;
			this.sql = sql;
			this.statementHints = statementHints;
			this.upsertUpdateType = upsertUpdateType;
		}

		@Override
		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
			String pkColumn = pkMember.getPkColumnAnnotation().value();
			String[] keyColumns;
			if (statementHints.contains(StatementHints.UPSERT)) {
				keyColumns = getKeyColumns(pkColumn);
			} else {
				keyColumns = new String[] {pkColumn};
			}
			PreparedStatement ps = con.prepareStatement(sql,
					jdbcFlavor.preprocessKeyColumnNames(keyColumns));

			for (Object entity : entities) {
				Object[] args;
				if (statementHints.contains(StatementHints.UPSERT)) {
					Object[] argsInsert = buildArguments(entity, upsertUpdateType, insertableMembers, settings.getDynamicColumns(), statementHints);
					Object[] argsUpdate = buildArguments(entity, UpdateType.UPDATE, updatableMembers, settings.getDynamicColumns(), statementHints);
					args = concatArgumentsForUpsert(argsInsert, argsUpdate, upsertUpdateType, entity);
				} else {
					args = buildArguments(entity, UpdateType.INSERT_AUTO_ID, insertableMembers, settings.getDynamicColumns(), statementHints);
				}
				if (allArgs == null) {
					allArgs = new ArrayList<>(args.length * entities.size());
				}
				allArgs.addAll(Arrays.asList(args));
				if (args.length > 0) {
					if (log.isDebugEnabled()) {
						log.debug("doInPreparedStatement - batch update with parameters: "
								+ JdbcUtils.parametersToString(true, args));
					}
				}

				lobCreator = jdbcFlavor.getPreparedStatementParametersSetter().setParameters(ps, lobCreator, args);
				ps.addBatch();
			}

			return ps;
		}

		@Override
		public void cleanupParameters() {
			if (lobCreator != null) {
				lobCreator.close();
				lobCreator = null;
				if (log.isTraceEnabled()) {
					log.trace("cleanupParameters - LobCreator closed.");
				}
			}

			// cleanup specific parameters
			if (allArgs != null) {
				jdbcFlavor.getPreparedStatementParametersSetter().cleanupParameters(allArgs.toArray(new Object[allArgs.size()]));
			}
		}

		String getSql() {
			return sql;
		}

		List<Object> getAllArgs() {
			return allArgs;
		}

	}

	/**
	 * Used for {@link UpdateType#INSERT_AUTO_ID} and for all batch upserts.
	 */
	class CustomBatchPreparedStatementCallback implements PreparedStatementCallback<int[]> {
		private final List<?> entities;
		private final TargetMember pkMember;
		private final UpdateSettings<? extends DynamicColumn> settings;
		private final boolean upsert;
		private final UpdateType upsertUpdateType;
		
		public CustomBatchPreparedStatementCallback(List<?> entities, TargetMember pkMember, UpdateSettings<? extends DynamicColumn> settings) {
			this(entities, pkMember, settings, false, null);
		}		

		public CustomBatchPreparedStatementCallback(List<?> entities, TargetMember pkMember,
				UpdateSettings<? extends DynamicColumn> settings, boolean upsert, UpdateType upsertUpdateType) {
			if (entities == null) {
				entities = Collections.emptyList();
			}
			this.entities = entities;
			this.pkMember = pkMember;
			this.settings = settings;
			this.upsert = upsert;
			this.upsertUpdateType = upsertUpdateType;
		}

		@Override
		public int[] doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
			int[] res = ps.executeBatch();

			ResultSet keysRs = ps.getGeneratedKeys();
			if (keysRs != null) {
				RowMapperResultSetExtractor<Map<String, Object>> rse = new RowMapperResultSetExtractor<>(KEYS_ROW_MAPPER, entities.size());
				List<Map<String, Object>> keys;
				try {
					keys = rse.extractData(keysRs);
				} finally {
					org.springframework.jdbc.support.JdbcUtils.closeResultSet(keysRs);
				}
				if (entities.size() != keys.size()) {
					// TODO: should throw an exception here ?
					log.error("doInPreparedStatement - For " + entities.size() + " entities got " + keys.size()
							+ " keys. Something is wrong, so the ids will not be assigned to entities.");
					return res;
				}

				for (int i = 0; i < entities.size(); i++) {
					Object entity = entities.get(i);
					if (upsert) {
						if (upsertUpdateType == UpdateType.INSERT_AUTO_ID) {
							setEntityId(entity, 
									(Number) keys.get(i).get(getPkColumnName(pkMember, settings.getPkDynamicColumn())), pkMember, settings.getPkDynamicColumn());							
						}
						setNewRowOnUpsert(entity, keys.get(i));
					} else {
						setEntityId(entity, 
								(Number) keys.get(i).get(getPkColumnName(pkMember, settings.getPkDynamicColumn())), pkMember, settings.getPkDynamicColumn());
					}
				}
			} else {
				// TODO: should throw an exception here ?
				log.error(
						"doInPreparedStatement - Null ids resultset. Something is wrong, the new entities will not have their ids assigned.");
				return res;
			}
			return res;
		}

	}
	
	private enum StatementHints {
		UPSERT, EXCLUDE_INPUT_STREAM_PROXIES
	}
}
