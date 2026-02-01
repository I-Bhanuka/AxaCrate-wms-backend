package com.axacrate.wms.repository;

import com.axacrate.wms.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Zone Repository - Database access for Zone entity
 *
 * Provides methods to:
 * - Find zones by warehouse
 * - Find zones by type (QC, STORAGE, etc.)
 * - Search zones
 */
@Repository
public interface ZoneRepository extends JpaRepository<Zone, UUID> {

    /**
     * Find all zones in a specific warehouse
     * SQL: SELECT * FROM zone WHERE warehouse_id = ?
     *
     * @param warehouseId - ID of the warehouse
     * @return List of zones in that warehouse
     */
    List<Zone> findByWarehouseId(UUID warehouseId);

    /**
     * Find a specific zone by name within a warehouse
     * SQL: SELECT * FROM zone WHERE name = ? AND warehouse_id = ?
     *
     * Useful to check if zone name already exists in warehouse
     *
     * @param name - Zone name
     * @param warehouseId - Warehouse ID
     * @return Optional containing zone if found
     */
    Optional<Zone> findByNameAndWarehouseId(String name, UUID warehouseId);

    /**
     * Find all zones of a specific type (e.g., all QC zones)
     * SQL: SELECT * FROM zone WHERE zone_type = ?
     *
     * @param zoneType - Type of zone (QC_ZONE, STORAGE_ZONE, etc.)
     * @return List of zones of that type
     */
    List<Zone> findByZoneType(Zone.ZoneType zoneType);

    /**
     * Find a zone by warehouse and type using custom query
     * Example: Find the QC zone in warehouse X
     *
     * Uses JPQL (Java Persistence Query Language)
     *
     * @param warehouseId - Warehouse ID
     * @param zoneType - Zone type
     * @return Optional containing the zone if found
     */
    @Query("SELECT z FROM Zone z WHERE z.warehouse.id = :warehouseId AND z.zoneType = :zoneType")
    Optional<Zone> findByWarehouseAndType(@Param("warehouseId") UUID warehouseId,
                                          @Param("zoneType") Zone.ZoneType zoneType);
}