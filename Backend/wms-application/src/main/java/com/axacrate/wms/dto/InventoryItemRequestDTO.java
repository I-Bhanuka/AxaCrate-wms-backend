package com.rfidwms.model.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

/**
 * DTO for Creating/Updating Inventory Item
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemRequestDTO {

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    private UUID currentZoneId; // Optional initially
}