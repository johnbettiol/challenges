package com.jbettiol.ewddemotest.tagging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.dropbox.core.v2.files.FileMetadata;
import com.jbettiol.ewddemo.dropbox.DropboxService;
import com.jbettiol.ewddemo.rest.TaggingApi;
import com.jbettiol.ewddemo.tagging.TaggingService;

// Easy example found here:
// https://www.tutorialspoint.com/spring_boot/spring_boot_rest_controller_unit_test.htm

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TaggingApiTest extends AbstractTest {

	private static final String TAG_BEACH = "beach";
	private static final String TAG_MIAMI = "miami";
	private static final String TAG_WORK = "work";
	private static final String TAG_CV = "cv";
	private static final String TAG_APPLICATION = "application";
	private static final String[] tagsToAdd = { TAG_BEACH, TAG_MIAMI, TAG_WORK, TAG_CV, TAG_APPLICATION };

	@Autowired
	private TaggingService taggingService;
	
	@Autowired
	private DropboxService dropboxService;

	@Value("${dropbox.ewdTestFolder}")
	String dropboxTestFolder;

	@Override
	@Before
	public void setUp() {
		super.setUp();
	}

	@Test
	public void t1_resetTaggingDatabase() throws Exception {

		String uri = TaggingApi.URI_PREFIX + "reset";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)).andReturn();
		int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);
		JacksonJsonParser jjp = new JacksonJsonParser();
		Map<String, Object> jsonData = jjp.parseMap(mvcResult.getResponse().getContentAsString());
		String result = (String) jsonData.get(TaggingApi.JSON_KEY_STATUS);
		assertNotNull(result);
		assertEquals(TaggingApi.JSON_VALUE_STATUS_SUCCESS, result);
	}

	@Test
	public void t2_deleteTestFolder() throws Exception {
		dropboxService.deleteFolder(dropboxTestFolder);
	}

	@Test
	public void t3_createTestFolder() throws Exception {
		dropboxService.createFolder(dropboxTestFolder);
	}

	private String dropboxFileId = null;

	@Test
	public void t4_uploadTestFile() throws Exception {
		String filename = "file-1.txt";
		ByteArrayInputStream bais = new ByteArrayInputStream("test file contents!!".getBytes());
		FileMetadata fmd = dropboxService.uploadFile(dropboxTestFolder + "/" + filename, bais);
		assertNotNull(fmd);
		assertNotNull(fmd.getId());
		dropboxFileId = fmd.getId();
	}

	@Test
	public void t5_addTagToFile() throws Exception {
		// POST /dbv2/tagging/addTag [dbid], [tag]
		// POST /dbv2/tagging/setTags [dbid], [tags]

		String uri = TaggingApi.URI_PREFIX + "add";
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		String tagToAdd = TAG_APPLICATION;
		String content = "{\"id\":\"" + dropboxFileId + "\",\"tag\":\"" + tagToAdd + "\"}";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).
				accept(MediaType.APPLICATION_JSON_VALUE)
				.headers(headers).content(content)).andReturn();

		String contentResult = mvcResult.getResponse().getContentAsString();

		JacksonJsonParser jjp = new JacksonJsonParser();
		Map<String, Object> jsonData = jjp.parseMap(contentResult);
		assertNotNull(jsonData.get("id"));
		assertNotNull(jsonData.get("tags"));
		System.out.println(jsonData.get("tags"));
	}

	@Test
	public void t6_removeTagFromFile() throws Exception {
		// POST /dbv2/tagging/addTag [dbid], [tag]
		// POST /dbv2/tagging/setTags [dbid], [tags]

		String uri = TaggingApi.URI_PREFIX + "del";
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		String tagToRemove = TAG_APPLICATION;
		String content = "{\"id\":\"" + dropboxFileId + "\",\"tag\":\"" + tagToRemove + "\"}";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).accept(MediaType.APPLICATION_JSON_VALUE)
				.headers(headers).content(content)).andReturn();

		String contentResult = mvcResult.getResponse().getContentAsString();

		JacksonJsonParser jjp = new JacksonJsonParser();
		Map<String, Object> jsonData = jjp.parseMap(contentResult);
		assertNotNull(jsonData.get("id"));
		assertNotNull(jsonData.get("tags"));
		System.out.println(jsonData.get("tags"));
	}

	@Test
	public void t7_fileSearchWithTagQuery() throws Exception {
		// POST /dbv2/tagging/addTag [dbid], [tag]
		// POST /dbv2/tagging/setTags [dbid], [tags]

		String uri = TaggingApi.URI_PREFIX + "search";
		HttpHeaders headers = new HttpHeaders();
		String tagQuery = TAG_BEACH;
		String content = "{\"query\":\"" + tagQuery + "\"}";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).accept(MediaType.APPLICATION_JSON_VALUE)
				.headers(headers).content(content)).andReturn();

		String contentResult = mvcResult.getResponse().getContentAsString();

		JacksonJsonParser jjp = new JacksonJsonParser();
		Map<String, Object> jsonData = jjp.parseMap(contentResult);
		assertNotNull(jsonData.get("files"));
		System.out.println(jsonData.get("files"));
	}

	@Test
	public void t8_fileDownloadWithTagQuery() throws Exception {
		// POST /dbv2/tagging/addTag [dbid], [tag]
		// POST /dbv2/tagging/setTags [dbid], [tags]

		String uri = TaggingApi.URI_PREFIX + "download";
		HttpHeaders headers = new HttpHeaders();
		String tagQuery = TAG_BEACH;
		String content = "{\"query\":\"" + tagQuery + "\"}";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).accept(MediaType.APPLICATION_JSON_VALUE)
				.headers(headers).content(content)).andReturn();

		String contentResult = mvcResult.getResponse().getContentAsString();

		JacksonJsonParser jjp = new JacksonJsonParser();
		Map<String, Object> jsonData = jjp.parseMap(contentResult);
		assertNotNull(jsonData.get("files"));
		System.out.println(jsonData.get("files"));
	}

}
