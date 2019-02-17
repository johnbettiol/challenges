package com.jbettiol.ewddemo.dropbox.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

// @Configuration
@Component
class DropboxConfiguration {

    @Value("${dropbox.accessToken}")
    private String accessToken;

    @Bean
    public DbxClientV2 dropboxClient() {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("example-app").build();
        return new DbxClientV2(config, accessToken);
    }

}
