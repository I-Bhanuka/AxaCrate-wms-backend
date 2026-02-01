package com.axacrate.wms.repository;

import com.axacrate.wms.entity.MovementLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Movement Log Repository - Database access for MovementLog entity
 *
 * Provides methods to:
 * - Find movements by tag
 * - Find movements by date range
 * - Find movements by event type
 */
@Repository
public interface MovementLogRepository extends JpaRepository<MovementLog, UUID> {

    /**
     * Find all movements for a specific tag
     * SQL: SELECT * FROM movement_log WHERE tag_id = ?
     *
     * @param tagId - RFID tag ID
     * @return List of movements
     */
    List<MovementLog> findByTagId(UUID tagId);

    /**
     * Find all movements for a tag, ordered by most recent first
     * SQL: SELECT * FROM movement_log WHERE tag_id = ? ORDER BY occurred_at DESC
     *
     * OrderBy keyword adds sorting
     * Desc = descending (newest first)
     *
     * @param tagId - RFID tag ID
     * @return List of movements, newest first
     */
    List<MovementLog> findByTagIdOrderByOccurredAtDesc(UUID tagId);

    /**
     * Find all movements of a specific type
     * SQL: SELECT * FROM movement_log WHERE event_type = ?
     *
     * Example: Find all QC_MOVEMENT events
     *
     * @param eventType - Type of movement
     * @return List of movements
     */
    List<MovementLog> findByEventType(MovementLog.EventType eventType);

    /**
     * Find movements by sync status
     * SQL: SELECT * FROM movement_log WHERE synced = ?
     *
     * Example: Find all unsynced movements (synced = false)
     * Useful for ESP32 offline data sync
     *
     * @param synced - Sync status (true/false)
     * @return List of movements
     */
    List<MovementLog> findBySynced(Boolean synced);

    /**
     * Find movements within a date range
     * SQL: SELECT * FROM movement_log WHERE occurred_at BETWEEN ? AND ?
     *
     * Example: Find all movements in last 24 hours
     *
     * @param start - Start datetime
     * @param end - End datetime
     * @return List of movements
     */
    @Query("SELECT ml FROM MovementLog ml WHERE ml.occurredAt BETWEEN :start AND :end")
    List<MovementLog> findByDateRange(@Param("start") OffsetDateTime start,
                                      @Param("end") OffsetDateTime end);

    /**
     * Find recent movements into a specific zone
     * Useful for zone activity monitoring
     *
     * @param zoneId - Zone ID
     * @return List of movements, newest first
     */
    @Query("SELECT ml FROM MovementLog ml WHERE ml.toZone.id = :zoneId ORDER BY ml.occurredAt DESC")
    List<MovementLog> findRecentMovementsToZone(@Param("zoneId") UUID zoneId);

    /**
     * Find movements by tag EPC (instead of tag ID)
     * Navigates relationship: movement -> tag -> epc
     *
     * @param epc - RFID tag EPC
     * @return List of movements
     */
    @Query("SELECT ml FROM MovementLog ml WHERE ml.tag.epc = :epc ORDER BY ml.occurredAt DESC")
    List<MovementLog> findByTagEpc(@Param("epc") String epc);
}