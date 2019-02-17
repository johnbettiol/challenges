package com.jbettiol.ewddemo.tagging;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.CoreContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jbettiol.ewddemo.dropbox.DropboxService;
import com.jbettiol.ewddemo.tagging.config.TaggingConfiguration;
import com.jbettiol.ewddemo.util.CustomException;

@Component
public class TaggingServiceImpl implements TaggingService {

	TaggingConfiguration config;

	DropboxService dropbox;

	@Autowired
	public TaggingServiceImpl(TaggingConfiguration config, DropboxService dropbox) {

		this.config = config;
		this.dropbox = dropbox;
		init();
	}

	SolrClient server;
	CoreContainer container;

	private void init() {
		if (config.getServerPath() != null) {
			container = new CoreContainer(config.getServerPath());
			container.load();
			server = new EmbeddedSolrServer(container, "collection");
		} else {
			server = new HttpSolrClient.Builder(config.getServerUrl()).build();
		}
	}

	public void deleteData() {
		try {
			server.deleteByQuery("*:*");
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), e);
		}
	}

	@Override
	public void insertOrUpdate(TaggedFile tf) {
		insertOrUpdateTaggedFile(tf.getDropboxId(), tf.getFilename(), tf.getFilepath(), tf.getFilesize(), tf.getTags());
	}

	@Override
	public void insertOrUpdateTaggedFile(String dropboxUid, String filename, String filepath, Long filesize, Set<String> tags) {
		try {
			System.out.println("i>" + dropboxUid + " tags: " + String.join(", ", tags));
			SolrInputDocument newDoc = new SolrInputDocument();
			newDoc.addField(TaggedFile.KEY_DROPBOX_ID, dropboxUid);
			newDoc.addField(TaggedFile.KEY_FILENAME, filename);
			newDoc.addField(TaggedFile.KEY_FILEPATH, filepath);
			newDoc.addField(TaggedFile.KEY_FILESIZE, filesize);
			newDoc.addField(TaggedFile.KEY_TAGS, String.join(" ", tags));
			server.add(newDoc);
			server.commit();
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), e);
		}
	}

	@Override
	public void fileDelete(String dropboxId) {
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
			insertOrUpdateTaggedFile(dropboxId, tf.getFilename(), tf.getFilepath(), tf.getFilesize(), tagList);
		}
	}

	@Override
	public void tagAdd(String dropboxId, String tagToAdd) {
		TaggedFile tf = fileLoadByDropboxId(dropboxId);
		if (tf != null) {
			Set<String> tagList = tf.getTags();
			tagList.add(tagToAdd);
			insertOrUpdateTaggedFile(dropboxId, tf.getFilename(), tf.getFilepath(), tf.getFilesize(), tagList);
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
			SolrDocument loadedFile = server.getById(dropboxId);
			return loadedFile != null ? new TaggedFile(loadedFile) : null;
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), e);
		}
	}

	@Override
	public void downloadTaggedFiles(List<TaggedFile> filesToDownload, OutputStream os) {
		long filesizeCount = 0L;
		for (TaggedFile taggedFile : filesToDownload) {
			filesizeCount += taggedFile.getFilesize();
			if (filesizeCount > config.getMaxDownloadLimit()) {
				throw new CustomException(
						"Tagged files exceed maximum permitted (" + config.getMaxDownloadSize() + ")");
			}
		}
		try {
			dropbox.downloadTagFilesToZipStream(filesToDownload, os);
		} catch (Exception e) {
			throw new CustomException("Unable to download tagged files: " + e.getMessage(), e);
		}
	}

}