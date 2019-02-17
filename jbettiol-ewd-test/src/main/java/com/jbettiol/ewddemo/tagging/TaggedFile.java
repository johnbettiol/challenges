package com.jbettiol.ewddemo.tagging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.SolrDocument;

public class TaggedFile {
	public static final String KEY_DROPBOX_ID = "id";
	public static final String KEY_FILENAME = "filename";
	public static final String KEY_FILEPATH = "filepath";
	public static final String KEY_FILESIZE = "filesize";
	public static final String KEY_TAGS = "tags";

	SolrDocument taggedFileDoc;

	public TaggedFile(SolrDocument newDoc) {
		this.taggedFileDoc = newDoc;

	}

	public String getDropboxId() {
		return (String) taggedFileDoc.getFieldValue(KEY_DROPBOX_ID);
	}
	@SuppressWarnings("unchecked")
	public String getFilename() {
		return ((ArrayList<String>) taggedFileDoc.getFieldValue(KEY_FILENAME)).get(0);
	}

	@SuppressWarnings("unchecked")
	public String getFilepath() {
		return ((ArrayList<String>) taggedFileDoc.getFieldValue(KEY_FILEPATH)).get(0);
	}

	@SuppressWarnings("unchecked")
	public Long getFilesize() {
		return ((ArrayList<Long>) taggedFileDoc.getFieldValue(KEY_FILESIZE)).get(0);
	}

	@SuppressWarnings("unchecked")
	public Set<String> getTags() {
		return new HashSet<String>((List<String>) (List<?>) Arrays
				.asList(((ArrayList<String>) taggedFileDoc.getFieldValue(KEY_TAGS)).get(0).split(" ")));
	}


	@SuppressWarnings("unchecked")
	public String toString() {
		return taggedFileDoc.getFieldValue(KEY_DROPBOX_ID) + ": "
				+ ((ArrayList<String>) taggedFileDoc.getFieldValue(KEY_FILENAME)).get(0) + ", "
				+ ((ArrayList<String>) taggedFileDoc.getFieldValue(KEY_FILEPATH)).get(0) + ", ("
				+ ((ArrayList<Long>) taggedFileDoc.getFieldValue(KEY_FILESIZE)).get(0) + ", ("
				+ ((ArrayList<String>) taggedFileDoc.getFieldValue(KEY_TAGS)).get(0) + ")";
	}

}
