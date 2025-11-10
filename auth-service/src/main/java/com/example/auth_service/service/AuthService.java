package com.example.auth_service.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.example.auth_service.dto.JwtResponse;
import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.RegisterRequest;
import com.example.auth_service.dto.UserDto;
import com.example.auth_service.security.JwtUtil;
import com.example.auth_service.userclient.UserClient;

@Service
public class AuthService {

  private final UserClient userClient;
  private final JwtUtil jwtUtil;
  private final CacheManager cacheManager;

  public AuthService(UserClient userClient, JwtUtil jwtUtil, CacheManager cacheManager) {
    this.userClient = userClient;
    this.jwtUtil = jwtUtil;
    this.cacheManager = cacheManager;
  }

  public JwtResponse login(LoginRequest login) {
    System.out.println("[AuthService] Login attempt for username: " + login.getUsername());
    // Clear user cache before verification to ensure fresh data
    if (cacheManager != null) {
      var usersCache = cacheManager.getCache("users");
      if (usersCache != null) {
        usersCache.evict("username:" + login.getUsername());
        System.out.println("[AuthService] Cleared cache for username: " + login.getUsername());
      }
    }
    try {
      System.out.println("[AuthService] Calling user service verify with username: " + login.getUsername() + ", password length: " + (login.getPassword() != null ? login.getPassword().length() : 0));
      UserDto user = userClient.verify(login); // 401 thrown by Feign if invalid
      System.out.println("[AuthService] User verified successfully: " + user.getUsername());
      
      // Build JWT claims
      Map<String, Object> claims = new HashMap<>();
      claims.put("uid", user.getId());
      claims.put("username", user.getUsername());
      
      // Add roles
      if (user.getRoles() != null && !user.getRoles().isEmpty()) {
        claims.put("roles", user.getRoles());
        claims.put("role", user.getRoles().get(0)); // First role for backward compatibility
      }
      
      // Generate token with username as subject
      String token = jwtUtil.generate(user.getUsername(), claims);
      System.out.println("[AuthService] Token generated successfully for: " + user.getUsername());
      return new JwtResponse(token);
    } catch (feign.FeignException.Unauthorized e) {
      System.err.println("[AuthService] Unauthorized error from user service: " + e.getMessage());
      System.err.println("[AuthService] Response body: " + e.contentUTF8());
      throw e; // Re-throw to be handled by GlobalExceptionHandler
    } catch (feign.FeignException e) {
      System.err.println("[AuthService] Feign error: " + e.status() + " - " + e.getMessage());
      System.err.println("[AuthService] Response body: " + e.contentUTF8());
      // Wrap FeignException to provide better error message
      throw new RuntimeException("Failed to connect to user service: " + e.getMessage(), e);
    } catch (Exception e) {
      System.err.println("[AuthService] Unexpected error during login: " + e.getMessage());
      System.err.println("[AuthService] Exception type: " + e.getClass().getName());
      e.printStackTrace();
      // Wrap to ensure proper error handling
      throw new RuntimeException("Login failed: " + e.getMessage(), e);
    }
  }

  public void register(RegisterRequest req) {
    // Typically: call user-service to register; left unimplemented
    throw new UnsupportedOperationException("Registration is handled by user-service");
  }

  public boolean validate(String token) {
    return jwtUtil.validate(token);
  }
}
