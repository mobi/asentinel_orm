package com.asentinel.common.resource;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

public class BasicAuthHttpUrlResourceTestCase {
	private static final Logger log = LoggerFactory.getLogger(BasicAuthHttpUrlResourceTestCase.class);
	
	HttpURLConnection con = mock(HttpURLConnection.class);


	@Test
	public void testAddAuthHeader() throws IOException {
		BasicAuthHttpUrlResource r = new BasicAuthHttpUrlResource("http://test.com", "razvan", "test");
		
		r.customizeConnection(con);
		verify(con).setRequestProperty ("Authorization", "Basic " + new String(Base64.getEncoder().encode(("razvan:test").getBytes())));
	}

	@Test
	public void testGetInputStreamOk() throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(new byte[1]);
		when(con.getInputStream()).thenReturn(in);
		BasicAuthHttpUrlResource r = new BasicAuthHttpUrlResource("http://test.com", "razvan", "test") {
			@Override
			URLConnection openConnectionInternal() {
				return con;
			}
		};
		log.debug("res: {}", r);
		assertSame(in, r.getInputStream());
		verify(con).setRequestProperty ("Authorization", "Basic " + new String(Base64.getEncoder().encode(("razvan:test").getBytes())));
	}

	@Test(expected = IOException.class)
	public void testGetInputStreamException() throws IOException {
		when(con.getInputStream()).thenThrow(new IOException());
		BasicAuthHttpUrlResource r = new BasicAuthHttpUrlResource("http://test.com", "razvan", "test") {
			@Override
			URLConnection openConnectionInternal() {
				return con;
			}
		};
		try {
			r.getInputStream();
		} finally {
			verify(con).disconnect();
		}
	}
}
