package com.asentinel.common.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.core.io.AbstractResource;

/**
 * In memory temporary resource. Instances of this class can be shared among multiple threads
 * with the condition that it is used as an effectively immutable object. A thread creates the 
 * resource (writes the resource bytes) and properly publishes it for the other threads that 
 * use the resource as a read-only resource.
 * 
 * @author Razvan Popian
 */
public class ByteArrayTempResource extends AbstractResource implements TempResource {
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	
	@Override
	public boolean isOpen() {
		// return false because we need this to work with MimeMessageHelper
		return false;
	}

	@Override
	public boolean isReadable() {
		return true;
	}
	
	@Override
	public boolean isWritable() {
		return true;
	}
	
	
	@Override
	public boolean exists() {
		return true;
	}
	
	@Override
	public long contentLength() throws IOException {
		return out.size();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(out.toByteArray());
	}
	
	@Override
	public OutputStream getOutputStream() throws IOException {
		return out;
	}

	@Override
	public String getDescription() {
		return "ByteArrayTempResource";
	}

	@Override
	public void cleanup() {
		// do nothing
	}
	
}
