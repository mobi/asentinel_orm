package com.asentinel.common.jdbc.resource.adapters;

import static org.springframework.util.StringUtils.hasText;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.io.AbstractResource;

import com.asentinel.common.util.Assert;

/**
 * {@code Resource} that turns a key-value map into a resource bundle like
 * representation. This is used by the {@code TableResourceLoader}.
 * 
 * @see TableResourceLoader
 * 
 * @since 1.59
 * @author Razvan Popian
 */
class TableResource extends AbstractResource {
	
	private final String description;
	private final Map<String, String> keyValues;
	
	/**
	 * @param description the description for this resource.
	 * @param keyValues the map to use, if {@code null} this is considered a non existing resource.
	 * 
	 * @see #exists()
	 */
	public TableResource(String description, Map<String, String> keyValues) {
		Assert.assertNotNull(description, "description");
		if (!hasText(description)) {
			description = "Unspecified";
		}
		this.description = description;
		this.keyValues = keyValues;
	}

	@Override
	public String getDescription() {
		return getFilename();
	}
	
	@Override
	public String getFilename() {
		return description;
	}

	@Override
	public boolean exists() {
		return keyValues != null; 
	}
	

	@Override
	public InputStream getInputStream() throws IOException {
		try(
			ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(streamOut, StandardCharsets.UTF_8);
			) {
			for (Entry<String, String> entry: keyValues.entrySet()) {
				if (hasText(entry.getKey()) && hasText(entry.getValue())) {
					writer.write(entry.getKey());
					writer.write("=");
					writer.write(entry.getValue());
					writer.write('\n');
				}
			}
			writer.flush();
			return new ByteArrayInputStream(streamOut.toByteArray());
		}
	}
	
	Map<String, String> getKeyValues() {
		if (keyValues == null) {
			return null;
		}
		return Collections.unmodifiableMap(keyValues);
	}

	// hashcode and equals not really used, they are here just to keep Sonar happy
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}
