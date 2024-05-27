package com.asentinel.common.jdbc.flavors.postgres;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PostgresSqlTemplatesTestCase {

	private final PostgresSqlTemplates t = new PostgresSqlTemplates();
	
	@Test(expected = IllegalArgumentException.class)
	public void getSqlForUpsertNoConflictHint() {
		t.getSqlForUpsert("INSERT", "UPDATE");
	}

	@Test(expected = IllegalArgumentException.class)
	public void getSqlForUpsertNoValueForConflict1() {
		t.getSqlForUpsert("INSERT", "UPDATE", PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER);
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void getSqlForUpsertNoValueForConflict2() {
		t.getSqlForUpsert("INSERT", "UPDATE", 1, 2, PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER);
	}

	@Test
	public void getSqlForUpsert() {
		String s = t.getSqlForUpsert("INSERT", "UPDATE", PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(name)");
		assertEquals("INSERTonconflict(name)doUPDATE", s.replaceAll("\\s", ""));
	}
	
}
