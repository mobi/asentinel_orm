package com.asentinel.common.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ListUtilsTestCase {
	private static final Logger log = LoggerFactory.getLogger(ListUtilsTestCase.class);
	
	@Test
	public void testMakeMultiRowList() {
		log.info("testMakeMultiRowList start");
		List<Integer> list = new ArrayList<Integer>();
		try {
			ListUtils.toMultiRowList(list, 0);
            fail("Should not get here.");
		} catch (Exception e) {
			//do nothing, exception expected
		}
		for (int c=1; c <= 10; c++) {
			for(int i=0; i<10; i++) {
				int rowCount = list.size() / c + (list.size() % c != 0?1:0);
				List<List<Integer>> rows = ListUtils.toMultiRowList(list, c);
				assertNotNull("Null rows", rows);
				assertEquals(rowCount, rows.size());
				list.add(i);
			}
		}
		log.info("testMakeMultiRowList stop");
	}
	
	
	@Test
	public void testMakeMultiRowListSymetric() {
		log.info("testMakeMultiRowListSymetric start");
		final int columns = 4;
		List<Integer> list = new ArrayList<Integer>();
		List<List<Integer>> result = ListUtils.toMultiRowListSymetric(list, columns);
		assertEquals(0, result.size());
		for (int i=0; i < 2 * columns; i++) {
			list.add(i);
			result = ListUtils.toMultiRowListSymetric(list, columns);
			List<Integer> lastRow = result.get(result.size() - 1);
			assertEquals(columns, lastRow.size());
		}
		
		log.info("testMakeMultiRowListSymetric stop");
	}
	
	@Test
	public void testToList() {
		log.info("testToList start");
		Integer[] values = {0, 1, 2, 3};
		String csvString = "";
		final String DELIMITER = ",";
		for (int i=0; i<values.length; i++) {
			csvString += i + DELIMITER; 
		}
		csvString = csvString.substring(0, csvString.length() - DELIMITER.length());
		
		// string
		List<String> listString = ListUtils.toList(csvString, String.class);
		assertEquals(values.length, listString.size());
		for (int i=0; i<values.length; i++) {
			assertEquals(String.valueOf(values[i]), listString.get(i));
		}
		log.debug("testToList done for strings");

		// integer
		List<Integer> listInteger = ListUtils.toList(csvString, Integer.class);
		assertEquals(values.length, listInteger.size());
		for (int i=0; i<values.length; i++) {
			assertEquals(values[i], listInteger.get(i));
		}
		log.debug("testToList done for ints");
		
		// long
		List<Long> listLong = ListUtils.toList(csvString, Long.class);
		assertEquals(values.length, listLong.size());
		for (int i=0; i<values.length; i++) {
			assertEquals(values[i].intValue(), listLong.get(i).intValue());
		}
		log.debug("testToList done for longs");
		
		// empty string
		listString = ListUtils.toList("", String.class);
		assertEquals(1, listString.size());
		
		listInteger = ListUtils.toList("", Integer.class);
		assertEquals(0, listInteger.size());
		log.debug("testToList done for empty string");

		listLong = ListUtils.toList("", Long.class);
		assertEquals(0, listLong.size());
		log.debug("testToList done for empty string");
		
		// invalid ints
		csvString = "1,aaa";
		try {
			ListUtils.toList(csvString, Integer.class);
			fail("Should not get here.");
		} catch(NumberFormatException e) {
			log.debug("Expected exception: {}", e.getMessage());
		}
		log.debug("testToList done for invalid ints");

		// empty strings
		csvString = ",,,,";
		listString = ListUtils.toList(csvString, String.class);
		for (String s:listString) {
			assertEquals("", s);
		}
		log.debug("testToList done for empty strings");

		
		log.info("testToList stop");
	}

}
