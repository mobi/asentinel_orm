package com.asentinel.common.orm.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.convert.support.ConfigurableConversionService;

/**
 * @since 1.72.0
 * @author Razvan Popian
 */
public abstract class OrmConversionServiceConfig implements BeanPostProcessor {
	
	@Override
	public final Object postProcessBeforeInitialization(Object bean, String beanName) {
		if (!OrmConfig.ORM_CS_BEAN_NAME.equals(beanName)) {
			return bean;
		}
		
		ConfigurableConversionService ccs = (ConfigurableConversionService) bean;
		registerConverters(ccs);
		
		return bean;
	}
	
	@Override
	public final Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
	
	protected abstract void registerConverters(ConfigurableConversionService conversionService);
}
