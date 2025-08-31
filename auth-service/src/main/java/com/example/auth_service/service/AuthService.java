package com.example.auth_service.service;

import com.example.auth_service.dto.JwtResponse;
import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.RegisterRequest;
import com.example.auth_service.dto.UserDto;
import com.example.auth_service.security.JwtUtil;
import com.example.auth_service.userclient.UserClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

  private final UserClient userClient;
  private final JwtUtil jwtUtil;

  public AuthService(UserClient userClient, JwtUtil jwtUtil) {
    this.userClient = userClient;
    this.jwtUtil = jwtUtil;
  }

  public JwtResponse login(LoginRequest login) {
    UserDto user = userClient.verify(login); // 401 thrown by Feign if invalid
    Map<String, Object> claims = new HashMap<>();
    claims.put("uid", user.getId());
    claims.put("role", user.getRole());
    String token = jwtUtil.generate(user.getUsername(), claims);
    return new JwtResponse(token);
  }

  public void register(RegisterRequest req) {
    // Typically: call user-service to register; left unimplemented
    throw new UnsupportedOperationException("Registration is handled by user-service");
  }

  public boolean validate(String token) {
    return jwtUtil.validate(token);
  }
}
