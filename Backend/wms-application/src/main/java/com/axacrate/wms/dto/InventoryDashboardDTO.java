package com.axacrate.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Inventory Dashboard DTO
 *
 * This DTO is used to return aggregated inventory data for the dashboard view.
 * It includes:
 * - Total number of items in inventory
 * - Number of items in each zone
 * - List of low stock items (below threshold)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDashboardDTO {
    private long totalItems; // Total number of items in inventory
    private long totaQuantity; // Total quantity of all items in inventory
    private long lowStockCount; // Number of items below low stock threshold
    private Map<String, Long> itemsByZone; // Map of zone name to number of items in that zone
    private List<InventoryItemResponseDTO> recentItems; // List of recently added items

}
