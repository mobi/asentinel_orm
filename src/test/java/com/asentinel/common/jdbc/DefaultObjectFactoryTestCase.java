package com.asentinel.common.jdbc;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DefaultObjectFactoryTestCase {
	
	@Test
	public void noExplicitNoArgsConstructor() {
		DefaultObjectFactory<NoExplicitNoArgsConstructor> f = new DefaultObjectFactory<>(NoExplicitNoArgsConstructor.class);
		assertTrue(f.newObject() instanceof NoExplicitNoArgsConstructor);
	}
	
	@Test
	public void noArgsPrivateConstructor() {
		DefaultObjectFactory<NoArgsPrivateConstructor> f = new DefaultObjectFactory<>(NoArgsPrivateConstructor.class);
		assertTrue(f.newObject() instanceof NoArgsPrivateConstructor);
	}

	@Test
	public void noArgsProtectedConstructor() {
		DefaultObjectFactory<NoArgsProtectedConstructor> f = new DefaultObjectFactory<>(NoArgsProtectedConstructor.class);
		assertTrue(f.newObject() instanceof NoArgsProtectedConstructor);
	}
	
	@Test
	public void noArgsPublicConstructor() {
		DefaultObjectFactory<NoArgsPublicConstructor> f = new DefaultObjectFactory<>(NoArgsPublicConstructor.class);
		assertTrue(f.newObject() instanceof NoArgsPublicConstructor);
	}

	
	@Test(expected = IllegalStateException.class)
	public void nonStaticNoExplicitNoArgsConstructor() {
		DefaultObjectFactory<NonStaticNoExplicitNoArgsConstructor> f = new DefaultObjectFactory<>(NonStaticNoExplicitNoArgsConstructor.class);
		assertTrue(f.newObject() instanceof NonStaticNoExplicitNoArgsConstructor);
	}

	
	@Test(expected = IllegalStateException.class)
	public void noNoArgsConstructor() {
		new DefaultObjectFactory<>(NoNoArgsConstructor.class);
	}
	
	@Test(expected = IllegalStateException.class)
	public void exceptionInNoArgsPrivateConstructor() {
		DefaultObjectFactory<ExceptionInNoArgsPrivateConstructor> f = new DefaultObjectFactory<>(ExceptionInNoArgsPrivateConstructor.class);
		f.newObject();
	}
	
	@Test(expected = IllegalStateException.class)
	public void abstractNoArgsPublicConstructor() {
		DefaultObjectFactory<AbstractNoArgsPublicContructor> f = new DefaultObjectFactory<>(AbstractNoArgsPublicContructor.class);
		f.newObject();
	}

	private static class NoExplicitNoArgsConstructor {
		
	}
	
	private static class NoArgsPrivateConstructor {
		
		private NoArgsPrivateConstructor() {
			
		}
	}

	private static class NoArgsProtectedConstructor {
		
		@SuppressWarnings("unused")
		protected NoArgsProtectedConstructor() {
			
		}
	}
	
	
	private static class NoArgsPublicConstructor {
		
		@SuppressWarnings("unused")
		public NoArgsPublicConstructor() {
			
		}
	}
	
	private static class ExceptionInNoArgsPrivateConstructor {
		
		private ExceptionInNoArgsPrivateConstructor() {
			throw new RuntimeException("FAIL");
		}
	}
	
	private abstract static class AbstractNoArgsPublicContructor {
		
	}
	
	private class NonStaticNoExplicitNoArgsConstructor {
		
	}
	
	private static class NoNoArgsConstructor {
		
		@SuppressWarnings("unused")
		public NoNoArgsConstructor(Object o) {
			
		}
	}
}
