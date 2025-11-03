package com.example.change_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class DatabaseConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUri;

    @Value("${spring.elasticsearch.username:}")
    private String elasticsearchUsername;

    @Value("${spring.elasticsearch.password:}")
    private String elasticsearchPassword;

    @Override
    public ClientConfiguration clientConfiguration() {
        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder = ClientConfiguration.builder()
            .connectedTo(elasticsearchUri.replace("http://", "").replace("https://", ""));

        if (!elasticsearchUsername.isEmpty() && !elasticsearchPassword.isEmpty()) {
            builder.withBasicAuth(elasticsearchUsername, elasticsearchPassword);
        }

        return builder.build();
    }
}

