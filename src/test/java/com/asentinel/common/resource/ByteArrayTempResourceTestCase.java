package com.asentinel.common.resource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;

import static org.junit.Assert.*;

public class ByteArrayTempResourceTestCase {
	
	private static final Logger log = LoggerFactory.getLogger(ByteArrayTempResourceTestCase.class);
	
	@Test
	public void testWriteRead() throws IOException {
		@SuppressWarnings("resource")
		TempResource r = new ByteArrayTempResource();
		byte[] bytes = {0, 1, 2, 3};
		OutputStream out = r.getOutputStream();
		FileCopyUtils.copy(bytes, out);
		
		byte[] readBytes = FileCopyUtils.copyToByteArray(r.getInputStream());
		log.debug("testWriteReadNoDeleteAfterRead - readBytes: {}", Arrays.toString(readBytes));
        assertArrayEquals(bytes, readBytes);

		assertFalse(r.isOpen());
		assertTrue(r.isReadable());
		assertTrue(r.isWritable());
	}

	
	@Test
	public void testAutoClose() {
		boolean[] closed = new boolean[]{false};
		try (TempResource r = new ByteArrayTempResource(){
			@Override
			public void cleanup() {
				super.cleanup();
				closed[0] = true;
			}
		}) {
			
		}
		assertTrue(closed[0]);
	}

}
