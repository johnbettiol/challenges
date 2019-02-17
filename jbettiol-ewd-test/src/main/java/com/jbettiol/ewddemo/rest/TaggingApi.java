package com.jbettiol.ewddemo.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jbettiol.ewddemo.tagging.TaggedFile;
import com.jbettiol.ewddemo.tagging.TaggingService;
import com.jbettiol.ewddemo.util.CustomException;
import com.jbettiol.ewddemo.util.RequestResponseLoggingInterceptor;

@RestController
public class TaggingApi {
	public static final String JSON_KEY_STATUS = "status";
	public static final String JSON_VALUE_STATUS_SUCCESS = "success";
	public static final String JSON_VALUE_STATUS_NOT_FOUND = "Dropbox Id Not Found!";
	public static final String URI_PREFIX = "/tags";

	@Autowired
	RestTemplate restTemplate;

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Autowired
	TaggingService tm;

	@RequestMapping(value = URI_PREFIX + "/reset", method = RequestMethod.GET)
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

	@RequestMapping(path = URI_PREFIX, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody String taggedFileInsert(HttpServletRequest request, @RequestBody(required = true) String body) {
		TaggedFile fileToInsert = new Gson().fromJson(body, TaggedFile.class);
		tm.insertOrUpdate(fileToInsert);
		Map<String, String> payload = new HashMap<>();
		payload.put(JSON_KEY_STATUS, JSON_VALUE_STATUS_SUCCESS);
		try {
			return new ObjectMapper().writeValueAsString(payload);
		} catch (JsonProcessingException e) {
			throw new CustomException(e.getMessage(), e);
		}
	}

	@RequestMapping(path = URI_PREFIX
			+ "/{dropboxId}", method = RequestMethod.PATCH, consumes = "application/json", produces = "application/json")
	public @ResponseBody String taggedFilePatch(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String dropboxId, @RequestBody(required = true) String body) {
		TaggedFile fileToUpdate = tm.fileLoadByDropboxId(dropboxId);
		if (fileToUpdate == null) {
			Map<String, String> payload = new HashMap<>();
			payload.put(JSON_KEY_STATUS, JSON_VALUE_STATUS_NOT_FOUND);
			response.setStatus(404);
			try {
				return new ObjectMapper().writeValueAsString(payload);
			} catch (JsonProcessingException e) {
				throw new CustomException(e.getMessage(), e);
			}

		}
		TaggedFile updateContents = new Gson().fromJson(body, TaggedFile.class);
		fileToUpdate.patch(updateContents);
		tm.insertOrUpdate(fileToUpdate);

		Map<String, String> payload = new HashMap<>();
		payload.put(JSON_KEY_STATUS, JSON_VALUE_STATUS_SUCCESS);
		try {
			return new ObjectMapper().writeValueAsString(payload);
		} catch (JsonProcessingException e) {
			throw new CustomException(e.getMessage(), e);
		}
	}

	@RequestMapping(path = URI_PREFIX
			+ "/{dropboxId}/{tagToAdd}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
	public @ResponseBody String taggedFileTagAdd(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String dropboxId, @PathVariable String tagToAdd) {
		TaggedFile fileToUpdate = tm.fileLoadByDropboxId(dropboxId);
		if (fileToUpdate == null) {
			Map<String, String> payload = new HashMap<>();
			payload.put(JSON_KEY_STATUS, JSON_VALUE_STATUS_NOT_FOUND);
			response.setStatus(404);
			try {
				return new ObjectMapper().writeValueAsString(payload);
			} catch (JsonProcessingException e) {
				throw new CustomException(e.getMessage(), e);
			}

		}
		tm.tagAdd(dropboxId, tagToAdd);
		Map<String, String> payload = new HashMap<>();
		payload.put(JSON_KEY_STATUS, JSON_VALUE_STATUS_SUCCESS);
		try {
			return new ObjectMapper().writeValueAsString(payload);
		} catch (JsonProcessingException e) {
			throw new CustomException(e.getMessage(), e);
		}
	}
	
	@RequestMapping(path = URI_PREFIX
			+ "/{dropboxId}/{tagToDel}", method = RequestMethod.DELETE, consumes = "application/json", produces = "application/json")
	public @ResponseBody String taggedFileTagDel(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String dropboxId, @PathVariable String tagToDel) {
		TaggedFile fileToUpdate = tm.fileLoadByDropboxId(dropboxId);
		if (fileToUpdate == null) {
			Map<String, String> payload = new HashMap<>();
			payload.put(JSON_KEY_STATUS, JSON_VALUE_STATUS_NOT_FOUND);
			response.setStatus(404);
			try {
				return new ObjectMapper().writeValueAsString(payload);
			} catch (JsonProcessingException e) {
				throw new CustomException(e.getMessage(), e);
			}

		}
		tm.tagDel(dropboxId, tagToDel);
		Map<String, String> payload = new HashMap<>();
		payload.put(JSON_KEY_STATUS, JSON_VALUE_STATUS_SUCCESS);
		try {
			return new ObjectMapper().writeValueAsString(payload);
		} catch (JsonProcessingException e) {
			throw new CustomException(e.getMessage(), e);
		}
	}


	@RequestMapping(path = URI_PREFIX
			+ "/{dropboxId}", method = RequestMethod.DELETE, consumes = "application/json", produces = "application/json")
	public @ResponseBody String taggedFileDelete(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String dropboxId) {
		TaggedFile fileToUpdate = tm.fileLoadByDropboxId(dropboxId);
		if (fileToUpdate == null) {
			Map<String, String> payload = new HashMap<>();
			payload.put(JSON_KEY_STATUS, JSON_VALUE_STATUS_NOT_FOUND);
			response.setStatus(404);
			try {
				return new ObjectMapper().writeValueAsString(payload);
			} catch (JsonProcessingException e) {
				throw new CustomException(e.getMessage(), e);
			}

		}
		tm.fileDelete(dropboxId);
		Map<String, String> payload = new HashMap<>();
		payload.put(JSON_KEY_STATUS, JSON_VALUE_STATUS_SUCCESS);
		try {
			return new ObjectMapper().writeValueAsString(payload);
		} catch (JsonProcessingException e) {
			throw new CustomException(e.getMessage(), e);
		}
	}

	
	/*
	 * 
	 * @RequestMapping(value = URI_PREFIX + "insertFile", method =
	 * RequestMethod.GET) public @ResponseBody String insertTFile(HttpServletRequest
	 * request,
	 * 
	 * @RequestBody(required = false) TaggedFile fileToInsert) {
	 * tm.fileInsert(fileToInsert); Map<String, String> payload = new HashMap<>();
	 * payload.put(JSON_KEY_STATUS, JSON_VALUE_STATUS_SUCCESS); try { return new
	 * ObjectMapper().writeValueAsString(payload); } catch (JsonProcessingException
	 * e) { throw new CustomException(e.getMessage(), e); } }
	 * 
	 */

	// Fallback routes!
/*
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
*/
}
