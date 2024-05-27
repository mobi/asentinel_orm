package com.asentinel.common.jdbc.flavors.postgres;

import java.util.Map;

import org.postgresql.util.PGobject;

import com.asentinel.common.orm.persist.NewRowOnUpsertDetector;

/**
 * @see NewRowOnUpsertDetector
 * 
 * @author Razvan.Popian
 */
public class PgNewRowOnUpsertDetector implements NewRowOnUpsertDetector {
	private static final String XMAX = "xmax";
	private static final String[] COLUMNS = new String[] {XMAX}; 

	@Override
	public String[] getColumns() {
		return COLUMNS;
	}

	@Override
	public boolean isNewRow(Map<String, Object> columnValues) {
		PGobject xmax = (PGobject) columnValues.get(XMAX);
		if (xmax != null) {
			return "0".equals(xmax.getValue());
		}
		throw new IllegalArgumentException("Invalid XMAX column value. "
				+ "Can not detect the type of operation performed by the upsert statement.");
	}

}
