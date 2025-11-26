package com.asentinel.common.util;

import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Date;

import static org.junit.Assert.*;

public class UtilsTestCase {

    @Test
    public void temporalToDate_Null() {
        assertNull(Utils.toDate((Temporal) null));
    }

	@Test
	public void temporalToDate_LocalDate() {
        LocalDate localDate = LocalDate.now();
        Date expected = Utils.toDate(localDate);
		assertEquals(expected, Utils.toDate((Temporal) localDate));
	}
	
	@Test
	public void temporalToDate_LocalTime() {
        LocalTime localTime = LocalTime.now();
        Timestamp expected = Utils.toTimestamp(localTime);
		assertEquals(expected, Utils.toDate((Temporal) localTime));
	}

	@Test
	public void temporalToDate_LocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        Timestamp expected = Utils.toTimestamp(localDateTime);
        assertEquals(expected, Utils.toDate((Temporal) localDateTime));
	}

	@Test
	public void temporalToDate_ZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        Timestamp expected = Utils.toTimestamp(zonedDateTime);
        assertEquals(expected, Utils.toDate(zonedDateTime));
	}
	
	@Test
	public void temporalToDate_Instant() {
        Instant instant = Instant.now();
        Timestamp expected = Utils.toTimestamp(instant);
        assertEquals(expected, Utils.toDate(instant));
	}

    @Test(expected = IllegalArgumentException.class)
    public void temporalToDate_UnsupportedType() {
        Utils.toDate(OffsetTime.now());
    }
}
