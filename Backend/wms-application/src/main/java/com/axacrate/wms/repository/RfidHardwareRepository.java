package com.axacrate.wms.repository;

import com.axacrate.wms.entity.RfidHardware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * RFID Hardware Repository - Database access for RfidHardware entity
 *
 * Provides methods to:
 * - Find hardware by zone
 * - Find hardware by type (READER/WRITER)
 * - Find hardware by status
 */
@Repository
public interface RfidHardwareRepository extends JpaRepository<RfidHardware, UUID> {

    /**
     * Find hardware assigned to a specific zone
     * Due to 1:1 relationship, returns Optional (single hardware)
     * SQL: SELECT * FROM rfid_hardware WHERE zone_location = ?
     *
     * @param zoneId - Zone ID
     * @return Optional containing hardware if found
     */
    Optional<RfidHardware> findByZoneLocationId(UUID zoneId);

    /**
     * Find all hardware of a specific type
     * SQL: SELECT * FROM rfid_hardware WHERE hardware_type = ?
     *
     * Example: Find all READERs or all WRITERs
     *
     * @param hardwareType - Type (READER or WRITER)
     * @return List of hardware
     */
    List<RfidHardware> findByHardwareType(RfidHardware.HardwareType hardwareType);

    /**
     * Find all hardware with a specific status
     * SQL: SELECT * FROM rfid_hardware WHERE hardware_status = ?
     *
     * Example: Find all ACTIVE, INACTIVE, or FAULTY hardware
     *
     * @param status - Hardware status
     * @return List of hardware
     */
    List<RfidHardware> findByHardwareStatus(RfidHardware.HardwareStatus status);

    /**
     * Find hardware in a specific zone with a specific type
     * Example: Find the READER in QC zone
     *
     * @param zoneId - Zone ID
     * @param type - Hardware type
     * @return Optional containing hardware if found
     */
    @Query("SELECT rh FROM RfidHardware rh WHERE rh.zoneLocation.id = :zoneId AND rh.hardwareType = :type")
    Optional<RfidHardware> findByZoneAndType(@Param("zoneId") UUID zoneId,
                                             @Param("type") RfidHardware.HardwareType type);

    /**
     * Find all active hardware
     * Useful for health monitoring
     *
     * @return List of active hardware
     */
    @Query("SELECT rh FROM RfidHardware rh WHERE rh.hardwareStatus = 'ACTIVE'")
    List<RfidHardware> findAllActiveHardware();

    /**
     * Find hardware that is not assigned to any zone
     * SQL: SELECT * FROM rfid_hardware WHERE zone_location IS NULL
     *
     * @return List of unassigned hardware
     */
    @Query("SELECT rh FROM RfidHardware rh WHERE rh.zoneLocation IS NULL")
    List<RfidHardware> findUnassignedHardware();
}