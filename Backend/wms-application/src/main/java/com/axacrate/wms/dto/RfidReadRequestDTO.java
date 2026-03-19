package com.axacrate.wms.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for RFID Read Event from ESP32
 * Sent when ESP32 detects an RFID tag
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RfidReadRequestDTO {

    @NotBlank(message = "Tag UID is required")
    private String tagId;

    @NotBlank(message = "Reader Name is required")
    private String hardwareName;

    @NotBlank(message = "Zone Name is required")
    private String zoneName;
}

