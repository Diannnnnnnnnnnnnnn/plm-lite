package com.example.api_gateway.filter;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.example.api_gateway.util.JwtUtil;

import reactor.core.publisher.Mono;

/**
 * JWT Authentication Filter for API Gateway
 * Validates JWT tokens and adds user context headers
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    // Public paths that don't require authentication
    // Any path starting with these prefixes will bypass JWT validation
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/auth/",
            "/actuator/health",
            "/eureka/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value(); // Use value() instead of toString()
        String method = exchange.getRequest().getMethod().toString();

        System.out.println("🔵 [Gateway Filter] Checking path: '" + path + "' method: " + method);

        // Allow CORS preflight requests (OPTIONS) to pass through without authentication
        if ("OPTIONS".equals(method)) {
            System.out.println("🟢 [Gateway] CORS preflight request for: " + path);
            return chain.filter(exchange);
        }

        // Defensive bypass: Always skip JWT check for auth endpoints (login, register, refresh)
        // This ensures auth endpoints are never blocked, even if path matching fails
        if (path != null && (path.contains("/auth/login") || path.contains("/auth/register") || path.contains("/auth/refresh"))) {
            System.out.println("🟢 [Gateway] Skipping JWT check for auth endpoint: " + path);
            return chain.filter(exchange);
        }

        // Skip authentication for public paths
        if (isPublicPath(path)) {
            System.out.println("🟢 [Gateway] Public path accessed: " + path);
            return chain.filter(exchange);
        }
        
        System.out.println("🔴 [Gateway] Path NOT public, checking token: " + path);

        // Extract JWT token from Authorization header
        String token = extractToken(exchange.getRequest());

        if (token == null || token.trim().isEmpty()) {
            System.out.println("🔴 [Gateway] No token provided for: " + path);
            System.out.println("🔴 [Gateway] Request headers: " + exchange.getRequest().getHeaders().getFirst("Authorization"));
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().add("WWW-Authenticate", "Bearer");
            return exchange.getResponse().setComplete();
        }
        
        System.out.println("🔵 [Gateway] Token found (length: " + token.length() + "), validating...");

        // Validate token
        try {
            boolean isValid = jwtUtil.validateToken(token);
            if (!isValid) {
                System.out.println("🔴 [Gateway] Invalid token for: " + path);
                System.out.println("🔴 [Gateway] Token (first 50 chars): " + (token.length() > 50 ? token.substring(0, 50) + "..." : token));
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                exchange.getResponse().getHeaders().add("WWW-Authenticate", "Bearer");
                return exchange.getResponse().setComplete();
            }

            System.out.println("🟢 [Gateway] Token is valid, extracting user info...");

            // Extract user info from token
            String username = null;
            String userId = null;
            List<String> roles = null;
            
            try {
                username = jwtUtil.getUsername(token);
                userId = jwtUtil.getUserId(token);
                roles = jwtUtil.getRoles(token);
            } catch (Exception e) {
                System.out.println("⚠️ [Gateway] Error extracting user info (but token is valid): " + e.getMessage());
                // Continue with null values - token is still valid
            }

            System.out.println("🟢 [Gateway] Authenticated user: " + username + " (ID: " + userId + ", Roles: " + roles + ") for: " + path);

            // Add user context headers for downstream services
            String rolesHeader = "";
            if (roles != null && !roles.isEmpty()) {
                rolesHeader = String.join(",", roles);
            }
            
            ServerHttpRequest modifiedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-User-Id", userId != null ? userId : "")
                    .header("X-Username", username != null ? username : "")
                    .header("X-User-Roles", rolesHeader)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            System.out.println("🔴 [Gateway] Token validation error: " + e.getMessage());
            System.out.println("🔴 [Gateway] Exception type: " + e.getClass().getName());
            e.printStackTrace();
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().add("WWW-Authenticate", "Bearer");
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
     * Uses simple startsWith check - any path starting with a public path prefix is public
     */
    private boolean isPublicPath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        // Normalize path - remove query parameters
        String normalizedPath = path.split("\\?")[0];
        // Simple startsWith check - more reliable than complex matching
        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(normalizedPath::startsWith);
        System.out.println("🔍 [Gateway] Path check: '" + normalizedPath + "' -> " + (isPublic ? "PUBLIC" : "PROTECTED"));
        return isPublic;
    }

    @Override
    public int getOrder() {
        return -200; // Run very early, before Spring Security filters
    }
}



