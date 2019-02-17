package com.jbettiol.ewddemotest.tagging;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbettiol.ewddemo.EwdDemoApplication;
import com.jbettiol.ewddemo.dropbox.DropboxService;
import com.jbettiol.ewddemo.tagging.TaggedFile;
import com.jbettiol.ewddemo.tagging.TaggingService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EwdDemoApplication.class)
public abstract class AbstractTaggingTest {

	protected static final String STR_EXCEPTION_TAGGED_FILES_EXCEED = "Tagged files exceed";

	protected static final String TEST_FOLDER_PATH = "/EWD-TestFolder";

	protected static final int DEF_MAX_FILES_PER_TAG = 2;
	protected static final int DEF_MAX_BYTES_SIZE = 256;
	protected static final String TAG_BEACH = "beach";
	protected static final String TAG_MIAMI = "miami";
	protected static final String TAG_WORK = "work";
	protected static final String TAG_CV = "cv";
	protected static final String TAG_APPLICATION = "application";
	protected static final Set<String> DEF_TAGS_TO_ADD = new HashSet<String>() {
		private static final long serialVersionUID = -321784417496157575L;
		{
			add(TAG_BEACH);
			add(TAG_MIAMI);
			add(TAG_WORK);
			add(TAG_CV);
			add(TAG_APPLICATION);
		}
	};
	protected static final Set<String> DEF_TAGS_MINUS_CV = new HashSet<String>() {
		private static final long serialVersionUID = 2811436662715165644L;
		{
			add(TAG_BEACH);
			add(TAG_MIAMI);
			add(TAG_WORK);
			add(TAG_APPLICATION);
		}
	};

	protected MockMvc mvc;
	@Autowired
	protected WebApplicationContext webApplicationContext;
	@Autowired
	protected TaggingService taggingService;
	@Autowired
	protected DropboxService dropboxService;

	protected void setUp() {
		mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	protected String mapToJson(Object obj) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(obj);
	}

	protected <T> T mapFromJson(String json, Class<T> clazz)
			throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(json, clazz);
	}

	protected byte[] makeFileContent(int fileCount, int maxFileSize) {
		StringBuffer sb = new StringBuffer();
		int rowCount = 1;
		while (sb.length() < maxFileSize) {
			sb.append(rowCount + ": Test Upload File # " + fileCount);
			rowCount++;

		}
		return sb.toString().getBytes();
	}

	protected List<TaggedFile> populateTags() {
		List<TaggedFile> taggedFilesAdded = new ArrayList<TaggedFile>();
		// Add 100 files to index for each tags, removing a tag until none are left
		List<String> tagsToAddList = new ArrayList<String>();
		tagsToAddList.addAll(DEF_TAGS_TO_ADD);
		int fileCount = 0;
		for (int i = 0; i < tagsToAddList.size(); i++) {
			Set<String> tagSet = new HashSet<String>();
			for (String tag : DEF_TAGS_TO_ADD) {
				if (tagSet.size() <= i) {
					tagSet.add(tag);
				}
			}
			for (int j = 0; j < DEF_MAX_FILES_PER_TAG; j++) {
				fileCount++;
				String filename = "TestFile-" + fileCount + ".txt";
				String filepath = TEST_FOLDER_PATH;
				byte[] fileBytes = makeFileContent(fileCount, DEF_MAX_BYTES_SIZE);
				ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);
				FileMetadata fmd = dropboxService.uploadFile(filepath + "/" + filename, bais);
				TaggedFile newTf = new TaggedFile(fmd.getId(), filename, filepath, (long) fileBytes.length,
						tagSet);
				taggingService.insertOrUpdate(newTf);
				taggedFilesAdded.add(newTf);
			}
		}
		return taggedFilesAdded;
	}

	protected void clearDropboxTestFolder() {
		try {
			FolderMetadata fldMd = dropboxService.getFolderDetails(TEST_FOLDER_PATH);
			if (fldMd.getId() != null) {
				dropboxService.deleteFolder(TEST_FOLDER_PATH);
			}
		} catch (Exception e) {

		}
	}
}
