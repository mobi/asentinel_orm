package com.asentinel.common.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.function.Function;

import org.junit.Test;

public class EitherTestCase {

	@Test
	public void left() {
		var value = 100; 
		
		Either<Object, Object> object = Either.left(value);
		
		Optional<Object> left = object.getLeft();
		assertTrue(left.isPresent());
		assertEquals(value, left.get());
		
		assertTrue(object.getRight().isEmpty());
	}
	
	@Test
	public void right() {
		var value = 100; 
		
		Either<Object, Object> object = Either.right(value);
		
		assertTrue(object.getLeft().isEmpty());
		
		Optional<Object> right = object.getRight();
		assertTrue(right.isPresent());
		assertEquals(value, right.get());
	}
	
	@Test
	public void getLeft() {				
		var value = 100; 		
		Either<Object, Object> object = Either.left(value);
		Optional<Object> left = object.getLeft();
		
		assertTrue(left.isPresent());
		assertEquals(value, left.get());
	}
	
	@Test
	public void getLeft_leftNull() {				
		Either<Object, Object> object = Either.left(null);
		assertTrue(object.getLeft().isEmpty());
	}
	
	@Test
	public void getRight() {				
		var value = 100; 		
		Either<Object, Object> object = Either.right(value);
		Optional<Object> right = object.getRight();
		
		assertTrue(right.isPresent());
		assertEquals(value, right.get());
	}
	
	@Test
	public void getRight_rightNull() {				
		Either<Object, Object> object = Either.right(null);
		assertTrue(object.getRight().isEmpty());
	}
	
	@Test
	public void isLeft() {				
		Either<Object, Object> object = Either.left(null);
		assertFalse(object.isLeft());
		
		var value = 100; 		
		object = Either.left(value);			
		assertTrue(object.isLeft());
	}
	
	@Test
	public void isRight() {				
		Either<Object, Object> object = Either.right(null);
		assertFalse(object.isRight());
		
		var value = 100; 		
		object = Either.right(value);			
		assertTrue(object.isRight());
	}
	
	@Test
	public void mapLeft() {				
		final Function<String, Integer> mapper = Integer::parseInt;
		
		Either<String, Object> either = Either.left(null);
		Optional<Integer> result = either.mapLeft(mapper);
		assertTrue(result.isEmpty());
		
		either = Either.left("123");
		result = either.mapLeft(mapper);
		assertTrue(result.isPresent());
		assertEquals(Integer.valueOf(123), result.get());
	}
	
	@Test
	public void mapRight() {				
		final Function<String, Integer> mapper = Integer::parseInt;
		
		Either<Object, String> either = Either.right(null);
		Optional<Integer> result = either.mapRight(mapper);
		assertTrue(result.isEmpty());
		
		either = Either.right("123");
		result = either.mapRight(mapper);
		assertTrue(result.isPresent());
		assertEquals(Integer.valueOf(123), result.get());
	}
	
	@Test
	public void toStringTest() {
		Either<String, String> either = Either.left("either");
		assertEquals("Left[either]", either.toString());		
		
		either = Either.right("either");
		assertEquals("Right[either]", either.toString());
	}
}
