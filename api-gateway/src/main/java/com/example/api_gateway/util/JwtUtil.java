package com.example.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * JWT Utility for parsing and validating JWT tokens
 */
@Component
public class JwtUtil {

    private final SecretKey key;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Parse JWT token and extract claims
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extract username from token
     */
    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * Extract user ID from token
     */
    public String getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", String.class);
    }

    /**
     * Extract roles from token
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        try {
            Claims claims = parseToken(token);
            Object rolesObj = claims.get("roles");
            if (rolesObj == null) {
                // Try alternative claim names
                rolesObj = claims.get("role");
            }
            if (rolesObj instanceof List) {
                return (List<String>) rolesObj;
            } else if (rolesObj instanceof String) {
                return Arrays.asList((String) rolesObj);
            }
            return null;
        } catch (Exception e) {
            System.out.println("⚠️ [JwtUtil] Error extracting roles: " + e.getMessage());
            return null;
        }
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}



