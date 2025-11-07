package com.example.api_gateway.filter;

import com.example.api_gateway.dto.UserContext;
import com.example.api_gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * JWT Authentication Filter for API Gateway
 * Validates JWT tokens and adds user context headers
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    // Public paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/auth/",
            "/actuator/health",
            "/eureka/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        // Skip authentication for public paths
        if (isPublicPath(path)) {
            System.out.println("🟢 [Gateway] Public path accessed: " + path);
            return chain.filter(exchange);
        }

        // Extract JWT token from Authorization header
        String token = extractToken(exchange.getRequest());

        if (token == null) {
            System.out.println("🔴 [Gateway] No token provided for: " + path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Validate token
        try {
            if (!jwtUtil.validateToken(token)) {
                System.out.println("🔴 [Gateway] Invalid token for: " + path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Extract user info from token
            String username = jwtUtil.getUsername(token);
            String userId = jwtUtil.getUserId(token);
            List<String> roles = jwtUtil.getRoles(token);

            System.out.println("🟢 [Gateway] Authenticated user: " + username + " for: " + path);

            // Add user context headers for downstream services
            ServerHttpRequest modifiedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-User-Id", userId != null ? userId : "")
                    .header("X-Username", username != null ? username : "")
                    .header("X-User-Roles", roles != null ? String.join(",", roles) : "")
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            System.out.println("🔴 [Gateway] Token validation error: " + e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Check if the path is public and doesn't require authentication
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -100; // Run before other filters
    }
}

