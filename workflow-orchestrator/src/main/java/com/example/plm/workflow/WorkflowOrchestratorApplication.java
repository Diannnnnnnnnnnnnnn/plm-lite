package com.example.plm.workflow;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@EnableFeignClients
@Profile("!dev")
@Deployment(resources = "classpath*:bpmn/*.bpmn")
public class WorkflowOrchestratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkflowOrchestratorApplication.class, args);
	}
}

