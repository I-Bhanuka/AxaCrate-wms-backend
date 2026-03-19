package com.axacrate.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LowStockItemDTO {
    private String sku;
    private String name;
    private Integer quantity;
    private Integer reorderThreshold;
    private String zoneName;
}
