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
    System.err.println("[GlobalExceptionHandler] Handling exception: " + ex.getClass().getName());
    System.err.println("[GlobalExceptionHandler] Message: " + ex.getMessage());
    if (ex.getCause() != null) {
      System.err.println("[GlobalExceptionHandler] Cause: " + ex.getCause().getClass().getName() + " - " + ex.getCause().getMessage());
    }
    ex.printStackTrace();
    
    // Provide more detailed error message
    String errorMessage = ex.getMessage();
    if (ex.getCause() != null && ex.getCause() instanceof feign.FeignException) {
      feign.FeignException feignEx = (feign.FeignException) ex.getCause();
      errorMessage = "User service unavailable (status: " + feignEx.status() + "): " + feignEx.getMessage();
    }
    
    ApiError err = new ApiError(500, "Internal Server Error", errorMessage, req.getRequestURI());
    return ResponseEntity.status(500).body(err);
  }
}
