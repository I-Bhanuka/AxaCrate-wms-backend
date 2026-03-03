package com.axacrate.wms.exception;
import java.io.Serial;
import java.util.UUID;
import lombok.Getter;
/**
 * Exception thrown when geofence rules are violated
 */
@Getter
public class GeofenceViolationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID tagId;
    private final String fromZone;
    private final String toZone;

    public GeofenceViolationException(String message,UUID tagId, String fromZone, String toZone) {
        super(message);
        this.tagId = tagId;
        this.fromZone = fromZone;
        this.toZone = toZone;
    }

}