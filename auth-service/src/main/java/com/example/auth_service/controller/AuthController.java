package com.example.auth_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth_service.dto.JwtResponse;
import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.RegisterRequest;
import com.example.auth_service.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
    System.out.println("[AuthController] Login request received for username: " + request.getUsername());
    try {
      JwtResponse response = authService.login(request);
      System.out.println("[AuthController] Login successful for username: " + request.getUsername());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      System.err.println("[AuthController] Login failed for username: " + request.getUsername() + ", error: " + e.getMessage());
      e.printStackTrace();
      throw e;
    }
  }

  @PostMapping("/register")
  public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(501).body("Registration is handled by user-service");
  }

  @GetMapping("/validate")
  public ResponseEntity<Boolean> validate(@RequestParam("token") String token) {
    return ResponseEntity.ok(authService.validate(token));
  }
}
