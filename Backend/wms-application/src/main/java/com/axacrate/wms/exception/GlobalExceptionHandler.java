package com.axacrate.wms.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 *
 * Catches all exceptions thrown in the application
 * and returns proper HTTP responses
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle Resource Not Found (404)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle Unauthorized Movement (403)
     */
    @ExceptionHandler(UnauthorizedMovementException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedMovement(UnauthorizedMovementException ex) {
        log.warn("Unauthorized movement detected: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Unauthorized Movement")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handle Geofence Violation (400)
     */
    @ExceptionHandler(GeofenceViolationException.class)
    public ResponseEntity<ErrorResponse> handleGeofenceViolation(GeofenceViolationException ex) {
        log.warn("Geofence violation: {} -> {}", ex.getFromZone(), ex.getToZone());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Geofence Violation")
                .message(ex.getMessage())
                .details(Map.of(
                        "fromZone", ex.getFromZone(),
                        "toZone", ex.getToZone()
                ))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle ESP32 Communication Error (503)
     */
    @ExceptionHandler(ESP32CommunicationException.class)
    public ResponseEntity<ErrorResponse> handleESP32Error(ESP32CommunicationException ex) {
        log.error("ESP32 communication error: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("Hardware Communication Error")
                .message("Unable to communicate with RFID hardware: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /**
     * Handle Validation Errors (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed")
                .details(validationErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle Illegal Arguments (400)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle Access Denied (403)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Access Denied")
                .message("You don't have permission to access this resource")
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handle Bad Credentials (401)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Authentication Failed")
                .message("Invalid username or password")
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Handle all other exceptions (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Error Response DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ErrorResponse {
        private OffsetDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private Map<String, ?> details;
    }
}