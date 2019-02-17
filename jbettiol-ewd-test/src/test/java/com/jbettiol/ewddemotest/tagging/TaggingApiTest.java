package com.jbettiol.ewddemotest.tagging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.gson.Gson;
import com.jbettiol.ewddemo.EwdDemoApplication;
import com.jbettiol.ewddemo.rest.TaggingApi;
import com.jbettiol.ewddemo.tagging.TaggedFile;
import com.jbettiol.ewddemo.util.CustomException;

// Easy example found here:
// https://www.tutorialspoint.com/spring_boot/spring_boot_rest_controller_unit_test.htm

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EwdDemoApplication.class)
@ActiveProfiles("test")
// @AutoConfigureRestDocs
public class TaggingApiTest extends AbstractTaggingTest {

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

		tagDel(tfBeachAdded, TAG_BEACH);

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
		Set<String> newTags = new HashSet<String>();
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
		MvcResult mvcTagAddResult = mvc
				.perform(
						MockMvcRequestBuilders.put(tagAddUri).accept(MediaType.APPLICATION_JSON_VALUE).headers(headers))
				.andReturn();
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
		MvcResult mvcTagAddResult = mvc.perform(
				MockMvcRequestBuilders.delete(tagDeleUri).accept(MediaType.APPLICATION_JSON_VALUE).headers(headers))
				.andReturn();
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

	public static final <T> List<T> getList(final Class<T[]> clazz, final String json) {
		final T[] jsonToObject = new Gson().fromJson(json, clazz);
		return new LinkedList<T>(Arrays.asList(jsonToObject));
	}

	@Test
	public void testSearch() throws Exception {
		taggingService.deleteData();
		clearDropboxTestFolder();
		List<TaggedFile> filesAdded = populateTags();

		// First test search queries

		String tagQuery = TAG_BEACH;
		List<TaggedFile> taggedFiles = doTagQuery(tagQuery, null, null);
		assertEquals(DEF_MAX_FILES_PER_TAG, taggedFiles.size());
		assertEquals(true, taggedFiles.get(0).getTags().contains(TAG_BEACH));

		String tagQuery2 = TAG_CV;
		List<TaggedFile> taggedFiles2 = doTagQuery(tagQuery2, null, null);
		assertEquals(DEF_MAX_FILES_PER_TAG * 4, taggedFiles2.size());

		String tagQuery3 = TAG_BEACH + " OR " + TAG_CV;
		List<TaggedFile> taggedFiles3 = doTagQuery(tagQuery3, null, null);
		assertEquals(DEF_MAX_FILES_PER_TAG * 4, taggedFiles3.size());
		assertEquals(true, taggedFiles3.get(0).getTags().contains(TAG_BEACH));

		// See if we can get out all files
		String tagQuery4 = "";
		List<TaggedFile> taggedFiles4 = doTagQuery(tagQuery4, null, null);
		assertEquals(filesAdded.size(), taggedFiles4.size());

		String tagQuery5 = "";
		List<TaggedFile> taggedFiles5 = doTagQuery(tagQuery5, null, 5);
		assertEquals(5, taggedFiles5.size());

		String tagQuery6 = "";
		List<TaggedFile> taggedFiles6 = doTagQuery(tagQuery6, 5, 5);
		assertEquals(5, taggedFiles6.size());
		assertNotEquals(taggedFiles5, taggedFiles6);

		// try download too many files
		verifyTagQueryDownload(tagQuery4, null, null, HttpStatus.PAYLOAD_TOO_LARGE.value(), taggedFiles4);

		// try download the correct amount of files
		verifyTagQueryDownload(tagQuery, null, null, HttpStatus.OK.value(), taggedFiles);

		taggingService.deleteData();

	}

	private List<TaggedFile> doTagQuery(String tagQuery, Integer offset, Integer limit)
			throws Exception, UnsupportedEncodingException {

		String tagAddUri = TaggingApi.URI_PREFIX;
		// use insert api endpoint to add to index
		HttpHeaders headers = new HttpHeaders();
//		headers.add("Content-Type", "");	

		final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

		params.add(TaggingApi.PARAM_QUERY, tagQuery);
		if (offset != null) {
			params.add(TaggingApi.PARAM_OFFSET, offset.toString());
		}
		if (limit != null) {
			params.add(TaggingApi.PARAM_LIMIT, limit.toString());
		}
		MvcResult mvcTagAddResult = mvc.perform(MockMvcRequestBuilders.get(tagAddUri).params(params)
				.accept(MediaType.APPLICATION_JSON_VALUE).headers(headers)).andReturn();
		assertEquals(200, mvcTagAddResult.getResponse().getStatus());

		int status = mvcTagAddResult.getResponse().getStatus();
		assertEquals(200, status);
		String resContent = mvcTagAddResult.getResponse().getContentAsString();

		return getList(TaggedFile[].class, resContent);
	}

	private void verifyTagQueryDownload(String tagQuery, Integer offset, Integer limit, int expectedResponse,
			List<TaggedFile> expectedTaggedFiles) throws Exception, UnsupportedEncodingException {

		String tagAddUri = TaggingApi.URI_PREFIX + "/download";
		// use insert api endpoint to add to index
		HttpHeaders headers = new HttpHeaders();
//		headers.add("Content-Type", "");	

		final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

		params.add(TaggingApi.PARAM_QUERY, tagQuery);
		if (offset != null) {
			params.add(TaggingApi.PARAM_OFFSET, offset.toString());
		}
		if (limit != null) {
			params.add(TaggingApi.PARAM_LIMIT, limit.toString());
		}
		MvcResult mvcTagAddResult = mvc.perform(MockMvcRequestBuilders.get(tagAddUri).params(params)
				.accept(MediaType.APPLICATION_OCTET_STREAM_VALUE).headers(headers)).andReturn();

		assertEquals(expectedResponse, mvcTagAddResult.getResponse().getStatus());
		ZipInputStream zis = new ZipInputStream(
				new ByteArrayInputStream(mvcTagAddResult.getResponse().getContentAsByteArray()));
		ZipEntry ze;
		while ((ze = zis.getNextEntry()) != null) {

			System.out.format("File: %s Size: %d Last Modified %s %n", ze.getName(), ze.getSize(), ze.getTime());
			TaggedFile fileToCheck = expectedTaggedFiles.remove(0);
			assertEquals(fileToCheck.getFilename(), ze.getName());

		}

	}

}
