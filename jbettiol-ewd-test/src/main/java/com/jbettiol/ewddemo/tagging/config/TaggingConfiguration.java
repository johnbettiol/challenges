package com.jbettiol.ewddemo.tagging.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.jbettiol.ewddemo.util.Tools;

@Component
@Configuration
public class TaggingConfiguration {

	@Value("${solr.serverPath}")
	String serverPath;

//	@Value("${solr.serverUrl}")
	String serverUrl;

	@Value("${tagging.maxDownloadSize}")
	String maxDownloadSize;

	public String getServerPath() {
		return serverPath;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public String getMaxDownloadSize() {
		return maxDownloadSize;
	}

	public long getMaxDownloadLimit() {
		return Tools.parseFilesize(maxDownloadSize);
	}

}
