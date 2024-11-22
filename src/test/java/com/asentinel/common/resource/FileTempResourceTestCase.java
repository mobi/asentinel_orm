package com.asentinel.common.resource;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;

import static org.junit.Assert.*;

public class FileTempResourceTestCase {
	private static final Logger log = LoggerFactory.getLogger(FileTempResourceTestCase.class);
	
	@Test
	public void testWriteReadNoDeleteAfterRead() throws IOException {
		@SuppressWarnings("resource")
		FileTempResource r = new FileTempResource();
		assertFalse(r.isCleaned());
		byte[] bytes = {0, 1, 2, 3};
		OutputStream out = r.getOutputStream();
		FileCopyUtils.copy(bytes, out);
		
		byte[] readBytes = FileCopyUtils.copyToByteArray(r.getInputStream());
		log.debug("testWriteReadNoDeleteAfterRead - readBytes: {}", Arrays.toString(readBytes));
        assertArrayEquals(bytes, readBytes);
		
		assertFalse(r.isCleaned());
		assertFalse(r.isOpen());
		assertTrue(r.isReadable());
		assertTrue(r.isWritable());
		
		r.cleanup();
		
		assertTrue(r.isCleaned());
		assertFalse(r.isOpen());
		assertFalse(r.isReadable());
		assertFalse(r.isWritable());
		
		// attempt to open more streams
		try {
			r.getInputStream();
			fail("Should not be able to open another input stream.");
		} catch (IOException e) {
			log.debug("testWriteReadNoDeleteAfterRead - Expected exception: {}", e.getMessage());
		}
		
		try {
			r.getOutputStream();
			fail("Should not be able to open another output stream.");
		} catch (IOException e) {
			log.debug("testWriteReadNoDeleteAfterRead - Expected exception: {}", e.getMessage());
		}
		
	}
	

	@Test
	public void testWriteReadWithoutOutputStreamClose() throws IOException {
		@SuppressWarnings("resource")
		FileTempResource r = new FileTempResource();
		assertFalse(r.isCleaned());
		byte[] bytes = {0, 1, 2, 3};
		OutputStream out = r.getOutputStream();
		out.write(bytes);
		
		byte[] readBytes = FileCopyUtils.copyToByteArray(r.getInputStream());
		log.debug("testWriteReadNoDeleteAfterRead - readBytes: {}", Arrays.toString(readBytes));
		assertTrue(Arrays.equals(bytes, readBytes));
		
		r.getOutputStream();
		r.getOutputStream();
		
		r.getInputStream();
		r.getInputStream();
		
		assertFalse(r.isCleaned());
		assertFalse(r.isOpen());
		assertTrue(r.isReadable());
		assertTrue(r.isWritable());
		
		r.cleanup();

		assertTrue(r.isCleaned());
		assertFalse(r.isOpen());
		assertFalse(r.isReadable());
		assertFalse(r.isWritable());
	}

	
	@Test
	public void testWithFileName() throws IOException {
		File f = File.createTempFile("asentinel", ".tmp");
		f.deleteOnExit();
		@SuppressWarnings("resource")
		FileTempResource r = new FileTempResource(f.getAbsolutePath());
		assertFalse(r.isCleaned());
		r.cleanup();
		assertTrue(r.isCleaned());
	}

	@SuppressWarnings("resource")
	@Test
	public void testWithFileThatDoesNotExist() {
		try {
			new FileTempResource("dummy");
			fail("Should not be able to create temp resource for a file that does not exist.");
		} catch (IllegalArgumentException e) {
			
		}
	}
	
	@SuppressWarnings("resource")
	@Test
	public void testAutoClose() {
		FileTempResource res;
		try (FileTempResource r = new FileTempResource()) {
			res = r;
		}
		assertTrue(res.isCleaned());
	}
	
	/**
	 * Relies on the assumption that {@link System#gc()} actually runs the garbage
	 * collector. If we find that this is not the case on certain JVMs we should
	 * mark the test with {@code Ignore}.
	 */
	@Test
	public void garbageCollectorCleanup() throws InterruptedException {
		@SuppressWarnings("resource")
		FileTempResource r = new FileTempResource();
		File f = r.getFile();
		assertTrue(f.exists());
		r = null; // so that the instance can be garbage collected
		
		System.gc();
		long t0 = System.currentTimeMillis();
		long t1 = t0;
		while (f.exists() && (t1 - t0) < 1000) {
			Thread.sleep(10);
			t1 = System.currentTimeMillis();
		}
		assertFalse(f.exists());
	}
	
}
