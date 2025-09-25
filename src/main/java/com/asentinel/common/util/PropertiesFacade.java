package com.asentinel.common.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Facade for a {@link java.util.Properties} object that allows the client to
 * convert properties to java objects or basic types. The properties values
 * support the following placeholders:
 * <li><code>${some.system.property}</code> the value between the braces will be
 * looked up among the system properties. You can define a system property by
 * passing a {@code -D} command line argument to the JVM.
 * <li><code>${some.system.property:some default value}</code> same as the
 * above, but allows defining a default value that will be used if the property
 * can not be found in the system properties. <br><br>
 * 
 * Because java.util.Properties is thread safe this class is also thread safe.
 * 
 * @author Razvan Popian
 * @author Bogdan Ravdan
 */
public class PropertiesFacade {
	private static final Logger log = LoggerFactory.getLogger(PropertiesFacade.class);
	
	private final static Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{.+\\}");
	
	private final Properties properties;
	
	/**
	 * Constructor.
	 * @param properties the {@link java.util.Properties} instance
	 * 			to be wrapped.
	 */
	public PropertiesFacade(Properties properties) {
		Assert.assertNotNull(properties, "properties");
		this.properties = properties;
	}


	/** @return the wrapped {@link java.util.Properties} instance. */
	public Properties getProperties() {
		return properties;
	}
	
	/**
	 * Checks if the specified property exists. We don't care about the value. Note
	 * that for properties referencing system properties with the syntax
	 * <code>${some.system.property}</code> this method will not check if the actual
	 * value exists in the system properties. So for this case this method always
	 * returns {@code true}.
	 * 
	 * @param name - a not-null <code>String</code> specifying the name of the
	 *             property
	 * @return a boolean indicating if the property was found
	 */
	public boolean propertyExists(String name) {
		Assert.assertNotNull(name, "name");
		
		String s = properties.getProperty(name);
		return s != null;
	}
	
	/**
	 * Returns the value of the specified property or throws an exception if the
	 * property is missing. Supports the placeholder mentioned at the class level
	 * documentation.
	 * 
	 * @param name the name of the property to retrieve.
	 * @return the value of the property with the specified name.
	 * @throws MissingResourceException for one of the following: - the property is
	 *                                  not defined, - the property is defined, but
	 *                                  its value is empty string or white spaces
	 *                                  only.
	 */
	public String getRequiredString(String name) throws MissingResourceException {
		Assert.assertNotNull(name, "name");
		String s = properties.getProperty(name);
		if (s != null && PLACEHOLDER_PATTERN.matcher(s).matches()) {
			String content = s.substring(2, s.length() - 1);
			int separatorIndex = content.indexOf(':');
			String envName;
			String defaultValue;
			if (separatorIndex < 0) {
				envName = content;
				defaultValue = null;
			} else {
				envName = content.substring(0, separatorIndex); 
				defaultValue = content.substring(separatorIndex + 1);
			}
			
			s = System.getProperty(envName);
			if (s == null) {
				if (StringUtils.hasText(defaultValue)) {
					s = defaultValue;
				} else {
					log.warn("getRequiredString - Property '{}' references the system property '{}', but that can not be found. "
							+ "Pass the property to the JVM using the '-D{}=value' command line argument.", name, envName, envName);
				}
			}
		}
    	if (StringUtils.hasText(s)) {
    		return s.trim();
    	} else {
    		throw new MissingResourceException("Missing required property '" + name + "'.", 
    				String.class.getName(), name);
    	}
	}
	
	
	/**
     * @param name
     * @param defaultValue
     * @return the value of the property with the provided <code>name</code> as String or 
     * 		   <code>defaultValue</code> if the property does not exist or 
     * 			is empty string or has only white spaces.  
     */
    public String getString(String name, String defaultValue) {
    	try {
    		return getRequiredString(name);
    	} catch (MissingResourceException e) {
    		return defaultValue;
    	}
    }
    
    /**
     * @param name
     * @return the value of the property with the provided <code>name</code> as String or
     * 			empty string if the property does not exist
     * 			or is empty string or has only white spaces.  
     */
    public String getString(String name) {
    	return getString(name, "");
    }
    
    /**
     * @return the value of the specified property or throws an exception if
     * the property is missing. This method used the {@link #getRequiredString(String)} 
     * method to retrieve the value of the property as String first. Returns true if 
     * the value is one of: yes, y, true, on.
     * 
     * @throws MissingResourceException if {@link #getRequiredString(String)} throws the exception.
     * 
     * @see #getRequiredString(String)
     */
    public boolean getRequiredBoolean(String name) throws MissingResourceException {
   		String s = getRequiredString(name);
    	if (s.equalsIgnoreCase("yes") 
    			|| s.equalsIgnoreCase("y")
    			|| s.equalsIgnoreCase("true") 
    			|| s.equalsIgnoreCase("on")){
    		return true;
    	} else {
    		return false;
    	}
    }
    
    /**
     * Return true if the value is one of: yes, y, true, on.
     * The value is case insensitive.
     * @param name
     * @param defaultValue
     * @return the value of the property with the provided <code>name</code> as boolean or 
     * 		   <code>defaultValue</code> if the property does not exist or is empty string or has only white spaces.    
     */
    public boolean getBoolean(String name, boolean defaultValue) {
    	try {
    		return getRequiredBoolean(name);
    	} catch (MissingResourceException e) {
    		return defaultValue;
    	}
    }
    
    /**
     * Return true if the value is one of: yes, y, true, on.
     * The value is case insensitive.
     * @param name
     * @return the value of the property with the provided <code>name</code> as boolean or 
     * 		   false if the property does not exist or is empty string or has only white spaces.  
     */
    public boolean getBoolean(String name) {
    	return getBoolean(name, false);
    }
    
    /**
     * @return the value of the specified property or throws an exception if
     * the property is missing. This method uses the {@link #getRequiredString(String)} 
     * method to retrieve the value of the property as String first.
     * 
     * @throws MissingResourceException if {@link #getRequiredString(String)} throws the exception.
     * @throws IllegalStateException if the property value is not a valid int.
     * 
     * @see #getRequiredString(String)
     */
    public int getRequiredInt(String name) throws MissingResourceException, IllegalStateException {
    	String s = getRequiredString(name);
    	try {
    		return Integer.parseInt(s);
    	} catch (NumberFormatException e) {
   			throw new IllegalStateException(createExceptionString(name, s, int.class), e);
    	}
    }
    
    /**
     * 
     * @param name
     * @param defaultValue
     * @return the value of the property with the provided <code>name</code> as int or 
     * 		   <code>defaultValue</code> if the property does not exist 
     * 			or is empty string or has only white spaces or the value can not be
     * 			parsed as int.
     * 
     * @see Integer#parseInt(String)
     */
    public int getInt(String name, int defaultValue) {
    	try {
    		return getRequiredInt(name);
    	} catch (MissingResourceException e) {
    		return defaultValue;
    	} catch (IllegalStateException e) {
    		if (e.getCause() instanceof NumberFormatException) {
    			return defaultValue;
    		}  else {
    			throw e;
    		}
    	}
    }

    /**
     * @param name
     * @return the value of the property with the provided <code>name</code> as int or 
     * 		   	0 if the property does not exist or is empty string or has only white spaces
     * 			or the value can not be parsed as int.
     * 
     * @see Integer#parseInt(String)  
     */
    public int getInt(String name) {
    	return getInt(name, 0);
    }
    
    /**
     * @return the value of the specified property or throws an exception if
     * the property is missing. This method uses the {@link #getRequiredString(String)} 
     * method to retrieve the value of the property as String first.
     * 
     * @throws MissingResourceException if {@link #getRequiredString(String)} throws the exception.
     * @throws IllegalStateException if the property value is not a valid long.
     * 
     * @see #getRequiredString(String)
     */
    public long getRequiredLong(String name) throws MissingResourceException, IllegalStateException {
    	String s = getRequiredString(name);
    	try{
    		return Long.parseLong(s);
		} catch (NumberFormatException e) {
	    	throw new IllegalStateException(createExceptionString(name, s, long.class), e);
		}
    }
    
    /**
     * @param name
     * @param defaultValue
     * @return the value of the property with the provided <code>name</code> as long or 
     * 		   <code>defaultValue</code> if the property does not exist 
     * 			or is empty string or has only white spaces or
     * 			the value can not be parsed as long.
     *   
     * @see Long#parseLong(String)
     */
    public long getLong(String name, long defaultValue){
    	try {
    		return getRequiredLong(name);
    	} catch (MissingResourceException e) {
    		return defaultValue;
    	} catch (IllegalStateException e) {
    		if (e.getCause() instanceof NumberFormatException) {
    			return defaultValue;
    		}  else {
    			throw e;
    		}
    	}
    }

    /**
     * @param name
     * @return the value of the property with the provided <code>name</code> as long or 
     * 		   	0L if the property does not exist or is empty string or has only white spaces
     *  		or the value can not be parsed as long.
     *   
     * @see Long#parseLong(String)
     */
	public long getLong(String name) {
    	return getLong(name, 0L);
    }
    
	
    /**
     * @return the value of the specified property or throws an exception if
     * the property is missing. This method uses the {@link #getRequiredString(String)} 
     * method to retrieve the value of the property as String first.
     * 
     * @throws MissingResourceException if {@link #getRequiredString(String)} throws the exception.
     * @throws IllegalStateException if the property value is not a valid double.
     * 
     * @see #getRequiredString(String)
     */
    public double getRequiredDouble(String name) throws MissingResourceException, IllegalStateException {
    	String s = getRequiredString(name);
    	try {
    		return Double.parseDouble(s);
    	} catch (NumberFormatException e) {
    		throw new IllegalStateException(createExceptionString(name, s, double.class), e); 
    	}
    }
	
	
    /**
     * @param name
     * @param defaultValue
     * @return the value of the property with the provided <code>name</code> as double or 
     * 		   <code>defaultValue</code> if the property does not exist 
     * 			or is empty string or has only white spaces
     * 			or the value can not be parsed as double.  
     * 
     * @see Double#parseDouble(String)
     */
    public double getDouble(String name, double defaultValue) {
    	try {
    		return getRequiredDouble(name);
    	} catch (MissingResourceException e) {
    		return defaultValue;
    	} catch (IllegalStateException e) {
    		if (e.getCause() instanceof NumberFormatException) {
    			return defaultValue;
    		}  else {
    			throw e;
    		}
    	}
    }

    /**
     * @param name
     * @return the value of the property with the provided <code>name</code> as double or 
     * 		   	0.0d if the property does not exist or is empty string or has only white spaces
     * 			or the value can not be parsed as double.
     * 
     * @see Double#parseDouble(String)
     */
	public double getDouble(String name) {
    	return getDouble(name, 0.0d);
    }
	
    /**
     * @return the value of the specified property or throws an exception if
     * the property is missing. This method uses the {@link #getRequiredString(String)} 
     * method to retrieve the value of the property as String first.
     * 
     * @throws MissingResourceException if {@link #getRequiredString(String)} throws the exception.
     * @throws IllegalStateException if the property value is not a valid float.
     * 
     * @see #getRequiredString(String)
     */
    public float getRequiredFloat(String name) throws MissingResourceException, IllegalStateException {
    	String s = getRequiredString(name);
    	try {
    		return Float.parseFloat(s);
		} catch (NumberFormatException e) {
			throw new IllegalStateException(createExceptionString(name, s, float.class), e); 
		}
    }
	
    
    /**
     * @param name
     * @param defaultValue
     * @return the value of the property with the provided <code>name</code> as float or 
     * 		   <code>defaultValue</code> if the property does not exist or is empty string or has only white spaces
     *			or the value can not be parsed as float. 
     * 
     * @see Float#parseFloat(String) 
     */
    public float getFloat(String name, float defaultValue) {
    	try {
    		return getRequiredFloat(name);
    	} catch (MissingResourceException e) {
    		return defaultValue;
    	} catch (IllegalStateException e) {
    		if (e.getCause() instanceof NumberFormatException) {
    			return defaultValue;
    		}  else {
    			throw e;
    		}
    	}
    }

    /**
     * @param name
     * @return the value of the property with the provided <code>name</code> as float or 
     * 		   	0.0f if the property does not exist or is empty string or has only white spaces
     * 			or the value can not be parsed as float. 
     * 
     * @see Float#parseFloat(String)   
     */
	public float getFloat(String name) {
    	return getFloat(name, 0.0f);
    }
	
    /**
     * @return the value of the specified property or throws an exception if
     * the property is missing. This method uses the {@link #getRequiredString(String)} 
     * method to retrieve the value of the property as String first.
     * 
     * @throws MissingResourceException if {@link #getRequiredString(String)} throws the exception.
     * @throws IllegalStateException if the property value is not a valid date.
     * 
     * @see #getRequiredString(String)
     */
    public Date getRequiredDate(String name, DateFormat dateFormat) throws MissingResourceException, IllegalStateException {
    	Assert.assertNotNull(dateFormat, "dateFormat");
    	String s = getRequiredString(name);
		try {
			return dateFormat.parse(s);
		} catch (ParseException e) {
	    	throw new IllegalStateException(createExceptionString(name, s, Date.class), e);	    	
		}
    }
	

    /**
     * @param name
     * @param dateFormat - formatter used for date parsing.
     * @param defaultValue
     * @return the value of the property with the provided <code>name</code> as Date or 
     * 		   	<code>defaultValue</code> if the property does not exist or is empty string or has only white spaces
     * 			or the value can not be parsed as Date using the DateFormat parameter.  
     */
    public Date getDate(String name, DateFormat dateFormat, Date defaultValue) {
    	Assert.assertNotNull(dateFormat, "dateFormat");
    	try {
    		return getRequiredDate(name, dateFormat);
    	} catch (MissingResourceException e) {
    		return defaultValue;
    	} catch (IllegalStateException e) {
    		if (e.getCause() instanceof ParseException) {
    			return defaultValue;
    		} else {
    			throw e;
    		}
    	}
    }

    /**
     * @param name
     * @param dateFormat - formatter used for date parsing.
     * @return the value of the property with the provided <code>name</code> as Date or 
     * 		   	null if the property does not exist or is empty string or has only white spaces
     * 			or the value can not be parsed as Date using the DateFormat parameter.    
     */
	public Date getDate(String name, DateFormat dateFormat) {
    	return getDate(name, dateFormat, null);
    }
	
    /**
     * @return the value of the specified property or throws an exception if
     * the property is missing. This method used the {@link #getRequiredString(String)} 
     * method to retrieve the value of the property as String first.
     * 
     * @throws MissingResourceException if {@link #getRequiredString(String)} throws the exception.
     * @throws IllegalStateException if the property value is not a valid Date.
     * 
     * @see #getRequiredString(String)
     */
    public Date getRequiredDate(String name) throws MissingResourceException, IllegalStateException {
    	return getRequiredDate(name, new SimpleDateFormat("MM/dd/yyyy"));
    }
	
	
	/**
	 * @param name
	 * @param defaultValue
     * @return the value of the property with the provided <code>name</code> as Date or 
     * 		   	<code>defaultValue</code> if the property does not exist or is empty string or has only white spaces
     * 			or the value can not be parsed as Date using the MM/dd/yyyy format.  
	 */
	public Date getDate(String name, Date defaultValue) {
		return getDate(name, new SimpleDateFormat("MM/dd/yyyy"), defaultValue);
	}
	
	/**
	 * @param name
     * @return the value of the property with the provided <code>name</code> as Date or 
     * 		   	null if the property does not exist or is empty string or has only white spaces
     * 			or the value can not be parsed as Date using the MM/dd/yyyy format.    
	 */
	public Date getDate(String name) {
		return getDate(name, (Date)null);
	}
	
    /**
     * @return the value of the specified property or throws an exception if
     * the property is missing. This method uses the {@link #getRequiredString(String)} 
     * method to retrieve the value of the property as String first.
     * 
     * @throws MissingResourceException if {@link #getRequiredString(String)} throws the exception.
     * @throws IllegalStateException if the property value is not a valid BigDecimal.
     * 
     * @see #getRequiredString(String)
     */
    public BigDecimal getRequiredBigDecimal(String name) throws MissingResourceException, IllegalStateException {
    	String s = getRequiredString(name);
    	try {
    		return new BigDecimal(s);
		} catch (NumberFormatException e) {
			throw new IllegalStateException(createExceptionString(name, s, BigDecimal.class), e); 
		}
    }
	
	
    /**
     * @param name
     * @return the value of the property with the provided <code>name</code> as {@link BigDecimal} or 
     * 		   <code>defaultValue</code> if the property does not exist 
     * 			or is empty string or has only white spaces
     * 			or the value can not be parsed as BigDecimal.  
     */
	public BigDecimal getBigDecimal(String name, BigDecimal defaultValue) {
    	try {
    		return getRequiredBigDecimal(name);
    	} catch (MissingResourceException e) {
    		return defaultValue;
    	} catch (IllegalStateException e) {
    		if (e.getCause() instanceof NumberFormatException) {
    			return defaultValue;
    		} else {
    			throw e;
    		}
    	}
	}

	/**
	 * @param name
     * @return the value of the property with the provided <code>name</code> as double or 
     * 		   	{@link BigDecimal#ZERO} if the property does not exist or is empty string or has only white spaces
     * 			or the value can not be parsed as BigDecimal.
	 */
	public BigDecimal getBigDecimal(String name) {
		return getBigDecimal(name, BigDecimal.ZERO);
	}
	
	
    /**
     * @return the value of the specified property or throws an exception if
     * the property is missing. This method uses the {@link #getRequiredString(String)} 
     * method to retrieve the value of the property as String first.
     * 
     * @throws MissingResourceException if {@link #getRequiredString(String)} throws the exception.
     * @throws IllegalStateException if the property value is not a valid BigInteger.
     * 
     * @see #getRequiredString(String)
     */
    public BigInteger getRequiredBigInteger(String name) throws MissingResourceException, IllegalStateException {
    	return getRequiredBigDecimal(name).toBigInteger();
    }
	
	
    /**
     * @param name
     * @return the value of the property with the provided <code>name</code> as {@link BigInteger} or 
     * 		   <code>defaultValue</code> if the property does not exist 
     * 			or is empty string or has only white spaces
     * 			or the value can not be parsed as BigInteger.  
     */
	public BigInteger getBigInteger(String name, BigInteger defaultValue) {
    	try {
    		return getRequiredBigInteger(name);
    	} catch (MissingResourceException e) {
    		return defaultValue;
    	} catch (IllegalStateException e) {
    		if (e.getCause() instanceof NumberFormatException) {
    			return defaultValue;
    		} else {
    			throw e;
    		}
    	}
	}
	
	
	/**
	 * @param name
     * @return the value of the property with the provided <code>name</code> as double or 
     * 		   	{@link BigInteger#ZERO} if the property does not exist or is empty string or has only white spaces
     * 			or the value can not be parsed as BigInteger.
	 */
	public BigInteger getBigInteger(String name) {
		return getBigInteger(name, BigInteger.ZERO);
	}

	/**
	 * @param name
     * @return the value of the property with the provided <code>name</code> as {@link LocalDate} or 
     * 		   	null if the property does not exist or is empty string or has only white spaces
     * 			or the value can not be parsed as {@link LocalDate} using the MM/dd/yyyy format.
	 */
	public LocalDate getLocalDate(String name) {
		return getLocalDate(name, (LocalDate)null);
	}
	
	/**
	 * @param name
	 * @param dtf - {@link DateTimeFormatter}
     * @return the value of the property with the provided <code>name</code> as {@link LocalDate} or 
     * 		   	null if the property does not exist or is empty string or has only white spaces
     * 			or the value can not be parsed as {@link LocalDate} using the format given in the {@code dtf} param.
	 */
	public LocalDate getLocalDate(String name, DateTimeFormatter dtf) {
		return getLocalDate(name, dtf, null);
	}
	
	/**
	 * @param name
	 * @param defaultValue
     * @return the value of the property with the provided <code>name</code> as {@link LocalDate} or 
     * 		   	<code>defaultValue</code> if the property does not exist or is empty string or has only white spaces
     * 			or the value can not be parsed as {@link LocalDate} using the MM/dd/yyyy format.
	 */
	public LocalDate getLocalDate(String name, LocalDate defaultValue) {
		return getLocalDate(name, DateTimeFormatter.ofPattern("MM/dd/yyyy"), defaultValue);
	}
	
	/**
     * @param name
     * @param dateTimeFormatter - formatter used for date parsing.
     * @param defaultValue
     * @return the value of the property with the provided <code>name</code> as Date or 
     * 		   	<code>defaultValue</code> if the property does not exist or is empty string or has only white spaces
     * 			or the value can not be parsed as Date using the DateFormat parameter.
     */
    public LocalDate getLocalDate(String name, DateTimeFormatter dateTimeFormatter, LocalDate defaultValue) {
    	Assert.assertNotNull(dateTimeFormatter, "dateTimeFormatter");
    	try {
    		return getRequiredLocalDate(name, dateTimeFormatter);
    	} catch (MissingResourceException e) {
    		return defaultValue;
    	} catch (IllegalStateException e) {
    		if (e.getCause() instanceof DateTimeParseException) {
    			return defaultValue;
    		} else {
    			throw e;
    		}
    	}
    }
    
    /**
     * @return the value of the specified property or throws an exception if
     * the property is missing. This method used the {@link #getRequiredString(String)} 
     * method to retrieve the value of the property as String first.
     * 
     * @throws MissingResourceException if {@link #getRequiredString(String)} throws the exception.
     * @throws IllegalStateException if the property value is not a valid Date.
     * 
     * @see #getRequiredString(String)
     */
    public LocalDate getRequiredLocalDate(String name) throws MissingResourceException, IllegalStateException {
    	return getRequiredLocalDate(name, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }
    
    /**
     * @return the value of the specified property or throws an exception if
     * the property is missing. This method uses the {@link #getRequiredString(String)} 
     * method to retrieve the value of the property as String first.
     * 
     * @throws MissingResourceException if {@link #getRequiredString(String)} throws the exception.
     * @throws IllegalStateException if the property value is not a valid date.
     * 
     * @see #getRequiredString(String)
     */
    public LocalDate getRequiredLocalDate(String name, DateTimeFormatter dateTimeFormatter) throws MissingResourceException, IllegalStateException {
    	Assert.assertNotNull(dateTimeFormatter, "dateTimeFormatter");
    	String s = getRequiredString(name);
		try {
			return LocalDate.parse(s, dateTimeFormatter);
		} catch (DateTimeParseException e) {
	    	throw new IllegalStateException(createExceptionString(name, s, Date.class), e);	    	
		}
    }
	
	protected String createExceptionString(String name, String value, Class<?> clasz) {
		return "Property '" + name + "' with value '" + value + "' can not be parsed as " + clasz.getName() + " .";		
	}
	
}
