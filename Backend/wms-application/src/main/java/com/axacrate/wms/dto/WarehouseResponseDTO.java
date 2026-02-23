package com.axacrate.wms.dto;

import lombok.*;

import java.util.UUID;

/**
 * DTO for Warehouse Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseResponseDTO {
    private UUID id;
    private String name;
}