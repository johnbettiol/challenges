package com.jbettiol.ewddemotest.tagging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.jbettiol.ewddemo.EwdDemoApplication;
import com.jbettiol.ewddemo.tagging.TaggedFile;
import com.jbettiol.ewddemo.util.CustomException;

// Easy example found here:
// https://www.tutorialspoint.com/spring_boot/spring_boot_rest_controller_unit_test.htm

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EwdDemoApplication.class)
@ActiveProfiles("test")
// @AutoConfigureRestDocs
public class TaggingServiceTest extends AbstractTaggingTest {



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
		taggingService.insertOrUpdate(dropboxId, filename, filepath, filesize, DEF_TAGS_TO_ADD);
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

		taggingService.fileDelete(dropboxId);
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

		clearDropboxTestFolder();

		populateTags();

		// Scenarios BEACH AND APPLICATION = 10
		CountExpectedTagQueryRows(TAG_BEACH + " AND " + TAG_APPLICATION, DEF_MAX_FILES_PER_TAG);

		// Scenarios BEACH OR APPLICATION = 30
		CountExpectedTagQueryRows(TAG_BEACH + " OR " + TAG_APPLICATION, DEF_MAX_FILES_PER_TAG * 3);

		// Scenarios MIAMI OR NOT APPLICATION = 50
		CountExpectedTagQueryRows(TAG_MIAMI + " OR NOT" + TAG_APPLICATION, DEF_MAX_FILES_PER_TAG * 5);
		try {
			File tmpZip = new File("./testOutput.zip");
			FileOutputStream fos = new FileOutputStream(tmpZip);
			taggingService.downloadTaggedFiles(taggingService.tagSearch(TAG_MIAMI), fos);
			fos.close();
		} catch (CustomException e) {
			// We want an exception thrown here
			assertTrue(e.getMessage().indexOf(STR_EXCEPTION_TAGGED_FILES_EXCEED) >= 0);
		}

		// It should work fine for just one file
		File tmpZip = new File("./testOutput.zip");
		FileOutputStream fos = new FileOutputStream(tmpZip);
		taggingService.downloadTaggedFiles(taggingService.tagSearch(TAG_BEACH + " AND " + TAG_APPLICATION), fos);
		fos.close();
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
