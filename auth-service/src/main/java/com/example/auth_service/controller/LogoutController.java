package com.example.auth_service.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth_service.security.JwtUtil;
import com.example.auth_service.service.TokenBlacklistService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@RestController
@RequestMapping("/api/auth")
public class LogoutController {

    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;

    public LogoutController(TokenBlacklistService tokenBlacklistService, JwtUtil jwtUtil) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid authorization header"));
        }

        String token = authHeader.substring(7);

        // Validate token first
        if (!jwtUtil.validate(token)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired token"));
        }

        // Check if already blacklisted
        if (tokenBlacklistService.isBlacklisted(token)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token already invalidated"));
        }

        // Extract expiration time from token
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtUtil.getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            long expirationTime = claims.getExpiration().getTime();
            
            // Add token to blacklist
            tokenBlacklistService.blacklistToken(token, expirationTime);
            
            return ResponseEntity.ok(Map.of(
                "message", "Logged out successfully",
                "token_expiry", expirationTime
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process logout"));
        }
    }

    @GetMapping("/check-token")
    public ResponseEntity<?> checkToken(@RequestParam("token") String token) {
        boolean valid = jwtUtil.validate(token);
        boolean blacklisted = tokenBlacklistService.isBlacklisted(token);
        
        return ResponseEntity.ok(Map.of(
            "valid", valid,
            "blacklisted", blacklisted,
            "accepted", valid && !blacklisted
        ));
    }
}

