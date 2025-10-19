package com.example.plm.workflow.client;

import com.example.plm.workflow.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign Client for User Service
 */
@FeignClient(name = "user-service", url = "http://localhost:8083")
public interface UserServiceClient {
    
    @GetMapping("/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long id);
    
    @GetMapping("/users/by-username/{username}")
    UserResponse getUserByUsername(@PathVariable("username") String username);
}

