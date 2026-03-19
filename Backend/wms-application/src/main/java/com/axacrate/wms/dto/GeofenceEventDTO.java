package com.axacrate.wms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeofenceEventDTO {

    @NotNull(message = "Hardware ID is required")
    private UUID hardwareId;

    @NotBlank(message = "Tag UID is required")
    private String tagUid;

    @NotBlank(message = "Direction is required")
    private String direction; // "IN" or "OUT"

    private String fromZone;

    private String toZone;

    private String readerName;

    private LocalDateTime timestamp;
}