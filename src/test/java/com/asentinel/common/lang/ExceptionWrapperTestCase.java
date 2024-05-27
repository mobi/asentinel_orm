package com.asentinel.common.lang;

import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExceptionWrapperTestCase {
		
	@Test(expected = UnsupportedOperationException.class)
	public void newInstance() {
		new ExceptionWrapper();
	}
	
	@Test
	public void apply_NoExceptionEncountered() {
		String result = List.of(new OddNumber(1),
						   new OddNumber(3)).stream()
								.map(ExceptionWrapper.apply(OddNumber::stringValue))
								.collect(joining("+"));
		
		assertEquals("1+3", result);
	}
	
	@Test(expected = RuntimeException.class)
	public void apply_ExceptionEncountered() {
		List.of(new OddNumber(1),
		   new OddNumber(2)).stream()
						.map(ExceptionWrapper.apply(OddNumber::stringValue))
						.collect(joining("+"));
	}
	
	@Test
	public void applyEither_NoExceptionEncountered() {
		boolean result = List.of(new OddNumber(1),
							new OddNumber(3)).stream()
								.map(ExceptionWrapper.applyEither(OddNumber::stringValue))
								.allMatch(Either::isRight);
		assertTrue(result);
	}
	
	@Test
	public void applyEither() {
		final List<Number> input = List.of(new OddNumber(1),
				   					  new OddNumber(2),
				   					  new EvenNumber(3),
				   					  new EvenNumber(4));
		
		final List<Either<? extends Exception, String>> all = input.stream()
				.map(ExceptionWrapper.applyEither(Number::stringValue))
				.collect(toList());
		assertEquals(input.size(), all.size());

		//inspect exceptions
		final List<? extends Exception> exceptions = all.stream()
				.filter(Either::isLeft)
				.map(Either::getLeft)
				.map(Optional::get)
				.collect(toList());
		
		assertEquals(2, exceptions.size());
		
		assertTrue(exceptions.get(0) instanceof NotOddException);
		assertEquals("Not an odd number.", exceptions.get(0).getMessage());
		
		assertTrue(exceptions.get(1) instanceof NotEvenException);
		assertEquals("Not an even number.", exceptions.get(1).getMessage());
		
		//inspect results
		final List<String> results = all.stream()
				.filter(Either::isRight)
				.map(Either::getRight)
				.map(Optional::get)
				.collect(toList());
		
		assertEquals(2, results.size());			
		assertEquals("1", results.get(0));
		assertEquals("4", results.get(1));
	}
	
	@Test
	public void test_NoExceptionEncountered() {
		final int value = 1;
		
		int result = List.of(new OddNumber(value)).stream()
								.filter(ExceptionWrapper.test(OddNumber::isValid))
								.map(OddNumber::getValue)
								.findFirst()
								.get();								
		assertEquals(value, result);
	}
	
	@Test(expected = RuntimeException.class)
	public void test_ExceptionEncountered() {
		List.of(new OddNumber(2)).stream()
					.filter(ExceptionWrapper.test(OddNumber::isValid))
					.map(OddNumber::getValue)
					.findFirst()
					.get();								
	}
	
	private interface Number {
		int getValue();
		String stringValue() throws Exception;
		boolean isValid() throws Exception;		
	}
	
	private static class OddNumber implements Number {
		
		private final int value;
		
		public OddNumber(int value) {
			this.value = value;
		}
		
		@Override
		public String stringValue() throws NotOddException {
			isValid();
			return String.valueOf(value);			
		}

		@Override
		public boolean isValid() throws NotOddException {
			if (value % 2 == 1) {
				return true;
			}
			throw new NotOddException("Not an odd number.");
		}

		@Override
		public int getValue() {
			return value;
		}
	}
	
	private static class NotOddException extends Exception {
		
		private static final long serialVersionUID = 1L;

		public NotOddException(String message) {
			super(message);
		}
	}
	
	private static class EvenNumber implements Number {
		
		private final int value;
		
		public EvenNumber(int value) {
			this.value = value;
		}
		
		@Override
		public String stringValue() throws NotEvenException {
			isValid();
			return String.valueOf(value);			
		}
		
		@Override
		public boolean isValid() throws NotEvenException {
			if (value % 2 == 0) {
				return true;				
			}
			throw new NotEvenException("Not an even number.");
		}
		
		@Override
		public int getValue() {
			return value;
		}
	}	
	
	private static class NotEvenException extends Exception {
		
		private static final long serialVersionUID = 1L;

		public NotEvenException(String message) {
			super(message);
		}
	}
}
