package com.axacrate.wms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

import lombok.*;

/**
 * DTO for creating a new Zone
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneCreateDTO {
    @NotBlank(message = "Zone name is required")
    private String name;

    @NotBlank(message = "Zone type is required")
    private String zoneType;

    // The warehouse name is selected using a dropdown list in the UI
    @NotBlank(message = "Warehouse must be selected")
    private String warehouseName;

    @NotNull(message = "Capacity is required")
    @Min(value = 0, message = "Capacity cannot be negative")
    private Integer capacity;

    // Zone status is either ACTIVE / INACTIVE
    @NotBlank(message = "Zone status is required")
    private String status;
}
