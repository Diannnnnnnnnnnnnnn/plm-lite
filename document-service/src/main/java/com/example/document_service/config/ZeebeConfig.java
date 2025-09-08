package com.example.document_service.config;

import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "workflow", name = "engine", havingValue = "zeebe")
public class ZeebeConfig {
  @Bean
  public ZeebeClient zeebeClient(org.springframework.core.env.Environment env) {
    String gateway = env.getProperty("workflow.zeebe.gateway", "127.0.0.1:26500");
    return ZeebeClient.newClientBuilder().gatewayAddress(gateway).usePlaintext().build();
  }
}
