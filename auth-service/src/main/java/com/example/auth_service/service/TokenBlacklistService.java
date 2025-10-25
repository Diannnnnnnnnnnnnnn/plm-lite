package com.example.auth_service.service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Service to manage JWT token blacklist using Redis cache
 * Useful for logout functionality and token revocation
 */
@Service
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;

    public TokenBlacklistService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Add a token to the blacklist
     * @param token The JWT token to blacklist
     * @param expirationTime When the token would normally expire
     */
    public void blacklistToken(String token, long expirationTime) {
        String key = "jwtBlacklist::" + token;
        long ttl = expirationTime - Instant.now().toEpochMilli();
        
        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, expirationTime, ttl, TimeUnit.MILLISECONDS);
            System.out.println("Token blacklisted in Redis: " + key + " (TTL: " + ttl + "ms)");
        }
    }

    /**
     * Check if a token is blacklisted
     * @param token The JWT token to check
     * @return true if token is blacklisted and hasn't expired yet
     */
    public boolean isBlacklisted(String token) {
        String key = "jwtBlacklist::" + token;
        Object value = redisTemplate.opsForValue().get(key);
        
        if (value == null) {
            return false;
        }
        
        // Check if the blacklist entry is still valid
        long expirationTime = ((Number) value).longValue();
        boolean isBlacklisted = Instant.now().toEpochMilli() < expirationTime;
        System.out.println("Token check: " + key + " -> Blacklisted: " + isBlacklisted);
        return isBlacklisted;
    }
}

