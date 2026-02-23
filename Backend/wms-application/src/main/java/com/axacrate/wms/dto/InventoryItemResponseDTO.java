package com.axacrate.wms.dto;

import com.axacrate.wms.entity.RfidTag;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Inventory Item Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemResponseDTO {
    private UUID id;
    private String sku;
    private String name;
    private Integer quantity;
    private String currentZoneName;
    private UUID currentZoneId;
    private String rfidTagUid;
    private RfidTag.RfidStatus rfidTagStatus;
    private LocalDateTime createdAt;
}