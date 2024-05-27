package com.asentinel.common.jdbc.resource.adapters;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.flavors.TableIntrospector;
import com.asentinel.common.util.Assert;

/**
 * {@code ResourceLoader} implementation that is able to turn a pair of table
 * columns into a resource bundle like representation. If the location matches
 * the format {@code db://table/key,value} a SQL query that pulls the
 * {@code key} and {@code value} from the table {@code table} is executed. If
 * the table or the {@code value} column does not exist a non existing resource
 * is returned. <br>
 * The intent is to use this class as the {@code ResourceLoader} for the Spring
 * {@code MessageSource} implementations. This class allows to transparently
 * pull messages from the database instead of using a true file resource. It
 * acts as a decorator for another {@code ResourceLoader} (the default is
 * Spring's {@code DefaultResourceLoader}) so that other types of resource URLs
 * are supported. <br>
 * See {@code ReloadableResourceBundleMessageSource} for additional information on
 * resource loaders for message resources.
 * 
 * @see #getResource(String)
 * @see ResourceLoader
 * 
 * @since 1.59
 * @author Razvan Popian
 */
public class TableResourceLoader implements ResourceLoader {
	private static final Logger log = LoggerFactory.getLogger(TableResourceLoader.class);
	
	private static final String DB_PREFIX = "db://";
	private static final String DB_PREFIX_REGEX = DB_PREFIX  + ".+\\/.+,.+";
	static final String SELECT = "select %s, %s from %s";
	
	private final SqlQuery queryEx;
	private final TableIntrospector tableIntrospector;
	private final ResourceLoader resourceLoader;

	/**
	 * Constructor that uses the Spring {@code DefaultResourceLoader} as the
	 * decorated {@code ResourceLoader}.
	 * 
	 * @see #TableResourceLoader(SqlQuery, TableIntrospector, ResourceLoader)
	 */
	public TableResourceLoader(SqlQuery queryEx, TableIntrospector tableIntrospector) {
		this(queryEx, tableIntrospector, null);
	}

	/**
	 * Constructor that allows the customization of the decorated
	 * {@code ResourceLoader}.
	 * 
	 * @param queryEx           {@code SqlQuery} database access object.
	 * @param tableIntrospector {@code TableIntrospector} implementation used for
	 *                          determining the columns that exist in the target
	 *                          table.
	 * @param resourceLoader    the {@code ResourceLoader} to use for URLs that do
	 *                          not point to the database. If {@code null} the
	 *                          Spring {@code DefaultResourceLoader} is used.
	 */
	public TableResourceLoader(SqlQuery queryEx, TableIntrospector tableIntrospector, ResourceLoader resourceLoader) {
		Assert.assertNotNull(queryEx, "queryEx");
		Assert.assertNotNull(tableIntrospector, "tableIntrospector");
		this.queryEx = queryEx;
		this.tableIntrospector = tableIntrospector; 
		this.resourceLoader = resourceLoader != null ? resourceLoader : new DefaultResourceLoader();
	}

	@Override
	public Resource getResource(String location) {
		Assert.assertNotNull(location, "location");
		if (matches(location)) {
			String[] locationElements = getLocationElements(location);
			if (!tableIntrospector.supports(locationElements[0], locationElements[2])) {
				if (log.isTraceEnabled()) {
					log.trace("getResource - Resource " + location + " does not exist. This is not necessarily a problem.");
				}
				return new TableResource(location, null);
			}
			
			Map<String, String> keyValues = loadMap(locationElements);
			return new TableResource(location, keyValues);
		}
		return resourceLoader.getResource(location);
	}
	
	protected Map<String, String> loadMap(String[] locationElements) {
		Map<String, String> keyValues = new HashMap<>();
		queryEx.query(String.format(SELECT, locationElements[1], locationElements[2], locationElements[0]), 
			rs -> keyValues.put(rs.getString(locationElements[1]), rs.getString(locationElements[2]))
		);
		return keyValues;
	}

	@Override
	public ClassLoader getClassLoader() {
		return resourceLoader.getClassLoader();
	}
	
	static boolean matches(String location) {
		if (location == null) return false;
		return location.toLowerCase().matches(DB_PREFIX_REGEX);
	}
	
	static String[] getLocationElements(String location) {
		String[] elements = new String[3];
		int i0 = DB_PREFIX.length();
		int i1 = location.indexOf('/', i0 + 1);
		elements[0] = location.substring(i0, i1).trim();
		int i2 = location.indexOf('.', i1 + 1);
		if (i2 < 0) {
			i2 = location.length();
		}
		String cols = location.substring(i1 + 1,  i2);
		String[] temp = cols.split(",");
		elements[1] = temp[0].trim();
		elements[2] = temp[1].trim();
		return elements;
	}

}
