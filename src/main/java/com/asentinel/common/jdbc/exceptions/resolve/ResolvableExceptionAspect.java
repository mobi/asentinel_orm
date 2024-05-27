package com.asentinel.common.jdbc.exceptions.resolve;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import com.asentinel.common.orm.EntityUtils;

/**
 * Make sure you specify the order for this advice in contexts where it may run
 * together with other advices like the transaction advice.
 * 
 * @see ThrowsResolvableException
 * 
 * @since 1.60.12
 * @author Razvan Popian
 */
@Aspect
public class ResolvableExceptionAspect implements Ordered {
	private static final Logger log = LoggerFactory.getLogger(ResolvableExceptionAspect.class);
	
	private List<ExceptionResolver> exceptionResolvers = emptyList();
	private int order = 0; // no assumption on order
	
	public ResolvableExceptionAspect() {
		
	}
	
	public ResolvableExceptionAspect(List<ExceptionResolver> exceptionResolvers) {
		setExceptionResolvers(exceptionResolvers);
	}

	/**
	 * @see ThrowsResolvableException
	 * @param jp the join point to which this advice applies.
	 * @param ex the exception that was thrown
	 */
	@AfterThrowing(
			pointcut = "@annotation(com.asentinel.common.jdbc.exceptions.resolve.ThrowsResolvableException)", 
			throwing = "ex")
	public void afterThrowingAdvice(JoinPoint jp, Exception ex) {
		log.debug("afterThrowingAdvice - {} triggered by exception type {} with message '{}'", 
				this.getClass().getSimpleName(), ex.getClass().getSimpleName(), ex.getMessage());
		Object[] args = jp.getArgs();
		
		// TODO: we may want in the future to annotate our target entity parameter somehow instead
		// of searching for it and using the first entity parameter, that would allow non ORM entities
		// to be used as target objects
		Optional<?> targetEntity = Stream.of(args)
					.filter(Objects::nonNull)
					.filter(arg -> EntityUtils.isEntityClass(arg.getClass()))
					.findFirst();
		if (targetEntity.isEmpty()) {
			log.debug("afterThrowingAdvice - No target entity found  in the parameters list of the method {}. "
					+ "None will be passed to the resolvers.", jp.getSignature());
		}
		for (ExceptionResolver resolver: exceptionResolvers) {
			if (resolver.supports(ex, targetEntity.map(Object::getClass))) {
				log.debug("afterThrowingAdvice - Found resolver {}", resolver.getClass().getName());
				throw new ResolvedException(resolver.resolve(ex, targetEntity), ex);
			}
		}
		// the exception could not be resolved, so the original exception will
		// get thrown
	}
	
	public final List<ExceptionResolver> getExceptionResolvers() {
		return exceptionResolvers;
	}

	@Autowired(required = false)
	public final void setExceptionResolvers(List<ExceptionResolver> exceptionResolvers) {
		this.exceptionResolvers = exceptionResolvers == null ? emptyList() : exceptionResolvers;
	}

	
	public final void setOrder(int order) {
		this.order = order;
	}

	@Override
	public final int getOrder() {
		return order;
	}
}