package com.asentinel.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

import org.junit.Test;

public class UtilsTestCase {
	private final LocalDateTime d = LocalDateTime.now();

	@Test
	public void temporalToDate_LocalDate() {
		assertEquals(Utils.toDate(d.toLocalDate()), Utils.toDate((Temporal) d.toLocalDate()));
	}
	
	@Test
	public void temporalToDate_LocalTime() {
		assertEquals(Utils.toDate(d.toLocalTime()), Utils.toDate((Temporal) d.toLocalTime()));
	}

	@Test
	public void temporalToDate_LocalDateTime() {
		assertEquals(Utils.toDate(d), Utils.toDate((Temporal) d));
	}

	@Test
	public void temporalToDate_Null() {
		assertNull(Utils.toDate((Temporal) null));
	}

	@Test
	public void temporalToDate_ZonedDateTime() {
		assertNotNull(Utils.toDate(ZonedDateTime.now()));
	}
	
	@Test
	public void temporalToDate_Instant() {
		assertNotNull(Utils.toDate(Instant.now()));
	}

}
