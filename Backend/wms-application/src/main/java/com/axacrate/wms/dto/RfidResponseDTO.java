package com.rfidwms.model.dto;

import lombok.*;

/**
 * DTO for RFID Response from ESP32
 * Response after write/read operation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfidResponseDTO {
    private Boolean success;
    private String message;
    private String tagEpc;
    private String action; // "ALLOW" or "FLAG"
}
