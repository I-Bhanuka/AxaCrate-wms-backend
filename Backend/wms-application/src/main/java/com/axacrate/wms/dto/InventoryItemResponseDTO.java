package com.axacrate.wms.dto;

import lombok.*;

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
    private String rfidTagEpc;
    private String rfidTagStatus;
}