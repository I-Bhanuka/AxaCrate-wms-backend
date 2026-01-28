package com.rfidwms.model.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for RFID Read Event from ESP32
 * Sent when ESP32 detects an RFID tag
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfidReadRequestDTO {

    @NotNull(message = "Hardware ID is required")
    private UUID hardwareId;

    @NotBlank(message = "Tag EPC is required")
    @Size(max = 255)
    private String tagEpc;

    @NotNull(message = "Timestamp is required")
    private OffsetDateTime timestamp;

    private Integer signalStrength; // Optional: RSSI value
}