package com.jbettiol.ewddemo.tagging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.CoreContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jbettiol.ewddemo.util.CustomException;

@Component
public class TaggingServiceImpl implements TaggingService {

	@Value("${solr.homePath}")
	String homePath;

	EmbeddedSolrServer server;
	CoreContainer container;

	public TaggingServiceImpl() {
		init();
	}

	private void init() {
		container = new CoreContainer("testdata/solr");
		container.load();
		server = new EmbeddedSolrServer(container, "collection");
	}

	public void deleteData() {
		try {
			server.deleteByQuery("*:*");
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), e);
		}
	}

	@Override
	public void fileInsert(String dropboxUid, String filename, String filepath, Long filesize, Set<String> tags) {
		try {
			SolrInputDocument newDoc = new SolrInputDocument();
			newDoc.addField(TaggedFile.KEY_DROPBOX_ID, dropboxUid);
			newDoc.addField(TaggedFile.KEY_FILENAME, filename);
			newDoc.addField(TaggedFile.KEY_FILEPATH, filepath);
			newDoc.addField(TaggedFile.KEY_FILESIZE, filesize);
			newDoc.addField(TaggedFile.KEY_TAGS, String.join(" ", tags));
			server.add(newDoc);
			UpdateResponse ur = server.commit();
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), e);
		}
	}

	@Override
	public void fileRemove(String dropboxId) {
		try {
			server.deleteById(dropboxId);
			server.commit();
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), e);
		}
	}

	@Override
	public void tagDel(String dropboxId, String tagToDel) {
		TaggedFile tf = fileLoadByDropboxId(dropboxId);
		if (tf != null) {
			Set<String> tagList = tf.getTags();
			tagList.remove(tagToDel);
			fileInsert(dropboxId, tf.getFilename(), tf.getFilepath(), tf.getFilesize(), tagList);
		}
	}

	@Override
	public void tagAdd(String dropboxId, String tagToAdd) {
		TaggedFile tf = fileLoadByDropboxId(dropboxId);
		if (tf != null) {
			Set<String> tagList = tf.getTags();
			tagList.add(tagToAdd);
			fileInsert(dropboxId, tf.getFilename(), tf.getFilepath(), tf.getFilesize(), tagList);
		}
	}

	String[] addElement(final String[] array, final String element) {
		List<String> myList = new ArrayList<String>(Arrays.asList(array));
		myList.add(element);
		return myList.toArray(new String[0]);
	}

	private String generateTagQuery(String query) {
		return null != query ? "tags" + ":(" + query + ")" : "";
	}

	@Override
	public List<TaggedFile> tagSearch(String tagQuery) {
		return tagSearch(tagQuery, -1, 10000000);
	}

	@Override
	public List<TaggedFile> tagSearch(String tagQuery, int start, int rows) {
		List<TaggedFile> taggedFiles = new ArrayList<TaggedFile>();
		try {
			ModifiableSolrParams params = new ModifiableSolrParams();
			String fullQuery = generateTagQuery(tagQuery);
			params.set("q", fullQuery);
			if (start >= 0) {
				params.set("start", start);
			}
			if (rows >= 1) {
				params.set("rows", rows);
			}
			QueryResponse qResp = server.query(params);

			SolrDocumentList docList = qResp.getResults();

			for (SolrDocument solrDocument : docList) {
				taggedFiles.add(new TaggedFile(solrDocument));
			}
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), e);
		}
		return taggedFiles;
	}

	@Override
	public TaggedFile fileLoadByDropboxId(String dropboxId) {
		try {
			return new TaggedFile(server.getById(dropboxId));
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), e);
		}
	}

}