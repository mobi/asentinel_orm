package com.asentinel.common.jdbc.exceptions.resolve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.aspectj.lang.JoinPoint;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

/**
 * @since 1.60.12
 * @author Razvan Popian
 */
public class ResolvableExceptionAspectTestCase {
	private final IllegalArgumentException e = new IllegalArgumentException();
	private final TestEntity entity = new TestEntity();
	private final MessageCodeWrapper mcw = new MessageCodeWrapper("test");
	
	private final ArgumentMatcher<Optional<Class<?>>> amc = argument -> argument.get() == entity.getClass();		
	private final ArgumentMatcher<Optional<?>> ame = argument -> argument.get() == entity;
	
	private final ExceptionResolver resolver = mock(ExceptionResolver.class);
	private final JoinPoint jp = mock(JoinPoint.class);

	private final ResolvableExceptionAspect aspect = new ResolvableExceptionAspect(List.of(resolver));
	
	@Test
	public void resolved_BothExAndEntityPresent() {
		
		when(resolver.supports(eq(e), argThat(amc))).thenReturn(true);
		when(resolver.resolve(eq(e), argThat(ame))).thenReturn(mcw);
		
		when(jp.getArgs()).thenReturn(new Object[] {null, entity, null});
		
		ResolvedException ex = assertThrows(ResolvedException.class, () -> aspect.afterThrowingAdvice(jp, e));
		assertEquals(mcw, ex.getMessageCodeWrapper());
		assertEquals(e, ex.getCause());
		assertEquals(mcw.getCode(), ex.getMessage());
	}

	@Test
	public void resolved_OnlyExPresent() {
		
		when(resolver.supports(e, Optional.empty())).thenReturn(true);
		when(resolver.resolve(e, Optional.empty())).thenReturn(mcw);
		
		when(jp.getArgs()).thenReturn(new Object[] {null, 5, null});
		
		ResolvedException ex = assertThrows(ResolvedException.class, () -> aspect.afterThrowingAdvice(jp, e));
		assertEquals(mcw, ex.getMessageCodeWrapper());
		assertEquals(e, ex.getCause());
		assertEquals(mcw.getCode(), ex.getMessage());
	}
	
	@Test
	public void notResolved() {
		
		when(resolver.supports(e, Optional.empty())).thenReturn(false);
		
		when(jp.getArgs()).thenReturn(new Object[] {5});
		
		try {
			aspect.afterThrowingAdvice(jp, e);
		} catch (Exception ex) {
			// this is an advice, in the real world the original exception
			// would be thrown
			fail("No exception expected.");
		}

	}

	
	@Table("table")
	private static final class TestEntity {
		@PkColumn("id")
		private int id;
	}
}
