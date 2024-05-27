package com.asentinel.common.jdbc.exceptions.resolve;

import org.springframework.util.StringUtils;

/**
 * @see ThrowsResolvableException
 * 
 * @since 1.60.12
 * @author Razvan Popian
 */
@SuppressWarnings("serial")
public class ResolvedException extends RuntimeException {
	
	private final MessageCodeWrapper messageCodeWrapper;

	public ResolvedException(MessageCodeWrapper messageCodeWrapper, Exception cause) {
        super(
        		StringUtils.hasText(messageCodeWrapper.getDefaultMessage()) 
        			? messageCodeWrapper.getDefaultMessage()
        			: messageCodeWrapper.getCode(), 
        		cause);
        this.messageCodeWrapper = messageCodeWrapper;
    }

    public MessageCodeWrapper getMessageCodeWrapper() {
		return messageCodeWrapper;
	}
}
