package com.example.user_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;  // Confirm Spring is using Redis (not fallback in-memory cache)
import org.springframework.cache.annotation.EnableCaching; // Confirm Spring is using Redis (not fallback in-memory cache)
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;  // Confirm Spring is using Redis (not fallback in-memory cache)


@SpringBootApplication
@EnableCaching
@EnableFeignClients(basePackages = "com.example.user_service.client")

public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
	
	@Bean  // Confirm Spring is using Redis (not fallback in-memory cache)
	public CommandLineRunner checkCacheManager(CacheManager cacheManager) {
    return args -> System.out.println("➡️ CacheManager: " + cacheManager.getClass());
    }

}

