package com.axacrate.wms.dto;

import com.axacrate.wms.entity.RfidHardware;
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
    private String itemName;
    private String fromZoneName;
    private String toZoneName;
    private String eventType;
    private OffsetDateTime occurredAt;
    private RfidHardware.HardwareType hardwareType;
    private Boolean synced;
    private String itemSku;
}