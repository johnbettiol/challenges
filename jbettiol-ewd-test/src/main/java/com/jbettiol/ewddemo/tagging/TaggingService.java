package com.jbettiol.ewddemo.tagging;

import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public interface TaggingService {

	public void deleteData();

	public void insertOrUpdate(TaggedFile tf);
	
	public void insertOrUpdate(String dropboxUid, String name, String path, Long filesize, Set<String> tags);

	public void fileDelete(String dropboxId);
	
	public void tagDel(String dropboxId, String tagToDel);

	public void tagAdd(String dropboxId, String tagToAdd);

	public TaggedFile fileLoadByDropboxId(String dropboxId);

	List<TaggedFile> tagSearch(String tagQuery);

	List<TaggedFile> tagSearch(String tagQuery, Integer start, Integer rows);

	void downloadTaggedFiles(List<TaggedFile> filesToDownload, OutputStream os);

	
}