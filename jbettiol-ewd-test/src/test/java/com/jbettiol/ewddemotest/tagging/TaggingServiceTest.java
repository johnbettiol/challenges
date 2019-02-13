package com.jbettiol.ewddemotest.tagging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.jbettiol.ewddemo.EwdDemoApplication;
import com.jbettiol.ewddemo.tagging.TaggedFile;
import com.jbettiol.ewddemo.tagging.TaggingService;

// Easy example found here:
// https://www.tutorialspoint.com/spring_boot/spring_boot_rest_controller_unit_test.htm

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EwdDemoApplication.class)
public class TaggingServiceTest {

	private static final int DEF_MAX_FILES_PER_TAG = 10;
	private static final String TAG_BEACH = "beach";
	private static final String TAG_MIAMI = "miami";
	private static final String TAG_WORK = "work";
	private static final String TAG_CV = "cv";
	private static final String TAG_APPLICATION = "application";
	private static final Set<String> DEF_TAGS_TO_ADD = new HashSet<String>() {
		{
			add(TAG_BEACH);
			add(TAG_MIAMI);
			add(TAG_WORK);
			add(TAG_CV);
			add(TAG_APPLICATION);
		}
	};
	private static final Set<String> DEF_TAGS_MINUS_CV = new HashSet<String>() {
		{
			add(TAG_BEACH);
			add(TAG_MIAMI);
			add(TAG_WORK);
			add(TAG_APPLICATION);
		}
	};

	@Autowired
	private TaggingService taggingService;

	/**
	 * This method adds a file to the SOLR index and tests all of the tagging
	 * functionality
	 * 
	 * @throws Exception
	 */
	@Test
	public void taggingAFile() throws Exception {
		taggingService.deleteData();
		String dropboxId = "dbid1";
		String filename = "Test File.txt";
		String filepath = "/path1/path2/";
		Long filesize = 1024L;
		taggingService.fileInsert(dropboxId, filename, filepath, filesize, DEF_TAGS_TO_ADD);
		TaggedFile newTaggedFile = taggingService.fileLoadByDropboxId(dropboxId);
		assertNotNull(newTaggedFile);
		assertThat(dropboxId).isEqualTo(newTaggedFile.getDropboxId());
		assertThat(filename).isEqualTo(newTaggedFile.getFilename());
		assertThat(filepath).isEqualTo(newTaggedFile.getFilepath());
		assertThat(filesize).isEqualTo(newTaggedFile.getFilesize());
		assertThat(DEF_TAGS_TO_ADD).hasSameElementsAs(newTaggedFile.getTags());

		List<TaggedFile> taggedFiles1 = taggingService.tagSearch(TAG_CV);
		TaggedFile searchResult1 = taggedFiles1.get(0);
		assertThat(taggedFiles1).hasSize(1);
		assertNotNull(searchResult1);
		assertThat(dropboxId).isEqualTo(searchResult1.getDropboxId());
		assertThat(filename).isEqualTo(searchResult1.getFilename());
		assertThat(filepath).isEqualTo(searchResult1.getFilepath());
		assertThat(filesize).isEqualTo(searchResult1.getFilesize());
		assertThat(DEF_TAGS_TO_ADD).hasSameElementsAs(searchResult1.getTags());

		taggingService.tagDel(dropboxId, TAG_CV);
		TaggedFile updatedTaggedFile = taggingService.fileLoadByDropboxId(dropboxId);
		assertNotNull(updatedTaggedFile);
		assertThat(dropboxId).isEqualTo(updatedTaggedFile.getDropboxId());
		assertThat(filename).isEqualTo(updatedTaggedFile.getFilename());
		assertThat(filepath).isEqualTo(updatedTaggedFile.getFilepath());
		assertThat(filesize).isEqualTo(updatedTaggedFile.getFilesize());
		assertThat(DEF_TAGS_MINUS_CV).hasSameElementsAs(updatedTaggedFile.getTags());

		List<TaggedFile> taggedFiles2 = taggingService.tagSearch(TAG_CV);
		assertThat(taggedFiles2).hasSize(0);

		taggingService.tagAdd(dropboxId, TAG_CV);
		TaggedFile updatedTaggedFile2 = taggingService.fileLoadByDropboxId(dropboxId);
		assertNotNull(updatedTaggedFile2);
		assertThat(dropboxId).isEqualTo(updatedTaggedFile2.getDropboxId());
		assertThat(filename).isEqualTo(updatedTaggedFile2.getFilename());
		assertThat(filepath).isEqualTo(updatedTaggedFile2.getFilepath());
		assertThat(filesize).isEqualTo(updatedTaggedFile2.getFilesize());
		assertThat(DEF_TAGS_TO_ADD).isEqualTo(updatedTaggedFile2.getTags());

		List<TaggedFile> taggedFiles3 = taggingService.tagSearch(TAG_CV);
		assertThat(taggedFiles3).hasSize(1);

		taggingService.fileRemove(dropboxId);
		List<TaggedFile> taggedFiles4 = taggingService.tagSearch(TAG_CV);
		assertThat(taggedFiles4).hasSize(0);
	}

	/**
	 * This test populates the index with many files and checked the pagination,
	 * download and download limit pieces of the TaggingAPI
	 * 
	 * @throws Exception
	 */
	@Test
	public void taggedFilesPaginationAndDownload() throws Exception {
		taggingService.deleteData();
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
				String dropboxId = "dbid-" + fileCount;
				String filename = "TestFile-" + fileCount + ".txt";
				String filepath = "/path" + i + "/path" + j + "/";
				Long filesize = 1024L;

				taggingService.fileInsert(dropboxId, filename, filepath, filesize, tagSet);
			}
		}

		// Scenarios BEACH AND APPLICATION = 10
		CountExpectedTagQueryRows(TAG_BEACH + " AND " + TAG_APPLICATION, DEF_MAX_FILES_PER_TAG);

		// Scenarios BEACH OR APPLICATION = 50
		CountExpectedTagQueryRows(TAG_BEACH + " OR " + TAG_APPLICATION, DEF_MAX_FILES_PER_TAG * 5);
	}

	private void CountExpectedTagQueryRows(String tagQuery, Integer expectedCount) {
		List<TaggedFile> searchResults = taggingService.tagSearch(tagQuery);
		Integer resultsSize = searchResults.size();
		assertEquals(expectedCount, resultsSize, 0);
	}

	@After
	public void resetTaggingDatabase() throws Exception {
		taggingService.deleteData();
	}

}
