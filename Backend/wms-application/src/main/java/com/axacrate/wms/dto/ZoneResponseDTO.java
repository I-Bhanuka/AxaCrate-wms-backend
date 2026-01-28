package com.rfidwms.model.dto;

import lombok.*;

import java.util.UUID;

/**
 * DTO for Zone Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneResponseDTO {
    private UUID id;
    private String name;
    private String zoneType;
    private Integer capacity;
    private Integer currentItemCount;
    private String warehouseName;
}