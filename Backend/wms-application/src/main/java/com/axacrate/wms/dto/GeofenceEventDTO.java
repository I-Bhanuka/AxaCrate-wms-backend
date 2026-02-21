package com.axacrate.wms.dto;
/**
 * DTO for Geofence Requests
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class GeofenceEventDTO {
    private UUID id;
    private String readerName;
    private String direction;
    private LocalDateTime timestamp;
}






