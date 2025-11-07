package com.example.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway Routes Configuration
 */
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service
                .route("auth-service", r -> r
                        .path("/auth/**")
                        .filters(f -> f.rewritePath("/auth/(?<segment>.*)", "/api/auth/${segment}"))
                        .uri("http://localhost:8110"))
                
                // User Service
                .route("user-service", r -> r
                        .path("/api/users", "/api/users/**")
                        .filters(f -> f.rewritePath("/api/users(?<segment>/.*)?", "/users${segment}"))
                        .uri("lb://user-service"))
                
                // Task Service (direct mapping)
                .route("task-service", r -> r
                        .path("/api/tasks", "/api/tasks/**")
                        .uri("http://localhost:8082"))
                
                // Document Service (needs rewrite from /api/documents to /api/v1/documents)
                .route("document-service", r -> r
                        .path("/api/documents/**")
                        .filters(f -> f.rewritePath("/api/documents(?<segment>/.*)?", "/api/v1/documents${segment}"))
                        .uri("http://localhost:8081"))
                
                // BOM Service (needs rewrite from /api/boms to /api/v1)
                .route("bom-service", r -> r
                        .path("/api/boms/**")
                        .filters(f -> f.rewritePath("/api/boms(?<segment>/.*)?", "/api/v1${segment}"))
                        .uri("http://localhost:8089"))
                
                // Change Service (direct mapping)
                .route("change-service", r -> r
                        .path("/api/changes", "/api/changes/**")
                        .uri("http://localhost:8084"))
                
                // Workflow Orchestrator
                .route("workflow-orchestrator", r -> r
                        .path("/api/workflows/**")
                        .uri("http://localhost:8086"))
                
                // Graph Service
                .route("graph-service", r -> r
                        .path("/api/graph/**")
                        .uri("lb://graph-service"))
                
                // File Storage Service
                .route("file-storage-service", r -> r
                        .path("/api/files", "/api/files/**")
                        .filters(f -> f.rewritePath("/api/files(?<segment>/.*)?", "/files${segment}"))
                        .uri("lb://file-storage-service"))
                
                .build();
    }
}

