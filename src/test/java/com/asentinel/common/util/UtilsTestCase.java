package com.asentinel.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

import org.junit.Test;

public class UtilsTestCase {
	LocalDateTime d = LocalDateTime.now();

	@Test
	public void testTemporalToDate_LocalDate() {
		assertEquals(Utils.toDate(d.toLocalDate()), Utils.toDate((Temporal) d.toLocalDate()));
	}
	
	@Test
	public void testTemporalToDate_LocalTime() {
		assertEquals(Utils.toDate(d.toLocalTime()), Utils.toDate((Temporal) d.toLocalTime()));
	}

	@Test
	public void testTemporalToDate_LocalDateTime() {
		assertEquals(Utils.toDate(d), Utils.toDate((Temporal) d));
	}

	@Test
	public void testTemporalToDate_Null() {
		assertNull(Utils.toDate((Temporal) null));
	}

	@Test
	public void testTemporalToDate_Unsupported() {
		org.junit.Assert.assertNotNull(Utils.toDate(ZonedDateTime.now()));
	}

}
