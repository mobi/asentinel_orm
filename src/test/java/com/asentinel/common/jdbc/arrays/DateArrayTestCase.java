package com.asentinel.common.jdbc.arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class DateArrayTestCase {
	
	private Calendar[] cals;
	private Date[] dates;
	
	@Before
	public void setup() {
		cals = new Calendar[]{Calendar.getInstance(), Calendar.getInstance(), null};
		cals[1].add(Calendar.DAY_OF_MONTH, 1);
		dates = new Date[] {cals[0].getTime(), cals[1].getTime(), null};
	}
	
	
	@Test
	public void testDateArrayConstructor() {
		DateArray a = new DateArray("test", dates);
		
		Timestamp[] timestamps = (Timestamp[]) a.getObjects();
		assertEquals(this.dates.length, timestamps.length);
		assertEquals(this.dates[0].getTime(), timestamps[0].getTime());
		assertEquals(this.dates[1].getTime(), timestamps[1].getTime());
		assertNull(timestamps[2]);
	}

	@Test
	public void testDateCollectionConstructor() {
		DateArray a = new DateArray("test", Arrays.asList(dates));
		
		Timestamp[] timestamps = (Timestamp[]) a.getObjects();
		assertEquals(this.dates.length, timestamps.length);
		assertEquals(this.dates[0].getTime(), timestamps[0].getTime());
		assertEquals(this.dates[1].getTime(), timestamps[1].getTime());
		assertNull(timestamps[2]);
	}


	@Test
	public void testCalendarArrayConstructor() {
		DateArray a = new DateArray("test", cals);
		
		Timestamp[] timestamps = (Timestamp[]) a.getObjects();
		assertEquals(this.dates.length, timestamps.length);
		assertEquals(this.dates[0].getTime(), timestamps[0].getTime());
		assertEquals(this.dates[1].getTime(), timestamps[1].getTime());
		assertNull(timestamps[2]);
	}

	@Test
	public void testCalendarArrayFactoryMethod() {
		DateArray a = DateArray.getInstance("test", Arrays.asList(cals));
		
		Timestamp[] timestamps = (Timestamp[]) a.getObjects();
		assertEquals(this.dates.length, timestamps.length);
		assertEquals(this.dates[0].getTime(), timestamps[0].getTime());
		assertEquals(this.dates[1].getTime(), timestamps[1].getTime());
		assertNull(timestamps[2]);
	}
	
}
