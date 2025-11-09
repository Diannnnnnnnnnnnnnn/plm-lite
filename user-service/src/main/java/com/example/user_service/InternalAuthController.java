package com.example.user_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.user_service.dto.LoginRequest;
import com.example.user_service.dto.UserDto;

/**
 * Internal endpoints for auth-service to call
 * These endpoints are not exposed to external clients
 */
@RestController
@RequestMapping("/internal/auth")
public class InternalAuthController {

    @Autowired
    private UserService userService;

    /**
     * Verify user credentials for authentication
     * Called by auth-service during login
     */
    @PostMapping("/verify")
    public ResponseEntity<UserDto> verify(@RequestBody LoginRequest login) {
        try {
            System.out.println("[Auth Verify] Checking credentials for: " + login.getUsername());
            
            User user = userService.findByUsername(login.getUsername());
            
            if (user == null) {
                System.out.println("[Auth Verify] User not found: " + login.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            System.out.println("[Auth Verify] User found, checking password...");
            
            if (!userService.checkPassword(user, login.getPassword())) {
                System.out.println("[Auth Verify] Invalid password for: " + login.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            System.out.println("[Auth Verify] Password valid, creating DTO...");
            
            // Convert to DTO (exclude password)
            UserDto dto = new UserDto(user.getId(), user.getUsername(), user.getRoles());
            
            System.out.println("[Auth Verify] DTO created successfully for: " + login.getUsername());
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            System.err.println("[Auth Verify] ERROR: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}

