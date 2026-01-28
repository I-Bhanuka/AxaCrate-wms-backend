package com.rfidwms.model.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;


/**
 * DTO for Movement Log Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovementLogResponseDTO {
    private UUID id;
    private String tagEpc;
    private String fromZoneName;
    private String toZoneName;
    private String eventType;
    private OffsetDateTime occurredAt;
    private String hardwareType;
    private Boolean synced;
    private String inventoryItemSku;
}