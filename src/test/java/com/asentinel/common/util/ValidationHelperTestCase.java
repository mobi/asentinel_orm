package com.asentinel.common.util;

import static org.junit.Assert.*;
import org.junit.Test;

public class ValidationHelperTestCase {
	
	@Test
	public void testIsEmailValid1() {
		String email = "Razvan.Popian@Asentinel.COM";
		assertTrue(ValidationHelper.isEmailValid(email));
	}

	@Test
	public void testIsEmailValid2() {
		String email = "Razvan.Popian@Asentinel.C@M";
		assertFalse(ValidationHelper.isEmailValid(email));
	}

	@Test
	public void testIsEmailValid3() {
		String email = "abc";
		assertFalse(ValidationHelper.isEmailValid(email));
	}
	
	@Test
	public void testIsEmailValid4() {
		String email = "111@111.CoM";
		assertTrue(ValidationHelper.isEmailValid(email));
	}

	@Test
	public void testIsEmailValid5() {
		String email = "";
		assertFalse(ValidationHelper.isEmailValid(email));
	}

	@Test
	public void testIsEmailValid6() {
		String email = null;
		assertFalse(ValidationHelper.isEmailValid(email));
	}
}
