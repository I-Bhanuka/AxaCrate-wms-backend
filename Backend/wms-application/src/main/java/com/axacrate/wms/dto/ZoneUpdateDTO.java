package com.axacrate.wms.dto;

import jakarta.validation.constraints.Min;

import lombok.*;

/**
 * DTO for updating an existing Zone
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneUpdateDTO {
    private String name;

    @Min(value = 0, message = "Capacity cannot be negative")
    private Integer capacity;

    // Zone status can be updated in the UI using a dropdown list (ACTIVE / INACTIVE)
    private String status;
}
