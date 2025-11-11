package com.example.plm.workflow.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration to provide a simple RestTemplate
 * to avoid circular dependency issues with Eureka client initialization.
 */
@Configuration
public class HttpClientConfig {

    /**
     * Provide a simple RestTemplate with basic configuration
     * that doesn't cause circular dependencies.
     */
    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        return new RestTemplate(factory);
    }
}

