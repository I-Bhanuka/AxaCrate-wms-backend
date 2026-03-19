package com.axacrate.wms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * InventoryUpdateResponseDTO
 *
 * This DTO is used for sending responses after updating inventory item details such as name and quantity.
 * It can include additional fields like updatedAt, status, etc. if needed in the future.
 *
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateResponseDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

}
