package com.axacrate.wms.repository;

import com.axacrate.wms.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, UUID> {

    // Get total quantity of all items
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM InventoryItem i")
    Long sumAllQuantities();

    // Get low stock items
    @Query("""
        SELECT i
        FROM InventoryItem i
        WHERE i.quantity <= i.reorderThreshold
        ORDER BY i.quantity ASC
    """)
    List<InventoryItem> findLowStockItems();
}