package com.asentinel.common.orm.persist;

/**
 * Interface to be implemented by entities that need to be notified whether an {@code upsert}
 * statement for them created a new row or updated an existing row in the target
 * database table.
 * 
 * @see Updater
 * @see Updater#upsert(Object, Object...)
 * @see NewRowOnUpsertDetector
 * 
 * @author Razvan.Popian
 */
public interface NewRowOnUpsertAware {

	void setNewRow(boolean newRow);
}
