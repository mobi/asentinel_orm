package com.asentinel.common.orm.persist;

import java.util.Arrays;
import java.util.Collection;

import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumn;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumnsEntity;

/**
 * Interface implemented by classes that are able to persist entities in the
 * database. Whether the persistence mechanism is deep (children entities are
 * persisted also) or not is an implementation detail.<br>
 * <br>
 * 
 * The main methods of this interface are:
 * <li>{@link Updater#update(Object)} - for saving a single entity.
 * <li>{@link Updater#upsert(Object, Object ...)} - performs an atomic insert or
 * update.
 * <li>{@link Updater#update(Collection)} - for saving a collection of entities.
 * <li>{@link Updater#upsert(Collection, Object...)} - for performing upserts
 * for a collection of entities. <br>
 * <br>
 * 
 * These should be good enough for most use cases. The other methods allow
 * forcing either insert or update SQL statements or allow inserts/updates for
 * entities that support dynamic columns (implementations of
 * {@link DynamicColumnsEntity}). <br>
 * <br>
 * See this link for details on the upsert methods in this class
 * https://en.wikipedia.org/wiki/Merge_(SQL). <br>
 * 
 * <br>
 * This is not intended to be implemented in the user code.
 * 
 * @see Table
 * @see PkColumn
 * @see Column
 * @see DynamicColumn
 * @see DynamicColumnsEntity
 * @see UpdateSettings
 * @see NewEntityDetector
 * @see EntityUtils
 * 
 * @author Razvan Popian
 */
public interface Updater {
	
	/**
	 * Saves the target entity in the database. It auto detects if it needs to
	 * perform an <code>insert</code> or an <code>update</code> usually by using a
	 * {@link NewEntityDetector} implementation. <br>
	 * <b>If a new record is created in the database and a new primary key is
	 * generated, this primary key should be set in the id field of the entity.</b>
	 * 
	 * @param entity the object to persist in the database.
	 * 
	 * @return the number of records affected by the operation.
	 */
	public default int update(Object entity) {
		return update(entity, UpdateType.AUTO);
	}
	
	/**
	 * Saves the target entity in the database performing either an
	 * <code>insert</code>, an <code>insert</code> with auto id generation or an
	 * <code>update</code> depending on the <code>updateType</code> parameter. If
	 * the <code>updateType</code> is {@link UpdateType#AUTO} the type of SQL
	 * statement is selected based on whether the entity is new or not. <br>
	 * <b>If a new record is created in the database and a new primary key is
	 * generated, this primary key should be set in the id field of the entity.</b>
	 * 
	 * @param entity     the object to persist in the database.
	 * @param updateType the type of update (can be <code>insert</code> or
	 *                   <code>insert</code> with auto id generation or
	 *                   <code>update</code> or <code>auto</code> - in this case the
	 *                   implementer detects the type of statement to create for
	 *                   each entity).
	 * 
	 * @return the number of records affected by the operation.
	 * 
	 * @see UpdateType
	 */
	public default int update(Object entity, UpdateType updateType) {
		return update(entity, new UpdateSettings<DynamicColumn>(updateType));
	}
	
	/**
	 * Saves the target entity in the database performing either an
	 * <code>insert</code>, an <code>insert</code> with auto id generation or an
	 * <code>update</code> depending on the <code>updateType</code> member of the
	 * {@code settings} parameter. If the <code>updateType</code> is
	 * {@link UpdateType#AUTO} the type of SQL statement is selected based on
	 * whether the entity is new or not. <br>
	 * <b>If a new record is created in the database and a new primary key is
	 * generated, this primary key should be set in the id field of the entity.</b>
	 * 
	 * @param entity   the object to persist in the database.
	 * @param settings {@code UpdateSettings} instance that can store additional
	 *                 information about the update requested like the collection of
	 *                 dynamic columns. Note that if dynamic columns are present the
	 *                 {@code entity} parameter must implement
	 *                 {@link DynamicColumnsEntity}
	 * 
	 * @return the number of records affected by the operation.
	 * 
	 * @see UpdateSettings
	 */
	public int update(Object entity, UpdateSettings<? extends DynamicColumn> settings); 	

	/**
	 * Performs an atomic insert or update depending on a constraint defined for the
	 * target entity on the database side. <b>The entity id is always updated with
	 * the value of the database record primary key regardless if an insert or
	 * update is performed.</b>
	 * 
	 * @param entity the object to persist in the database.
	 * @param hints  database specific arguments needed to create the upsert SQL
	 *               string. The number of hints must be even because they are
	 *               interpreted as key-value pairs. For example the following call
	 *               should be used for PostgresSQL (for this database a set of
	 *               columns or a constraint has to be specified, see the
	 *               PostgresSQL insert documentation):
	 * 
	 *               <pre>
	 *               orm.upsert(e, PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(UniqueId)");
	 *               </pre>
	 * 
	 * @see #upsert(Object, UpdateType, Object...)
	 */
	public default int upsert(Object entity, Object ... hints) {
		return upsert(entity, UpdateType.INSERT_AUTO_ID, hints);
	}

	
	/**
	 * Performs an atomic insert or update depending on a constraint defined for the
	 * target entity on the database side. <b>The entity id is always updated with
	 * the value of the database record primary key regardless if an insert or
	 * update is performed.</b>
	 * 
	 * @param entity           the object to persist in the database.
	 * @param updateTypeInsert can be either {@code UpdateType#INSERT} or
	 *                         {@code UpdateType#INSERT_AUTO_ID}. Use
	 *                         {@code UpdateType#INSERT} only if the primary key is
	 *                         not a sequence or autonumber. Other values will be
	 *                         considered equivalent to
	 *                         {@code UpdateType#INSERT_AUTO_ID}
	 * @param hints            database specific arguments needed to create the
	 *                         upsert SQL string. The number of hints must be even
	 *                         because they are interpreted as key-value pairs. For
	 *                         example the following call should be used for
	 *                         PostgresSQL (for this database a set of columns or a
	 *                         constraint has to be specified, see the PostgresSQL
	 *                         insert documentation):
	 * 
	 *                         <pre>
	 *                         orm.upsert(e, UpdateType.INSERT, PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(UniqueId)");
	 *                         </pre>
	 * 
	 * @return the number of records affected by the operation.
	 * 
	 * @see UpdateType
	 */
	public default int upsert(Object entity, UpdateType updateTypeInsert, Object ... hints) {
		return upsert(entity, new UpdateSettings<DynamicColumn>(updateTypeInsert), hints);
	}

	/**
	 * Performs an atomic insert or update depending on a constraint defined for the
	 * target entity on the database side. <b>The entity id is always updated with
	 * the value of the database record primary key regardless if an insert or
	 * update is performed.</b>
	 * 
	 * @param entity   the object to persist in the database.
	 * @param settings {@code UpdateSettings} instance that can store additional
	 *                 information about the update requested like the collection of
	 *                 dynamic columns. Note that if dynamic columns are present the
	 *                 {@code entity} parameter must implement
	 *                 {@link DynamicColumnsEntity}
	 * @param hints    database specific arguments needed to create the upsert SQL
	 *                 string. The number of hints must be even because they are
	 *                 interpreted as key-value pairs. For example the following
	 *                 call should be used for PostgresSQL (for this database a set
	 *                 of columns or a constraint has to be specified, see the
	 *                 PostgresSQL insert documentation):
	 * 
	 *                 <pre>
	 *                 orm.upsert(e, UpdateType.INSERT, PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(UniqueId)");
	 *                 </pre>
	 * 
	 * @return the number of records affected by the operation.
	 * 
	 * @see UpdateSettings
	 */
	public int upsert(Object entity, UpdateSettings<? extends DynamicColumn> settings, Object ... hints);	

	
	
	// TODO: add a return type, to inform the client about the number of rows updated
	/**
	 * Saves the entities collection in the database auto detecting for each entity
	 * if an insert or update SQL statement has to be executed, usually by using a
	 * {@link NewEntityDetector} implementation. The statements should be executed
	 * using JDBC batching for maximum performance.<br>
	 * <b>If new records are created in the database and new primary keys are
	 * generated these primary keys should be set in the id fields of the
	 * corresponding entities.</b>
	 * 
	 * @param entities collection of entities to be updated. All entities in the
	 *                 collection must have the same type, otherwise unpredictable
	 *                 results may occur.
	 *
	 * @see #update(Collection, UpdateType)
	 * @see NewEntityDetector
	 * @see UpdateType
	 */
	public default <E> void update(Collection<E> entities) {
		update(entities, UpdateType.AUTO);
	}
	
	// TODO: add a return type, to inform the client about the number of rows updated
	/**
	 * Saves the entities collection in the database performing a SQL statement
	 * based on the <code>updateType</code> parameter. The statements should be
	 * executed using JDBC batching for maximum performance. <br>
	 * <b>If new records are created in the database and new primary keys are
	 * generated, these primary keys should be set in the id fields of the
	 * corresponding entities.</b>
	 * 
	 * @param entities   collection of entities to be updated. All entities in the
	 *                   collection must have the same type, otherwise unpredictable
	 *                   results may occur.
	 * @param updateType the type of update (can be <code>insert</code> or
	 *                   <code>insert</code> with auto id generation or
	 *                   <code>update</code> or <code>auto</code> - in this case the
	 *                   implementer detects the type of statement to create for
	 *                   each entity).
	 * 
	 * @see #update(Collection)
	 * @see NewEntityDetector
	 * @see UpdateType
	 */
	public default <E> void update(Collection<E> entities, UpdateType updateType) {
		update(entities, new UpdateSettings<DynamicColumn>(updateType));
	}
	
	/**
	 * Saves the entities collection in the database performing a SQL statement
	 * based on the <code>updateType</code> parameter. The statements should be
	 * executed using JDBC batching for maximum performance. <br>
	 * <b>If new records are created in the database and new primary keys are
	 * generated, these primary keys should be set in the id fields of the
	 * corresponding entities.</b>
	 * 
	 * @param entities   collection of entities to be updated. All entities in the
	 *                   collection must have the same type, otherwise unpredictable
	 *                   results may occur.
	 * @param settings   {@code UpdateSettings} instance that can store additional
	 *                   information about the update requested like the collection
	 *                   of dynamic columns. Note that if dynamic columns are
	 *                   present the entities in the {@code entities} collection
	 *                   parameter must implement {@link DynamicColumnsEntity}
	 * 
	 * @see #update(Collection)
	 * @see NewEntityDetector
	 * @see UpdateSettings
	 */
	public <E> void update(Collection<E> entities, UpdateSettings<? extends DynamicColumn> settings);	
	
	/**
	 * @see #update(Collection)
	 */
	public default <E> void update(@SuppressWarnings("unchecked") E ... entities) {
		if (entities == null || entities.length == 0) {
			return;
		}
		this.update(Arrays.asList(entities));
	}

	/**
	 * Saves the entities collection in the database performing upserts. The
	 * statements should be executed using JDBC batching for maximum performance.
	 * <br>
	 * <b>This method assures that primary keys generated in the database are
	 * populated in each entity from the collection</b>
	 *
	 * @param entities         collection of entities to be updated. All entities in
	 *                         the collection must have the same type, otherwise
	 *                         unpredictable results may occur.
	 * @param hints            database specific arguments needed to create the
	 *                         upsert SQL string. The number of hints must be even
	 *                         because they are interpreted as key-value pairs. For
	 *                         example the following call should be used for
	 *                         PostgresSQL (for this database a set of columns or a
	 *                         constraint has to be specified, see the PostgresSQL
	 *                         insert documentation):
	 * 
	 *                         <pre>
	 *                         orm.upsert(e, PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(UniqueId)");
	 *                         </pre>
	 * 
	 * @see #upsert(Collection, UpdateType, Object...)
	 */
	public default <E> void upsert(Collection<E> entities, Object ... hints) {
		upsert(entities, UpdateType.INSERT_AUTO_ID, hints);
	}
	
	// TODO: add a return type, to inform the client about the number of rows updated
	/**
	 * Saves the entities collection in the database performing upserts. The
	 * statements should be executed using JDBC batching for maximum performance.
	 * <br>
	 * <b>This method assures that primary keys generated in the database are
	 * populated in each entity from the collection</b>
	 * 
	 * @param entities         collection of entities to be updated. All entities in
	 *                         the collection must have the same type, otherwise
	 *                         unpredictable results may occur.
	 * @param updateTypeInsert can be either {@code UpdateType#INSERT} or
	 *                         {@code UpdateType#INSERT_AUTO_ID}. Use
	 *                         {@code UpdateType#INSERT} only if the primary key is
	 *                         not a sequence or autonumber. Other values will be
	 *                         considered equivalent to
	 *                         {@code UpdateType#INSERT_AUTO_ID}
	 * @param hints            database specific arguments needed to create the
	 *                         upsert SQL string. The number of hints must be even
	 *                         because they are interpreted as key-value pairs. For
	 *                         example the following call should be used for
	 *                         PostgresSQL (for this database a set of columns or a
	 *                         constraint has to be specified, see the PostgresSQL
	 *                         insert documentation):
	 * 
	 *                         <pre>
	 *                         orm.upsert(e, UpdateType.INSERT, PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(UniqueId)");
	 *                         </pre>
	 * 
	 * @see #update(Collection)
	 * @see NewEntityDetector
	 * @see UpdateType
	 */
	public default <E> void upsert(Collection<E> entities, UpdateType updateTypeInsert, Object ... hints) {
		upsert(entities, new UpdateSettings<DynamicColumn>(updateTypeInsert), hints);
	}
	
	
	/**
	 * Saves the entities collection in the database performing upserts. The
	 * statements should be executed using JDBC batching for maximum performance.
	 * <br>
	 * <b>This method assures that primary keys generated in the database are
	 * populated in each entity from the collection</b>
	 * 
	 * @param entities collection of entities to be updated. All entities in the
	 *                 collection must have the same type, otherwise unpredictable
	 *                 results may occur.
	 * @param settings {@code UpdateSettings} instance that can store additional
	 *                 information about the update requested like the collection of
	 *                 dynamic columns. Note that if dynamic columns are present the
	 *                 entities in the {@code entities} collection parameter must
	 *                 implement {@link DynamicColumnsEntity}
	 * @param hints    database specific arguments needed to create the upsert SQL
	 *                 string. The number of hints must be even because they are
	 *                 interpreted as key-value pairs. For example the following
	 *                 call should be used for PostgresSQL (for this database a set
	 *                 of columns or a constraint has to be specified, see the
	 *                 PostgresSQL insert documentation):
	 * 
	 *                 <pre>
	 *                 orm.upsert(e, new UpdateSettings<DynamicColumn>(UpdateType.INSERT),
	 *                 		PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(UniqueId)");
	 *                 </pre>
	 * 
	 * @see #update(Collection)
	 * @see NewEntityDetector
	 * @see UpdateSettings
	 */
	public <E> void upsert(Collection<E> entities, UpdateSettings<? extends DynamicColumn> settings, Object ... hints);
	

	/**
	 * @see #upsert(Collection, Object...)
	 */
	public default <E> void upsert(E[] entities, Object ... hints) {
		if (entities == null || entities.length == 0) {
			return;
		}
		upsert(Arrays.asList(entities), hints);
	}

	/**
	 * Deletes the entities of the specified type having the specified ids.
	 * 
	 * @param entityType the type of the entity to delete.
	 * @param ids        the ids of the entities to be deleted.
	 * 
	 * @return the number of records affected by the operation.
	 */
	public int delete(Class<?> entityType, Object ... ids);
	
}
