package com.asentinel.common.orm.persist;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableCollection;

import java.util.Collection;

import com.asentinel.common.orm.mappers.dynamic.DynamicColumn;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumnsEntity;

/**
 * Holder of configuration information for the methods in the {@link Updater}
 * interface. The following things can be configured in the instances of this
 * class:
 * <li>the type of update to perform
 * <li>the collection of dynamic columns a certain entity class supports 
 * <li>the name of the primary key column if that has to be dynamically specified
 * <li> the name of the target table if that has to be dynamically specified
 * <br>
 * <br>
 * 
 * In the future additional fields may be added. This design ensures that the
 * {@code Updater} interface does not have to change much because of a new
 * feature.
 * 
 * @see Updater
 * @see SimpleUpdater
 * @see DynamicColumn
 * @see DynamicColumnsEntity
 * 
 * @author Razvan.Popian
 */
public class UpdateSettings<T extends DynamicColumn> {

	private final UpdateType updateType;
	private final Collection<T> dynamicColumns;
	private final DynamicColumn pkDynamicColumn;
	private final String table;
	
	// if you add new members make sure you update the #setUpdateType method

	public UpdateSettings(Collection<T> dynamicColumns) {
		this(dynamicColumns, null);
	}
	
	public UpdateSettings(Collection<T> dynamicColumns, String table) {
		this(UpdateType.AUTO, dynamicColumns, null, table);
	}
	
	
	public UpdateSettings(Collection<T> dynamicColumns, DynamicColumn pkDynamicColumn, String table) {
		this(null, dynamicColumns, pkDynamicColumn, table);
	}


	
	
	public UpdateSettings(UpdateType updateType) {
		this(updateType, emptyList());
	}
	
	public UpdateSettings(UpdateType updateType, Collection<T> dynamicColumns) {
		this(updateType, dynamicColumns, null, null);
	}

	public UpdateSettings(UpdateType updateType, Collection<T> dynamicColumns, DynamicColumn pkDynamicColumn, String table) {
		if (updateType == null) {
			updateType = UpdateType.AUTO;
		}
		if (dynamicColumns == null) {
			dynamicColumns = emptyList();
		}
		this.updateType = updateType;
		this.dynamicColumns = dynamicColumns;
		this.pkDynamicColumn = pkDynamicColumn;
		this.table = table;
	}

	
	
	public UpdateType getUpdateType() {
		return updateType;
	}	

	public Collection<T> getDynamicColumns() {
		return unmodifiableCollection(dynamicColumns);
	}
	
	public DynamicColumn getPkDynamicColumn() {
		return pkDynamicColumn;
	}
	
	public String getTable() {
		return table;
	}
	
	UpdateSettings<T> setUpdateType(UpdateType updateType) {
		return new UpdateSettings<>(updateType, this.getDynamicColumns(), pkDynamicColumn, table);
	}
	
	@Override
	public String toString() {
		return "UpdateSettings [updateType=" + updateType 
				+ ", dynamicColumns=" + dynamicColumns 
				+ ", pkDynamicColumn=" + pkDynamicColumn
				+ ", table=" + table
				+ "]";
	}

}
