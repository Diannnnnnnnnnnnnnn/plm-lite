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
        User user = userService.findByUsername(login.getUsername());

        if (user != null && userService.checkPassword(user, login.getPassword())) {
            // Convert to DTO (exclude password)
            UserDto dto = new UserDto(user.getId(), user.getUsername(), user.getRoles());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}

