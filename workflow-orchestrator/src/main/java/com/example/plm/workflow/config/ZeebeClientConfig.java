package com.example.plm.workflow.config;

import io.camunda.zeebe.client.ZeebeClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Camunda Zeebe Client Configuration
 * Now uses auto-configured ZeebeClient from spring-boot-starter-camunda
 * This class just deploys BPMN workflows on startup
 */
@Configuration
public class ZeebeClientConfig {

    @Autowired
    private ZeebeClient zeebeClient;

    @Value("${zeebe.client.broker.gateway-address:localhost:26500}")
    private String gatewayAddress;

    /**
     * Deploy BPMN workflows after the ZeebeClient bean is created
     */
    @PostConstruct
    public void init() {
        System.out.println("========================================");
        System.out.println("🔧 Zeebe Client Auto-Configured");
        System.out.println("   Gateway Address: " + gatewayAddress);
        System.out.println("========================================");

        try {
            System.out.println("✅ Zeebe Client ready!");
            System.out.println("   Connected to: " + gatewayAddress);

            // Deploy BPMN workflows
            deployWorkflows(zeebeClient);
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize Zeebe Client: " + e.getMessage());
            e.printStackTrace();
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

            // Deploy document-approval-with-review.bpmn (two-stage review)
            try {
                client.newDeployResourceCommand()
                        .addResourceFromClasspath("bpmn/document-approval-with-review.bpmn")
                        .send()
                        .join();
                System.out.println("   ✓ Deployed: document-approval-with-review.bpmn");
            } catch (Exception e) {
                System.err.println("   ⚠️  Warning: Could not deploy document-approval-with-review.bpmn: " + e.getMessage());
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

