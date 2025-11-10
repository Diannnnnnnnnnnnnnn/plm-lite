package com.example.plm.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.plm.workflow.dto.UserResponse;

/**
 * Feign Client for User Service
 */
@FeignClient(name = "user-service", url = "${user-service.base-url:http://localhost:8083}")
public interface UserServiceClient {
    
    @GetMapping("/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long id);
    
    @GetMapping("/users/by-username/{username}")
    UserResponse getUserByUsername(@PathVariable("username") String username);
}

