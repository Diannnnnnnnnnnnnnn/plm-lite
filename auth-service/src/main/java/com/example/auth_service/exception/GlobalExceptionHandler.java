package com.example.auth_service.exception;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(FeignException.Unauthorized.class)
  public ResponseEntity<ApiError> handleUnauthorized(FeignException ex, HttpServletRequest req) {
    ApiError err = new ApiError(401, "Unauthorized", "Invalid credentials", req.getRequestURI());
    return ResponseEntity.status(401).body(err);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
    ApiError err = new ApiError(500, "Internal Server Error", ex.getMessage(), req.getRequestURI());
    return ResponseEntity.status(500).body(err);
  }
}
