package com.asentinel.common.jdbc.flavors;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;

/**
 * Configuration class that holds the {@link JdbcFlavor} implementation used by the application. The {@link JdbcFlavor}
 * is initialized based on the property <code>jdbc.flavor</code> found in the classpath file  
 * <code>com/asentinel/common/jdbc/flavors/jdbc.properties</code>.
 * 
 * @see #getJdbcFlavor()
 * 
 * @deprecated in favor of injecting the {@code JdbcFlavor} instance whereever it is needed.
 */
@Deprecated
public final class JdbcFlavorConfig {
	private final static Logger log = LoggerFactory.getLogger(JdbcFlavorConfig.class);
	
	private final static String PROPERTY_JDBC_FLAVOR = "jdbc.flavor";
	private final static String CLASSPATH_PATH_TO_PROPS = "com/asentinel/common/jdbc/flavors/jdbc.properties";
	
	private final static JdbcFlavor instance;
	static {
		JdbcFlavor temp;
		Resource resource = new ClassPathResource(CLASSPATH_PATH_TO_PROPS);
		Properties props = new Properties();
		try (InputStream in = resource.getInputStream()) {
			props.load(in);
			String flavorClassName = props.getProperty(PROPERTY_JDBC_FLAVOR);
			try {
				Class<?> flavorClass = Class.forName(flavorClassName);
				if (!JdbcFlavor.class.isAssignableFrom(flavorClass)) {
					throw new RuntimeException("Class " + flavorClass + " is not a JdbcFlavor.");
				}
				temp = (JdbcFlavor) flavorClass.newInstance();
				log.info("init - The selected JdbcFlavor implementation is " + temp.getClass().getName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("Can not find/instantiate class " + flavorClassName, e);
			}
		} catch (IOException e) {
			log.info("init - Properties file " + CLASSPATH_PATH_TO_PROPS + " not found. Defaulting to Postgres jdbc flavor.");
			temp = new PostgresJdbcFlavor();
		}
		instance = temp;
	}
	
	private JdbcFlavorConfig() {
		
	}
	
	/**
	 * @return the configured {@link JdbcFlavor}.
	 */
	@Deprecated
	public static JdbcFlavor getJdbcFlavor() {
		return instance;
	}

}
