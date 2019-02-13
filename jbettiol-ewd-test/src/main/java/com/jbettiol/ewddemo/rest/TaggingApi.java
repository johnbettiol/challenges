package com.jbettiol.ewddemo.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbettiol.ewddemo.tagging.TaggingService;
import com.jbettiol.ewddemo.util.CustomException;
import com.jbettiol.ewddemo.util.RequestResponseLoggingInterceptor;

@RestController
public class TaggingApi {
	public static final String JSON_KEY_STATUS = "status";
	public static final String JSON_VALUE_STATUS_SUCCESS = "success";
	public static final String URI_PREFIX = "/tagging/";

	@Autowired
	RestTemplate restTemplate;

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Autowired
	TaggingService tm;

	@RequestMapping(value = URI_PREFIX + "reset", method = RequestMethod.GET)
	public @ResponseBody String reset(HttpServletRequest request, @RequestBody(required = false) String body) {
		tm.deleteData();
		Map<String, String> payload = new HashMap<>();
		payload.put(JSON_KEY_STATUS, JSON_VALUE_STATUS_SUCCESS);
		try {
			return new ObjectMapper().writeValueAsString(payload);
		} catch (JsonProcessingException e) {
			throw new CustomException(e.getMessage(), e);
		}
	}

	// Fallback routes!

	@RequestMapping(value = URI_PREFIX + "**", method = RequestMethod.GET)
	public @ResponseBody String restProxyGet(HttpServletRequest request, @RequestBody(required = false) String body) {
		restTemplate.setInterceptors(Collections.singletonList(new RequestResponseLoggingInterceptor()));
		restTemplate.setErrorHandler(new CustomResponseErrorHandler());

		return "processed: " + request.getRequestURI();
	}

	@RequestMapping(value = URI_PREFIX + "**", headers = {
			"content-type=application/json" }, method = RequestMethod.POST)
	public @ResponseBody String restProxyPost(HttpServletRequest request, @RequestBody(required = false) String body) {
		restTemplate.setInterceptors(Collections.singletonList(new RequestResponseLoggingInterceptor()));
		restTemplate.setErrorHandler(new CustomResponseErrorHandler());

		return "processed: " + request.getRequestURI();
	}

}
