package com.asentinel.common.jdbc.flavors.postgres;

import java.util.HashSet;
import java.util.Set;

import com.asentinel.common.jdbc.ReusableRowMappers;
import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.flavors.TableIntrospector;
import com.asentinel.common.util.Assert;
import com.asentinel.common.util.ConcurrentCache;

/**
 * {@code TableIntrospector} implementation for Postgres databases.
 * 
 * @see TableIntrospector
 * 
 * @since 1.59
 * @author Razvan Popian
 */
public class PgTableIntrospector implements TableIntrospector {
	static final String SELECT_META = 
			"SELECT lower(column_name)" + 
			" FROM information_schema.columns" + 
			" WHERE table_schema = 'public'" + 
			" AND table_name = ?";

	private final SqlQuery queryEx;
	private final ConcurrentCache<String, Set<String>> columnsCache = new ConcurrentCache<>(); 
	

	public PgTableIntrospector(SqlQuery queryEx) {
		Assert.assertNotNull(queryEx, "queryEx");
		this.queryEx = queryEx;
	}

	@Override
	public Set<String> getColumns(String table) {
		return columnsCache.get(table.toLowerCase(),
			() -> new HashSet<>(queryEx.query(SELECT_META, ReusableRowMappers.ROW_MAPPER_STRING, table.toLowerCase()))
		);
	}

}
