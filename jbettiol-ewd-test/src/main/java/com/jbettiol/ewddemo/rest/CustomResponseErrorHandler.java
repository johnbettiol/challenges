package com.jbettiol.ewddemo.rest;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;

import com.jbettiol.ewddemo.util.CustomException;

public class CustomResponseErrorHandler implements ResponseErrorHandler {

	private ResponseErrorHandler errorHandler = new DefaultResponseErrorHandler();

	public void handleError(ClientHttpResponse response) throws IOException {

		List<String> customHeader = response.getHeaders().get("x-app-err-id");

		String svcErrorMessageID = "";
		if (customHeader != null) {
			svcErrorMessageID = customHeader.get(0);
		}
		String responseString = IOUtils.toString(response.getBody(), "UTF-8");

		try {
			errorHandler.handleError(response);

		} catch (RestClientException scx) {

			throw new CustomException(responseString + "\n\n" + scx.getMessage(), scx, svcErrorMessageID);
		}
	}

	public boolean hasError(ClientHttpResponse response) throws IOException {
		return errorHandler.hasError(response);
	}
}
