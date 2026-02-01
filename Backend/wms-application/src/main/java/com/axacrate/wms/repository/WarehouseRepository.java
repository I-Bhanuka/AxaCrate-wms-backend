package com.axacrate.wms.repository;

import com.axacrate.wms.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Warehouse Repository - Database access for Warehouse entity
 *
 * Provides methods to:
 * - Find warehouses by name
 * - Search warehouses
 * - All standard CRUD operations (inherited from JpaRepository)
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

    /**
     * Find warehouse by exact name
     * SQL: SELECT * FROM warehouse WHERE name = ?
     *
     * @param name - Warehouse name to search for
     * @return Optional containing warehouse if found, empty otherwise
     */
    Optional<Warehouse> findByName(String name);

    /**
     * Find warehouses where name contains the search string (case-insensitive)
     * SQL: SELECT * FROM warehouse WHERE LOWER(name) LIKE LOWER('%?%')
     *
     * Example: findByNameContainingIgnoreCase("main")
     *          finds "Main Warehouse", "main storage", "MAIN DEPOT"
     *
     * @param name - Search string
     * @return List of matching warehouses
     */
    List<Warehouse> findByNameContainingIgnoreCase(String name);
}