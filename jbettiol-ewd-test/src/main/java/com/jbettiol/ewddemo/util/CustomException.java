package com.jbettiol.ewddemo.util;

import org.springframework.web.client.RestClientException;

public class CustomException extends RuntimeException {

	private static final long serialVersionUID = 8909382171533557670L;

	public CustomException(String message, Exception e) {
		super(message, e);
	}

	public CustomException(String message, RestClientException scx, String svcErrorMessageID) {
		super(svcErrorMessageID + ": " + message, scx);
	}

	public CustomException(String message) {
		super(message);
	}

}
