package com.axacrate.wms.repository;

import com.axacrate.wms.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Inventory Item Repository - Database access for InventoryItem entity
 *
 * Provides methods to:
 * - Find items by SKU
 * - Find items in a zone
 * - Search items by name
 * - Count items
 */
@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {

    /**
     * Find inventory item by SKU (Stock Keeping Unit)
     * SKU is unique, so returns single item
     * SQL: SELECT * FROM inventory_item WHERE sku = ?
     *
     * @param sku - Unique product code
     * @return Optional containing item if found
     */
    Optional<InventoryItem> findBySku(String sku);

    /**
     * Find all items currently in a specific zone
     * SQL: SELECT * FROM inventory_item WHERE current_zone_id = ?
     *
     * @param zoneId - Zone ID
     * @return List of items in that zone
     */
    List<InventoryItem> findByCurrentZoneId(UUID zoneId);

    /**
     * Find all items that are not assigned to any zone
     * SQL: SELECT * FROM inventory_item WHERE current_zone_id IS NULL
     *
     * Useful for finding items that need to be placed in a zone
     *
     * @return List of unassigned items
     */
    List<InventoryItem> findByCurrentZoneIsNull();

    /**
     * Search items by name (partial match, case-insensitive)
     * SQL: SELECT * FROM inventory_item WHERE LOWER(name) LIKE LOWER('%?%')
     *
     * Example: findByNameContainingIgnoreCase("laptop")
     *          finds "Dell Laptop", "LAPTOP BAG", "laptop stand"
     *
     * @param name - Search string
     * @return List of matching items
     */
    List<InventoryItem> findByNameContainingIgnoreCase(String name);

    /**
     * Find all items in a specific warehouse (across all zones)
     * Uses custom query to navigate relationships: item -> zone -> warehouse
     *
     * @param warehouseId - Warehouse ID
     * @return List of items in that warehouse
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.currentZone.warehouse.id = :warehouseId")
    List<InventoryItem> findByWarehouseId(@Param("warehouseId") UUID warehouseId);

    /**
     * Count how many items are in a specific zone
     * SQL: SELECT COUNT(*) FROM inventory_item WHERE current_zone_id = ?
     *
     * Useful for checking zone capacity
     *
     * @param zoneId - Zone ID
     * @return Number of items in zone
     */
    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.currentZone.id = :zoneId")
    Long countItemsInZone(@Param("zoneId") UUID zoneId);
}