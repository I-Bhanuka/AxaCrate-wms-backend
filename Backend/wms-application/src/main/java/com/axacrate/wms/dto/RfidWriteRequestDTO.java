package com.axacrate.wms.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

/**
 * DTO for RFID Write Request to ESP32
 * Sent when UI requests tag writing
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfidWriteRequestDTO {

    @NotBlank(message = "Tag EPC is required")
    @Size(max = 255)
    private String tagEpc;

    @NotNull(message = "Inventory Item ID is required")
    private UUID inventoryItemId;

    private String additionalData; // Any extra data to write on tag
}
