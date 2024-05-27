package com.asentinel.common.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class CustomResourceLoaderTestCase {
	
	ResourceLoader innerRl =  mock(ResourceLoader.class);
	CustomResourceLoader rl = new CustomResourceLoader(innerRl);

	@Test
	public void testHttpWithAuth1() {
		Resource r = rl.getResource("http://razvan:test@testurl.com");
		assertTrue(r instanceof BasicAuthHttpUrlResource);
		verifyNoMoreInteractions(innerRl);
	}

	@Test
	public void testHttpWithAuth2() {
		Resource r = rl.getResource("HTTP://razvan:test@testurl.com");
		assertTrue(r instanceof BasicAuthHttpUrlResource);
		verifyNoMoreInteractions(innerRl);
	}
	
	@Test
	public void testHttpWithAuth3() {
		Resource r = rl.getResource("HTTP://razvan:test@testurl.com:8080");
		assertTrue(r instanceof BasicAuthHttpUrlResource);
		verifyNoMoreInteractions(innerRl);
	}
	
	@Test
	public void testHttpDoubleColon() {
		String loc = "http://razvan:aaa:bbb@testurl.com";
		Resource r = rl.getResource(loc);
		assertTrue(r instanceof BasicAuthHttpUrlResource);
		assertEquals("razvan", ((BasicAuthHttpUrlResource) r).getUser());
		assertEquals("aaa:bbb", ((BasicAuthHttpUrlResource) r).getPassword());
		verifyNoMoreInteractions(innerRl);
	}	
	
	@Test
	public void testHttpsWithAuth1() {
		Resource r = rl.getResource("https://razvan:test@testurl.com");
		assertTrue(r instanceof BasicAuthHttpUrlResource);
		verifyNoMoreInteractions(innerRl);
	}

	@Test
	public void testHttpsWithAuth2() {
		Resource r = rl.getResource("HTTPS://razvan:test@testurl.com");
		assertTrue(r instanceof BasicAuthHttpUrlResource);
		verifyNoMoreInteractions(innerRl);
	}

	@Test
	public void testHttpsWithAuth3() {
		Resource r = rl.getResource("HTTPS://razvan:test@testurl.com:8080");
		assertTrue(r instanceof BasicAuthHttpUrlResource);
		verifyNoMoreInteractions(innerRl);
	}
	
	@Test
	public void testHttpsDoubleColon() {
		String loc = "https://razvan:aaa:bbb@testurl.com";
		Resource r = rl.getResource(loc);
		assertTrue(r instanceof BasicAuthHttpUrlResource);
		assertEquals("razvan", ((BasicAuthHttpUrlResource) r).getUser());
		assertEquals("aaa:bbb", ((BasicAuthHttpUrlResource) r).getPassword());
		verifyNoMoreInteractions(innerRl);
	}	
	
	@Test
	public void testHttpWithWrongFormat1() {
		String loc = "HTTPS://razvantest@testurl.com";
		rl.getResource(loc);
		verify(innerRl).getResource(loc);
	}

	@Test
	public void testHttpWithWrongFormat2() {
		String loc = "HTTPS://razvan:testtesturl.com";
		rl.getResource(loc);
		verify(innerRl).getResource(loc);
	}

	@Test
	public void testFallback() {
		String loc = "HTTPS://testurl.com";
		rl.getResource(loc);
		verify(innerRl).getResource(loc);
	}

	@Test
	public void testEmptyUserAndPass() {
		String loc = "HTTPS://:@testurl.com";
		rl.getResource(loc);
		verify(innerRl).getResource(loc);
	}

	@Test
	public void testEmptyPass() {
		String loc = "HTTPS://razvan:@testurl.com";
		rl.getResource(loc);
		verify(innerRl).getResource(loc);
	}

	@Test
	public void testEmptyUser() {
		String loc = "HTTPS://:aaa@testurl.com";
		rl.getResource(loc);
		verify(innerRl).getResource(loc);
	}
	
}
