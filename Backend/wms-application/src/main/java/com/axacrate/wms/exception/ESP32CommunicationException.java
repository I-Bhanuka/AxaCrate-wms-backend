package com.axacrate.wms.exception;

/**
 * Exception thrown when ESP32 communication fails
 */
public class ESP32CommunicationException extends RuntimeException {
    public ESP32CommunicationException(String message) {
        super(message);
    }

    public ESP32CommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}