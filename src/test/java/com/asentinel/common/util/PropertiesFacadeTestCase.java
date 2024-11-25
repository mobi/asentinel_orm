package com.asentinel.common.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PropertiesFacadeTestCase {
	private static final Logger log = LoggerFactory.getLogger(PropertiesFacadeTestCase.class);
	
	private static final String EMPTY_VALUE = "  ";
	private static final String DATE_FORMAT = "MM/dd/yyyy";
	
	private static final String STRING_VALUE = "test";
	private static final boolean BOOLEAN_VALUE = true;
	private static final int INTEGER_VALUE = 10;
	private static final long LONG_VALUE = 11;
	private static final double DOUBLE_VALUE = 10.10;
	private static final float FLOAT_VALUE = 11.11f;
	
	private Date dateValue;
	private LocalDate localDateValue;
	
	private DateFormat df; 
	private DateTimeFormatter dtf;
	private Properties props;

	
	@Before
	public void setUp() {
		df = new SimpleDateFormat(DATE_FORMAT);
		dtf = DateTimeFormatter.ofPattern(DATE_FORMAT);
		try {
			dateValue = df.parse(df.format(new Date()));
		} catch (ParseException e) {
			throw new RuntimeException("Failed to create the test date.", e);
		}
		localDateValue = LocalDate.now();
		props = new Properties();
		
		props.setProperty("string.key", "  " + STRING_VALUE + "  ");
		props.setProperty("empty.string.key", EMPTY_VALUE);
		
		props.setProperty("boolean.key", "  " + (BOOLEAN_VALUE?"on":"off") + "  ");
		props.setProperty("empty.boolean.key", EMPTY_VALUE);
		
		props.setProperty("integer.key", "  " + INTEGER_VALUE + "  ");
		props.setProperty("empty.integer.key", EMPTY_VALUE);
		props.setProperty("invalid.integer.key", "xxx");

		props.setProperty("long.key", "  " + LONG_VALUE + "  ");
		props.setProperty("empty.long.key", EMPTY_VALUE);
		props.setProperty("invalid.long.key", "xxx");

		props.setProperty("double.key", "  " + DOUBLE_VALUE + "  ");
		props.setProperty("empty.double.key", EMPTY_VALUE);
		props.setProperty("invalid.double.key", "xxx");

		props.setProperty("float.key", "  " + FLOAT_VALUE + "  ");
		props.setProperty("empty.float.key", EMPTY_VALUE);
		props.setProperty("invalid.float.key", "xxx");
		
		props.setProperty("date.key", "  " + df.format(dateValue) + "  ");
		props.setProperty("empty.date.key", EMPTY_VALUE);
		props.setProperty("invalid.date.key", "xxx");
		
		try {
			props.setProperty("local.date.key", "  " + localDateValue.format(dtf) + "  ");
		} catch (DateTimeException e) {
			throw new RuntimeException("Failed to create the test date.", e);
		}
		props.setProperty("empty.local.date.key", EMPTY_VALUE);
		props.setProperty("invalid.local.date.key", "xxx");
	}
	
	@Test
	public void testGetProperties() throws ParseException {
		log.info("testGetProperties - start");
		PropertiesFacade facade = new PropertiesFacade(props);
		assertEquals(STRING_VALUE, facade.getString("string.key"));
		assertEquals("", facade.getString("string.empty.key"));
		assertEquals("default", facade.getString("string.empty.key", "default"));
		assertEquals("", facade.getString("inexistent"));
		assertEquals("default", facade.getString("inexistent", "default"));
		log.debug("testGetProperties - Passed for String");
		
		assertEquals(BOOLEAN_VALUE, facade.getBoolean("boolean.key"));
        assertFalse(facade.getBoolean("boolean.empty.key"));
        assertTrue(facade.getBoolean("boolean.empty.key", true));
        assertFalse(facade.getBoolean("boolean.empty.key", false));
        assertFalse(facade.getBoolean("inexistent"));
        assertTrue(facade.getBoolean("inexistent", true));
        assertFalse(facade.getBoolean("inexistent", false));
		log.debug("testGetProperties - Passed for boolean");
		
		assertEquals(INTEGER_VALUE, facade.getInt("integer.key"));
		assertEquals(0, facade.getInt("integer.empty.key"));
		assertEquals(15, facade.getInt("integer.empty.key", 15));
		assertEquals(0, facade.getInt("inexistent"));
		assertEquals(15, facade.getInt("inexistent", 15));
		assertEquals(0, facade.getInt("invalid.integer.key"));
		assertEquals(15, facade.getInt("invalid.integer.key", 15));
		log.debug("testGetProperties - Passed for int");

		assertEquals(LONG_VALUE, facade.getLong("long.key"));
		assertEquals(0, facade.getLong("long.empty.key"));
		assertEquals(15, facade.getLong("long.empty.key", 15));
		assertEquals(0, facade.getLong("inexistent"));
		assertEquals(15, facade.getLong("inexistent", 15));
		assertEquals(0, facade.getLong("invalid.long.key"));
		assertEquals(15, facade.getLong("invalid.long.key", 15));
		log.debug("testGetProperties - Passed for long");

		assertEquals(DOUBLE_VALUE, facade.getDouble("double.key"), 0.01);
		assertEquals(0d, facade.getDouble("double.empty.key"), 0.01);
		assertEquals(15d, facade.getDouble("double.empty.key", 15d), 0.01);
		assertEquals(0d, facade.getDouble("inexistent"), 0.01);
		assertEquals(15d, facade.getDouble("inexistent", 15d), 0.01);
		assertEquals(0d, facade.getDouble("invalid.double.key"), 0.01);
		assertEquals(15d, facade.getDouble("invalid.double.key", 15d), 0.01);
		log.debug("testGetProperties - Passed for double");

		assertEquals(FLOAT_VALUE, facade.getFloat("float.key"), 0.01);
		assertEquals(0f, facade.getFloat("float.empty.key"), 0.01);
		assertEquals(15f, facade.getFloat("float.empty.key", 15f), 0.01);
		assertEquals(0f, facade.getFloat("inexistent"), 0.01);
		assertEquals(15f, facade.getFloat("inexistent", 15f), 0.01);
		assertEquals(0f, facade.getFloat("invalid.float.key"), 0.01);
		assertEquals(15f, facade.getFloat("invalid.float.key", 15f), 0.01);
		log.debug("testGetProperties - Passed for float");

		Date defaultDate = df.parse(df.format(new Date()));
		assertEquals(dateValue, facade.getDate("date.key", df));
        assertNull(facade.getDate("date.empty.key", df));
		assertEquals(defaultDate, facade.getDate("date.empty.key", df, defaultDate));
        assertNull(facade.getDate("inexistent", df));
		assertEquals(defaultDate, facade.getDate("inexistent", df, defaultDate));
        assertNull(facade.getDate("invalid.date.key", df));
		assertEquals(defaultDate, facade.getDate("invalid.date.key", df, defaultDate));
		log.debug("testGetProperties - Passed for date");

		assertEquals(new BigDecimal(String.valueOf(DOUBLE_VALUE)), facade.getBigDecimal("double.key"));
		assertEquals(BigDecimal.ZERO, facade.getBigDecimal("double.empty.key"));
		assertEquals(BigDecimal.TEN, facade.getBigDecimal("double.empty.key", BigDecimal.TEN));
		assertEquals(BigDecimal.ZERO, facade.getBigDecimal("inexistent"));
		assertEquals(BigDecimal.TEN, facade.getBigDecimal("inexistent",  BigDecimal.TEN));
		assertEquals(BigDecimal.ZERO, facade.getBigDecimal("invalid.double.key"));
		assertEquals(BigDecimal.TEN, facade.getBigDecimal("invalid.double.key", BigDecimal.TEN));
		log.debug("testGetProperties - Passed for BigDecimal");
		
		assertEquals(new BigInteger(String.valueOf(INTEGER_VALUE)), facade.getBigInteger("double.key"));
		assertEquals(BigInteger.ZERO, facade.getBigInteger("double.empty.key"));
		assertEquals(BigInteger.TEN, facade.getBigInteger("double.empty.key", BigInteger.TEN));
		assertEquals(BigInteger.ZERO, facade.getBigInteger("inexistent"));
		assertEquals(BigInteger.TEN, facade.getBigInteger("inexistent",  BigInteger.TEN));
		assertEquals(BigInteger.ZERO, facade.getBigInteger("invalid.double.key"));
		assertEquals(BigInteger.TEN, facade.getBigInteger("invalid.double.key", BigInteger.TEN));
		log.debug("testGetProperties - Passed for BigInteger");
		
		LocalDate defaultLocalDate = LocalDate.now();
		assertEquals(localDateValue, facade.getLocalDate("local.date.key", dtf));
        assertNull(facade.getLocalDate("local.date.empty.key", dtf));
		assertEquals(defaultLocalDate, facade.getLocalDate("local.date.empty.key", dtf, defaultLocalDate));
        assertNull(facade.getLocalDate("inexistent", dtf));
		assertEquals(defaultLocalDate, facade.getLocalDate("inexistent", dtf, defaultLocalDate));
        assertNull(facade.getLocalDate("invalid.local.date.key", dtf));
		assertEquals(defaultLocalDate, facade.getLocalDate("invalid.local.date.key", dtf, defaultLocalDate));
		log.debug("testGetProperties - Passed for LocalDate");
		
		log.info("testGetProperties stop");
	}

	@Test
	public void testGetRequiredProperties() {
		log.info("testGetRequiredProperties start");
		PropertiesFacade facade = new PropertiesFacade(props);
		assertEquals(STRING_VALUE, facade.getRequiredString("string.key"));
		try {
			facade.getRequiredString("string.empty.key");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		try {
			facade.getRequiredString("inexistent");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		log.debug("testGetProperties - Passed for String");
		
		assertEquals(BOOLEAN_VALUE, facade.getRequiredBoolean("boolean.key"));
		try {
			facade.getRequiredBoolean("boolean.empty.key");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		try {
			facade.getRequiredBoolean("inexistent");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		log.debug("testGetProperties - Passed for boolean");
		
		assertEquals(INTEGER_VALUE, facade.getRequiredInt("integer.key"));
		try {
			facade.getRequiredInt("integer.empty.key");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		try {
			facade.getRequiredInt("inexistent");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		try {
			facade.getRequiredInt("invalid.integer.key");
			fail("Should not get to this point.");
		} catch (IllegalStateException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		log.debug("testGetProperties - Passed for int");
		
		assertEquals(LONG_VALUE, facade.getRequiredLong("long.key"));
		try {
			facade.getRequiredLong("long.empty.key");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		try {
			facade.getRequiredLong("inexistent");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		try {
			facade.getRequiredLong("invalid.long.key");
			fail("Should not get to this point.");
		} catch (IllegalStateException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		log.debug("testGetProperties - Passed for long");
		
		assertEquals(DOUBLE_VALUE, facade.getRequiredDouble("double.key"), 0.01);
		try {
			facade.getRequiredDouble("double.empty.key");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		try {
			facade.getRequiredDouble("inexistent");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		try {
			facade.getRequiredDouble("invalid.double.key");
			fail("Should not get to this point.");
		} catch (IllegalStateException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		log.debug("testGetProperties - Passed for double");

		assertEquals(FLOAT_VALUE, facade.getRequiredFloat("float.key"), 0.01);
		try {
			facade.getRequiredFloat("float.empty.key");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		try {
			facade.getRequiredFloat("inexistent");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		try {
			facade.getRequiredFloat("invalid.float.key");
			fail("Should not get to this point.");
		} catch (IllegalStateException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		log.debug("testGetProperties - Passed for float");
		

		assertEquals(dateValue, facade.getRequiredDate("date.key", df));
		try {
			facade.getRequiredDate("date.empty.key");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		try {
			facade.getRequiredDate("inexistent");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		try {
			facade.getRequiredDate("invalid.date.key");
			fail("Should not get to this point.");
		} catch (IllegalStateException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		log.debug("testGetProperties - Passed for date");
		
		assertEquals(new BigDecimal(String.valueOf(DOUBLE_VALUE)), facade.getRequiredBigDecimal("double.key"));
		try {
			facade.getRequiredBigDecimal("double.empty.key");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		try {
			facade.getRequiredBigDecimal("inexistent");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		try {
			facade.getRequiredBigDecimal("invalid.double.key");
			fail("Should not get to this point.");
		} catch (IllegalStateException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		log.debug("testGetProperties - Passed for BigDecimal");

		assertEquals(new BigInteger(String.valueOf(INTEGER_VALUE)), facade.getBigInteger("integer.key"));
		try {
			facade.getRequiredBigInteger("integer.empty.key");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		try {
			facade.getRequiredBigInteger("inexistent");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		try {
			facade.getRequiredBigInteger("invalid.integer.key");
			fail("Should not get to this point.");
		} catch (IllegalStateException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		log.debug("testGetProperties - Passed for BigInteger");
		
		assertEquals(localDateValue, facade.getRequiredLocalDate("local.date.key", dtf));
		try {
			facade.getRequiredLocalDate("local.date.empty.key");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		try {
			facade.getRequiredLocalDate("inexistent");
			fail("Should not get to this point.");
		} catch (MissingResourceException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		try {
			facade.getRequiredLocalDate("invalid.local.date.key");
			fail("Should not get to this point.");
		} catch (IllegalStateException e) {
			log.debug("testGetRequiredProperties - Expected exception: {}", e.getMessage());
		}
		log.debug("testGetProperties - Passed for LocalDate");

		
		log.info("testGetRequiredProperties stop");		
	}
	
	@Test
	public void testPropertyExists() {
		PropertiesFacade facade = new PropertiesFacade(props);
		
		log.debug("testPropertyExists - Test existent key");
        assertTrue(facade.propertyExists("string.key"));
		
		log.debug("testPropertyExists - Test inexistent key");
        assertFalse(facade.propertyExists("inexistent.key"));
		
		log.debug("testPropertyExists - Test for null parameter");
		try {
			facade.propertyExists(null);
			fail("Should not get to this point.");
		} catch (NullPointerException e) {
			log.debug("testPropertyExists - Expected exception: {}", e.getMessage());
		}
	}
}
