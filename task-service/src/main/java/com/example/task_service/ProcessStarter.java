package com.example.task_service;

import java.util.HashMap;
import java.util.Map;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

public class ProcessStarter {
    public static void main(String[] args) {

        try (ZeebeClient client = ZeebeClient.newClientBuilder()
                .gatewayAddress("localhost:26500")
                .usePlaintext()
                .build()) {

            Map<String, Object> variables = new HashMap<>();
            variables.put("name", "Test task");
            variables.put("description", "This is a sample");
            variables.put("userId", 4);

            ProcessInstanceEvent event = client.newCreateInstanceCommand()
                    .bpmnProcessId("Process_1uuukhn")
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();

            System.out.println("Started process instance with key: " + event.getProcessInstanceKey());
        }
    }
}
