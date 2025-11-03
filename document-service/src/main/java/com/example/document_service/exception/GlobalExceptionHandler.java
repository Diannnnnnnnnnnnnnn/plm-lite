package com.example.document_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(NotFoundException ex, WebRequest request) {
        ApiError error = new ApiError(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DocumentServiceException.class)
    public ResponseEntity<?> handleDocumentServiceException(DocumentServiceException ex, WebRequest request) {
        ApiError error = new ApiError(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Service Error",
            ex.getMessage(),
            request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidationException(ValidationException ex, WebRequest request) {
        ApiError error = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            ex.getMessage(),
            request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request) {
        // Suppress verbose logging for known legacy workflow endpoint calls
        if (ex.getMessage() != null && ex.getMessage().contains("api/documents")) {
            logger.debug("Legacy workflow endpoint call (suppressed): {}", ex.getMessage());
        } else {
            logger.warn("Resource not found: {}", ex.getMessage());
        }
        ApiError error = new ApiError(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            "The requested resource was not found",
            request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Unexpected error occurred: ", ex);
        ApiError error = new ApiError(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "System Error",
            ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred",
            request.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
