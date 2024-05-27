package com.asentinel.common.resource;

import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.asentinel.common.util.Assert;

/**
 * {@code ResourceLoader} implementation that is capable of interpreting HTTP URLs containing username
 * and password. It returns a {@link BasicAuthHttpUrlResource} for this types of URLs. The format of the
 * URL must be the following:
 * <li> http://user:password@test.com
 * <li> https://user:password@test.com
 * <br><br>
 * 
 * @see BasicAuthHttpUrlResource
 * 
 * @author Razvan Popian
 */
public class CustomResourceLoader implements ResourceLoader {
	private static final Logger log = LoggerFactory.getLogger(CustomResourceLoader.class);
	
	private static final String HTTP_PREFIX = "http://";
	private static final String HTTPS_PREFIX = "https://";
	
	private static final String SEP = ":";
	
	private static final String HTTP_BASIC_AUTH_PREFIX_REGEX 	= HTTP_PREFIX  + ".+" + SEP + ".+@.+";
	private static final String HTTPS_BASIC_AUTH_PREFIX_REGEX 	= HTTPS_PREFIX + ".+" + SEP + ".+@.+";
	
	private final ResourceLoader resourceLoader;

	/**
	 * No-args constructor that sets a {@link DefaultResourceLoader} as
	 * the fallback {@code ResourceLoader}.
	 */
	public CustomResourceLoader() {
		this(new DefaultResourceLoader());
	}
	
	/**
	 * Creates a new {@code CustomResourceLoader} that will fallback to the specified
	 * {@code ResourceLoader} for URL formats that it can not interpret. See the class
	 * documentation.
	 * @param resourceLoader the fallback {@code ResourceLoader}
	 */
	public CustomResourceLoader(ResourceLoader resourceLoader) {
		Assert.assertNotNull(resourceLoader, "resourceLoader");
		this.resourceLoader = resourceLoader;
	}
	
	private Resource getResourceInternal(String location, String user, String password) {
		try {
			return new BasicAuthHttpUrlResource(location, user, password);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String[] getUserAndPassword(String location) {
		location = location.replace(HTTPS_PREFIX, "").replace(HTTP_PREFIX, "");
		int indexOfAt = location.indexOf("@");
		if (indexOfAt < 0) {
			throw new IllegalArgumentException("Can not extract the user and password from the provided location: " + location);
		}
		location = location.substring(0, indexOfAt);
		int colonIndex = location.indexOf(SEP);
		if (colonIndex < 0) {
			throw new IllegalArgumentException("Can not extract the user and password from the provided location: " + location);
		}
		String left = location.substring(0, colonIndex);
		String right = location.substring(colonIndex + 1);
		String[] ret = new String[]{left, right};
		return ret;
	}

	/**
	 * See the class documentation.
	 */
	@Override
	public Resource getResource(String location) {
		if (location.toLowerCase().matches(HTTP_BASIC_AUTH_PREFIX_REGEX)
				|| location.toLowerCase().matches(HTTPS_BASIC_AUTH_PREFIX_REGEX)) {
			String finalLocation = location.replaceAll(HTTP_PREFIX + ".+:.+@", HTTP_PREFIX)
					.replaceAll(HTTPS_PREFIX + ".+:.+@", HTTPS_PREFIX);
			String[] userAndPass = getUserAndPassword(location);
			if (log.isTraceEnabled()) {
				log.trace("getResource - finalLocation: " + finalLocation);
				log.trace("getResource - user: " + userAndPass[0]);
			}
			Resource resource = getResourceInternal(finalLocation, userAndPass[0], userAndPass[1]);
			if (log.isDebugEnabled()) {
				log.debug("getResource - resource: " + resource);
			}
			return resource;
		}
		return resourceLoader.getResource(location);
	}

	/**
	 * Delegates to the fallback {@code ResourceLoader}.
	 */
	@Override
	public ClassLoader getClassLoader() {
		return resourceLoader.getClassLoader();
	}
	
	
}
