package com.axacrate.wms.exception;

/**
 * Exception thrown when geofence rules are violated
 */
public class GeofenceViolationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String fromZone;
    private final String toZone;

    public GeofenceViolationException(String message, String fromZone, String toZone) {
        super(message);
        this.fromZone = fromZone;
        this.toZone = toZone;
    }

    public String getFromZone() {
        return fromZone;
    }

    public String getToZone() {
        return toZone;
    }
}
