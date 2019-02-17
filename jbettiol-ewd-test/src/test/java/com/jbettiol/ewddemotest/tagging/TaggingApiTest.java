package com.jbettiol.ewddemotest.tagging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;
import com.jbettiol.ewddemo.EwdDemoApplication;
import com.jbettiol.ewddemo.dropbox.DropboxService;
import com.jbettiol.ewddemo.rest.TaggingApi;
import com.jbettiol.ewddemo.tagging.TaggedFile;
import com.jbettiol.ewddemo.tagging.TaggingService;
import com.jbettiol.ewddemo.util.CustomException;

// Easy example found here:
// https://www.tutorialspoint.com/spring_boot/spring_boot_rest_controller_unit_test.htm

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EwdDemoApplication.class)
@ActiveProfiles("test")
public class TaggingApiTest extends AbstractTest {

	@Autowired
	private TaggingService taggingService;
	@Autowired
	private DropboxService dropboxService;

	@Override
	@Before
	public void setUp() {
		super.setUp();
	}

	@Test
	public void resetTaggingDatabase() throws Exception {
		String uri = TaggingApi.URI_PREFIX + "/reset";
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
	public void testInsertUpdateAndDelete() throws Exception {

		// Test file to index
		TaggedFile tf = new TaggedFile("dbid1", "TestFile.txt", "/path1/path2/", 1024, DEF_TAGS_TO_ADD);
		postTaggedFile(tf);

		// Test file patch
		TaggedFile tfPatched = patchTaggedFile(tf);

		TaggedFile tfBeachAdded = tagAdd(tfPatched, TAG_BEACH);

		TaggedFile tfBeachRemoved = tagDel(tfBeachAdded, TAG_BEACH);

		// Call Delete API endpoint
		deleteTaggedFile(tf, 200);

		// Make sure deletd file was actually removed from index!
		assertNull(taggingService.fileLoadByDropboxId(tf.getDropboxId()));

		// Call Delete API endpoint 2nd time
		deleteTaggedFile(tf, 404);

	}

	private void postTaggedFile(TaggedFile tf) {
		String uri = TaggingApi.URI_PREFIX;

		// use insert api endpoint to add to index
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		Gson gson = new Gson();
		String jsonString = gson.toJson(tf);

		MvcResult mvcResult;
		try {
			mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).accept(MediaType.APPLICATION_JSON_VALUE)
					.headers(headers).content(jsonString)).andReturn();
			int status = mvcResult.getResponse().getStatus();
			assertEquals(200, status);
			String requestContent = mvcResult.getResponse().getContentAsString();
			// Parse response object
			JacksonJsonParser jjp = new JacksonJsonParser();
			Map<String, Object> jsonData = jjp.parseMap(requestContent);
			String result = (String) jsonData.get(TaggingApi.JSON_KEY_STATUS);
			assertNotNull(result);
			// Check response is Success!
			assertEquals(TaggingApi.JSON_VALUE_STATUS_SUCCESS, result);

			// Check if new tagged file is in index
			TaggedFile tfNew = taggingService.fileLoadByDropboxId(tf.getDropboxId());
			assertNotNull(tfNew);
			// Validate object sent to API matches object returned from TaggingService
			assertTrue(EqualsBuilder.reflectionEquals(tf, tfNew));
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), e);
		}
	}

	private TaggedFile patchTaggedFile(TaggedFile tf) {
		String uri = TaggingApi.URI_PREFIX + "/" + tf.getDropboxId();
		TaggedFile patchedFile = new TaggedFile();
		patchedFile.setFilename("Changed filename.txt");
		Set<String> newTags = new HashSet();
		newTags.addAll(tf.getTags());
		newTags.remove(TAG_BEACH);
		patchedFile.setTags(newTags);

		// use insert api end-ppoint to add to index
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		Gson gson = new Gson();

		String jsonString = gson.toJson(patchedFile);

		MvcResult mvcResult;
		try {
			mvcResult = mvc.perform(MockMvcRequestBuilders.patch(uri).accept(MediaType.APPLICATION_JSON_VALUE)
					.headers(headers).content(jsonString)).andReturn();
			int status = mvcResult.getResponse().getStatus();
			assertEquals(200, status);
			String requestContent = mvcResult.getResponse().getContentAsString();
			// Parse response object
			JacksonJsonParser jjp = new JacksonJsonParser();
			Map<String, Object> jsonData = jjp.parseMap(requestContent);
			String result = (String) jsonData.get(TaggingApi.JSON_KEY_STATUS);
			assertNotNull(result);
			// Check response is Success!
			assertEquals(TaggingApi.JSON_VALUE_STATUS_SUCCESS, result);

			// Check if new tagged file is in index
			TaggedFile tfNew = taggingService.fileLoadByDropboxId(tf.getDropboxId());
			assertNotNull(tfNew);
			

			tf.patch(patchedFile);
			
			// Validate object sent to API matches object returned from TaggingService
			assertTrue(EqualsBuilder.reflectionEquals(tf, tfNew));

			return tfNew;
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), e);
		}
	}
	
	private TaggedFile tagAdd(TaggedFile tf, String tagToAdd) throws Exception {
		String tagAddUri = TaggingApi.URI_PREFIX + "/" + tf.getDropboxId() + "/" + tagToAdd;
		// use insert api endpoint to add to index
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		MvcResult mvcTagAddResult = mvc.perform(MockMvcRequestBuilders.put(tagAddUri)
				.accept(MediaType.APPLICATION_JSON_VALUE).headers(headers)).andReturn();
		assertEquals(200, mvcTagAddResult.getResponse().getStatus());
		// Check if new tagged file is in index
		TaggedFile tfTagAdded = taggingService.fileLoadByDropboxId(tf.getDropboxId());
		assertTrue(tfTagAdded.getTags().contains(tagToAdd));
		return tfTagAdded;
	}

	
	private TaggedFile tagDel(TaggedFile tf, String tagToDel) throws Exception {
		String tagDeleUri = TaggingApi.URI_PREFIX + "/" + tf.getDropboxId() + "/" + tagToDel;
		// use insert api endpoint to add to index
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		MvcResult mvcTagAddResult = mvc.perform(MockMvcRequestBuilders.delete(tagDeleUri)
				.accept(MediaType.APPLICATION_JSON_VALUE).headers(headers)).andReturn();
		assertEquals(200, mvcTagAddResult.getResponse().getStatus());
		// Check if new tagged file is in index
		TaggedFile tfTagDeleted = taggingService.fileLoadByDropboxId(tf.getDropboxId());
		assertFalse(tfTagDeleted.getTags().contains(tagToDel));
		return tfTagDeleted;
	}

	private void deleteTaggedFile(TaggedFile tf, int expectedResponse) throws Exception {
		String deleteUri = TaggingApi.URI_PREFIX + "/" + tf.getDropboxId();
		// use insert api endpoint to add to index
		HttpHeaders deleteHeaders = new HttpHeaders();
		deleteHeaders.add("Content-Type", "application/json");
		MvcResult mvcDeleteResult = mvc.perform(MockMvcRequestBuilders.delete(deleteUri)
				.accept(MediaType.APPLICATION_JSON_VALUE).headers(deleteHeaders)).andReturn();
		assertEquals(expectedResponse, mvcDeleteResult.getResponse().getStatus());

	}

	/*
	 * private String dropboxFileId = null;
	 * 
	 * @Test public void t4_uploadTestFile() throws Exception { String filename =
	 * "file-1.txt"; ByteArrayInputStream bais = new
	 * ByteArrayInputStream("test file contents!!".getBytes()); FileMetadata fmd =
	 * dropboxService.uploadFile(dropboxTestFolder + "/" + filename, bais);
	 * assertNotNull(fmd); assertNotNull(fmd.getId()); dropboxFileId = fmd.getId();
	 * }
	 * 
	 * @Test public void t5_addTagToFile() throws Exception { // POST
	 * /dbv2/tagging/addTag [dbid], [tag] // POST /dbv2/tagging/setTags [dbid],
	 * [tags]
	 * 
	 * String uri = TaggingApi.URI_PREFIX + "add"; HttpHeaders headers = new
	 * HttpHeaders(); headers.add("Content-Type", "application/json"); String
	 * tagToAdd = TAG_APPLICATION; String content = "{\"id\":\"" + dropboxFileId +
	 * "\",\"tag\":\"" + tagToAdd + "\"}"; MvcResult mvcResult =
	 * mvc.perform(MockMvcRequestBuilders.post(uri).
	 * accept(MediaType.APPLICATION_JSON_VALUE)
	 * .headers(headers).content(content)).andReturn();
	 * 
	 * String contentResult = mvcResult.getResponse().getContentAsString();
	 * 
	 * JacksonJsonParser jjp = new JacksonJsonParser(); Map<String, Object> jsonData
	 * = jjp.parseMap(contentResult); assertNotNull(jsonData.get("id"));
	 * assertNotNull(jsonData.get("tags"));
	 * System.out.println(jsonData.get("tags")); }
	 * 
	 * @Test public void t6_removeTagFromFile() throws Exception { // POST
	 * /dbv2/tagging/addTag [dbid], [tag] // POST /dbv2/tagging/setTags [dbid],
	 * [tags]
	 * 
	 * String uri = TaggingApi.URI_PREFIX + "del"; HttpHeaders headers = new
	 * HttpHeaders(); headers.add("Content-Type", "application/json"); String
	 * tagToRemove = TAG_APPLICATION; String content = "{\"id\":\"" + dropboxFileId
	 * + "\",\"tag\":\"" + tagToRemove + "\"}"; MvcResult mvcResult =
	 * mvc.perform(MockMvcRequestBuilders.post(uri).accept(MediaType.
	 * APPLICATION_JSON_VALUE) .headers(headers).content(content)).andReturn();
	 * 
	 * String contentResult = mvcResult.getResponse().getContentAsString();
	 * 
	 * JacksonJsonParser jjp = new JacksonJsonParser(); Map<String, Object> jsonData
	 * = jjp.parseMap(contentResult); assertNotNull(jsonData.get("id"));
	 * assertNotNull(jsonData.get("tags"));
	 * System.out.println(jsonData.get("tags")); }
	 * 
	 * @Test public void t7_fileSearchWithTagQuery() throws Exception { // POST
	 * /dbv2/tagging/addTag [dbid], [tag] // POST /dbv2/tagging/setTags [dbid],
	 * [tags]
	 * 
	 * String uri = TaggingApi.URI_PREFIX + "search"; HttpHeaders headers = new
	 * HttpHeaders(); String tagQuery = TAG_BEACH; String content = "{\"query\":\""
	 * + tagQuery + "\"}"; MvcResult mvcResult =
	 * mvc.perform(MockMvcRequestBuilders.post(uri).accept(MediaType.
	 * APPLICATION_JSON_VALUE) .headers(headers).content(content)).andReturn();
	 * 
	 * String contentResult = mvcResult.getResponse().getContentAsString();
	 * 
	 * JacksonJsonParser jjp = new JacksonJsonParser(); Map<String, Object> jsonData
	 * = jjp.parseMap(contentResult); assertNotNull(jsonData.get("files"));
	 * System.out.println(jsonData.get("files")); }
	 * 
	 * @Test public void t8_fileDownloadWithTagQuery() throws Exception { // POST
	 * /dbv2/tagging/addTag [dbid], [tag] // POST /dbv2/tagging/setTags [dbid],
	 * [tags]
	 * 
	 * String uri = TaggingApi.URI_PREFIX + "download"; HttpHeaders headers = new
	 * HttpHeaders(); String tagQuery = TAG_BEACH; String content = "{\"query\":\""
	 * + tagQuery + "\"}"; MvcResult mvcResult =
	 * mvc.perform(MockMvcRequestBuilders.post(uri).accept(MediaType.
	 * APPLICATION_JSON_VALUE) .headers(headers).content(content)).andReturn();
	 * 
	 * String contentResult = mvcResult.getResponse().getContentAsString();
	 * 
	 * JacksonJsonParser jjp = new JacksonJsonParser(); Map<String, Object> jsonData
	 * = jjp.parseMap(contentResult); assertNotNull(jsonData.get("files"));
	 * System.out.println(jsonData.get("files")); }
	 */
}
