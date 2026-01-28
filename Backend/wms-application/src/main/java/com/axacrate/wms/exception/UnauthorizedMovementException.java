package com.rfidwms.exception;

/**
 * Exception thrown when movement is unauthorized (geofencing violation)
 */
public class UnauthorizedMovementException extends RuntimeException {
    public UnauthorizedMovementException(String message) {
        super(message);
    }
}