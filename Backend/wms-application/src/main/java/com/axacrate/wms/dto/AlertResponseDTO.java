package com.rfidwms.model.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for Alert Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertResponseDTO {
    private UUID id;
    private String alertType;
    private String severity;
    private String alertStatus;
    private String message;
    private OffsetDateTime createdAt;
    private OffsetDateTime resolvedAt;
    private String resolvedByUsername;
}