package com.jbettiol.ewddemo.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jbettiol.ewddemo.tagging.TaggedFile;
import com.jbettiol.ewddemo.tagging.TaggingService;
import com.jbettiol.ewddemo.util.CustomException;

@RestController
public class TaggingApi {
	public static final String JSON_KEY_STATUS = "status";
	public static final String JSON_VALUE_STATUS_SUCCESS = "success";
	public static final String JSON_VALUE_STATUS_NOT_FOUND = "Dropbox Id Not Found!";
	public static final String URI_PREFIX = "/tags";

	public static final String PARAM_QUERY = "q";
	public static final String PARAM_OFFSET = "o";
	public static final String PARAM_LIMIT = "l";
	public static final String PARAM_ZIP = "zip";

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
	public @ResponseBody String taggedFileInsert(HttpServletRequest request,
			@RequestBody(required = true) String body) {
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

	@RequestMapping(path = URI_PREFIX, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_UTF8_VALUE,
			MediaType.APPLICATION_OCTET_STREAM_VALUE })
	public @ResponseBody String taggedFileSearch(HttpServletRequest request, HttpServletResponse response) {

		String query = request.getParameter(PARAM_QUERY);
		Integer offset = request.getParameter(PARAM_OFFSET) != null
				? Integer.parseInt(request.getParameter(PARAM_OFFSET))
				: null;
		Integer limit = request.getParameter(PARAM_LIMIT) != null ? Integer.parseInt(request.getParameter(PARAM_LIMIT))
				: null;

		List<TaggedFile> taggedFiles = tm.tagSearch(query, offset, limit);

		return new Gson().toJson(taggedFiles);
	}

	@RequestMapping(path = URI_PREFIX
			+ "/download", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void taggedFileDownload(HttpServletRequest request, HttpServletResponse response) {
		String query = request.getParameter(PARAM_QUERY);
		Integer offset = request.getParameter(PARAM_OFFSET) != null
				? Integer.parseInt(request.getParameter(PARAM_OFFSET))
				: null;
		Integer limit = request.getParameter(PARAM_LIMIT) != null ? Integer.parseInt(request.getParameter(PARAM_LIMIT))
				: null;

		List<TaggedFile> taggedFiles = tm.tagSearch(query, offset, limit);

		try {
			// Set the content type and attachment header.
			response.addHeader("Content-disposition", "attachment;filename=tagged-files.zip");
			response.setContentType("txt/plain");
			tm.downloadTaggedFiles(taggedFiles, response.getOutputStream());
		} catch (CustomException e) {
			throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, e.getMessage());
		} catch (IOException ioe) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, ioe.getMessage());
		}
	}
}
