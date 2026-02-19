package com.axacrate.wms.dto;

import com.axacrate.wms.entity.RfidTag;
import com.axacrate.wms.entity.Zone;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

/**
 * DTO for Creating/Updating Inventory Item
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemRequestDTO {

    @NotBlank(message = "SKU is required") // SKU should not be blank and inventoryController will validate it using @Valid
    private String sku;

    @NotBlank(message = "Name is required") // Name should not be blank and inventoryController will validate it using @Valid
    private String name;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @NotNull(message = "RFID Tag is required")
    private String rfidTag; // RFID tag associated with the inventory item

    @NotNull(message = "Zone name is required")
    private String zoneName; // The type of zone where the item will be stored
}