package com.axacrate.wms.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * InventoryUpdateRequestDTO
 *
 * This DTO is used for updating inventory item details such as name and quantity.
 * It is used in the InventoryController for handling update requests.
 *
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

}
