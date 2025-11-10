package com.example.api_gateway.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

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
     * Supports both "uid" (used by auth-service) and "userId" (legacy)
     */
    public String getUserId(String token) {
        Claims claims = parseToken(token);
        // Try "uid" first (as used by auth-service), then fallback to "userId"
        Object uidObj = claims.get("uid");
        if (uidObj != null) {
            return String.valueOf(uidObj);
        }
        Object userIdObj = claims.get("userId");
        if (userIdObj != null) {
            return String.valueOf(userIdObj);
        }
        return null;
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
            if (rolesObj instanceof List<?> list) {
                return (List<String>) list;
            } else if (rolesObj instanceof String str) {
                return Arrays.asList(str);
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



