package com.jbettiol.ewddemo.util;

import org.springframework.web.client.RestClientException;

public class CustomException extends RuntimeException {

	public CustomException(String message, Exception e) {
		super(message, e);
	}

	public CustomException(String message, RestClientException scx, String svcErrorMessageID) {
		super(svcErrorMessageID + ": " + message, scx);
	}

}
