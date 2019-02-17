package com.jbettiol.ewddemo.tagging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.SolrDocument;

public class TaggedFile implements Cloneable {
	public static final String KEY_DROPBOX_ID = "id";
	public static final String KEY_FILENAME = "filename";
	public static final String KEY_FILEPATH = "filepath";
	public static final String KEY_FILESIZE = "filesize";
	public static final String KEY_TAGS = "tags";

	private String dropboxId, filename, filepath;
	private Long filesize;
	private Set<String> tags;

	public TaggedFile(String dropboxId, String filename, String filepath, long filesize, Set<String> tags) {
		this.dropboxId = dropboxId;
		this.filename = filename;
		this.filepath = filepath;
		this.filesize = filesize;
		this.tags = tags;
	}

	@SuppressWarnings("unchecked")
	public TaggedFile(SolrDocument newDoc) {
		this.dropboxId = (String) newDoc.getFieldValue(KEY_DROPBOX_ID);
		this.filename = ((ArrayList<String>) newDoc.getFieldValue(KEY_FILENAME)).get(0);
		this.filepath = ((ArrayList<String>) newDoc.getFieldValue(KEY_FILEPATH)).get(0);
		this.filesize = ((ArrayList<Long>) newDoc.getFieldValue(KEY_FILESIZE)).get(0);
		this.tags = new HashSet<String>((List<String>) (List<?>) Arrays
				.asList(((ArrayList<String>) newDoc.getFieldValue(KEY_TAGS)).get(0).split(" ")));
	}

	public TaggedFile() {
	}

	public String getDropboxId() {
		return dropboxId;
	}

	public String getFilename() {
		return filename;
	}

	public String getFilepath() {
		return filepath;
	}

	public Long getFilesize() {
		return filesize;
	}

	public Set<String> getTags() {
		return tags;
	}

	public String toString() {
		return dropboxId + ": " + filename + ", " + filepath + ", (" + filesize + ", (" + tags + ")";
	}

	public TaggedFile clone() {
		Set<String> replTags = new HashSet<String>();
		replTags.addAll(tags);
		return new TaggedFile(dropboxId, filename, filepath, filesize, replTags);
	}

	public void setFilename(String newFilename) {
		this.filename = newFilename;
	}

	public void removeTag(String tagToRemove) {
		this.tags.remove(tagToRemove);
	}

	public void setTags(Set<String> newTags) {
		this.tags = newTags;
	}

	public void patch(TaggedFile updateContents) {
		if (updateContents.getFilename() != null) {
			this.filename = updateContents.getFilename();
		}
		if (updateContents.getFilepath() != null) {
			this.filepath = updateContents.getFilepath();
		}
		if (updateContents.getFilesize()  != null) {
			this.filesize = updateContents.getFilesize();
		}
		if (updateContents.getTags() != null) {
			this.tags = updateContents.getTags();
		}
	}
}
