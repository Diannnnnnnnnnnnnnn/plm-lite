package com.example.auth_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.stereotype.Component;

/**
 * Custom error handler for Redis cache operations
 * Provides graceful degradation when Redis is unavailable
 */
@Component
public class RedisCacheErrorHandler implements CacheErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheErrorHandler.class);

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Failed to get from cache '{}' for key '{}'. Falling back to database. Error: {}", 
                 cache.getName(), key, exception.getMessage());
        // Don't throw exception - let application continue without cache
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn("Failed to put into cache '{}' for key '{}'. Error: {}", 
                 cache.getName(), key, exception.getMessage());
        // Don't throw exception - let application continue without caching
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Failed to evict from cache '{}' for key '{}'. Error: {}", 
                 cache.getName(), key, exception.getMessage());
        // Don't throw exception - stale cache entry will expire naturally
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn("Failed to clear cache '{}'. Error: {}", 
                 cache.getName(), exception.getMessage());
        // Don't throw exception - cache entries will expire naturally
    }
}

