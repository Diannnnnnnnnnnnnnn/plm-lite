package com.example.plm.workflow.config;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.impl.ZeebeClientBuilderImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Camunda Zeebe Client Configuration
 * Creates and configures the ZeebeClient bean for workflow operations
 */
@Configuration
public class ZeebeClientConfig {

    @Value("${zeebe.client.broker.gateway-address:localhost:26500}")
    private String gatewayAddress;

    @Value("${zeebe.client.worker.max-jobs-active:32}")
    private int maxJobsActive;

    @Value("${zeebe.client.worker.threads:3}")
    private int workerThreads;

    @Value("${zeebe.client.job.timeout:30000}")
    private long jobTimeout;

    /**
     * Creates the ZeebeClient bean
     * This client is used to:
     * - Start workflow instances
     * - Complete user tasks
     * - Cancel process instances
     * - Deploy BPMN processes
     */
    @Bean
    public ZeebeClient zeebeClient() {
        System.out.println("========================================");
        System.out.println("🔧 Configuring Zeebe Client");
        System.out.println("   Gateway Address: " + gatewayAddress);
        System.out.println("   Max Jobs Active: " + maxJobsActive);
        System.out.println("   Worker Threads: " + workerThreads);
        System.out.println("   Job Timeout: " + jobTimeout + "ms");
        System.out.println("========================================");

        try {
            ZeebeClient client = ZeebeClient.newClientBuilder()
                    .gatewayAddress(gatewayAddress)
                    .usePlaintext()
                    .defaultJobTimeout(Duration.ofMillis(jobTimeout))
                    .defaultJobWorkerMaxJobsActive(maxJobsActive)
                    .numJobWorkerExecutionThreads(workerThreads)
                    .build();

            System.out.println("✅ Zeebe Client created successfully!");
            System.out.println("   Connected to: " + gatewayAddress);

            // Deploy BPMN workflows
            deployWorkflows(client);

            return client;
        } catch (Exception e) {
            System.err.println("❌ Failed to create Zeebe Client: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to Zeebe", e);
        }
    }

    /**
     * Deploy BPMN workflows to Zeebe
     */
    private void deployWorkflows(ZeebeClient client) {
        try {
            System.out.println("\n📦 Deploying BPMN workflows...");

            // Deploy document-approval.bpmn
            try {
                client.newDeployResourceCommand()
                        .addResourceFromClasspath("bpmn/document-approval.bpmn")
                        .send()
                        .join();
                System.out.println("   ✓ Deployed: document-approval.bpmn");
            } catch (Exception e) {
                System.err.println("   ⚠️  Warning: Could not deploy document-approval.bpmn: " + e.getMessage());
            }

            // Deploy change-approval.bpmn
            try {
                client.newDeployResourceCommand()
                        .addResourceFromClasspath("bpmn/change-approval.bpmn")
                        .send()
                        .join();
                System.out.println("   ✓ Deployed: change-approval.bpmn");
            } catch (Exception e) {
                System.err.println("   ⚠️  Warning: Could not deploy change-approval.bpmn: " + e.getMessage());
            }

            System.out.println("✅ BPMN workflows deployed successfully!\n");

        } catch (Exception e) {
            System.err.println("❌ Failed to deploy workflows: " + e.getMessage());
            // Don't fail startup if deployment fails
        }
    }
}

