package com.axacrate.wms.repository;

import com.axacrate.wms.entity.RfidTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * RFID Tag Repository - Database access for RfidTag entity
 *
 * Provides methods to:
 * - Find tags by EPC (RFID identifier)
 * - Find tags by status
 * - Find stale tags (not seen recently)
 */
@Repository
public interface RfidTagRepository extends JpaRepository<RfidTag, UUID> {

    /**
     * Find tag by UID (Unique Identifier)
     * UID is unique identifier on the RFID tag
     * SQL: SELECT * FROM rfid_tag WHERE uid = ?
     *
     * @param uid - RFID tag identifier
     * @return Optional containing tag if found
     */
    Optional<RfidTag> findByUid(String uid);

    /**
     * Find all tags with a specific status
     * SQL: SELECT * FROM rfid_tag WHERE status = ?
     *
     * Example: Find all ACTIVE tags, or all LOST tags
     *
     * @param status - Tag status (ACTIVE, INACTIVE, LOST)
     * @return List of tags with that status
     */
    List<RfidTag> findByStatus(RfidTag.RfidStatus status);

    /**
     * Find tag assigned to a specific inventory item
     * SQL: SELECT * FROM rfid_tag WHERE inventory_item_id = ?
     *
     * @param inventoryItemId - Inventory item ID
     * @return Optional containing tag if found
     */
    Optional<RfidTag> findByInventoryItemId(UUID inventoryItemId);

    /**
     * Find all tags last seen in a specific zone
     * SQL: SELECT * FROM rfid_tag WHERE last_seen_zone_id = ?
     *
     * @param zoneId - Zone ID
     * @return List of tags
     */
    List<RfidTag> findByLastSeenZoneId(UUID zoneId);

    /**
     * Find "stale" tags that haven't been seen recently
     * Useful for identifying lost or inactive tags
     *
     * Example: Find tags not seen in last 24 hours
     *
     * @param threshold - DateTime threshold (e.g., 24 hours ago)
     * @return List of stale tags
     */
    @Query("SELECT rt FROM RfidTag rt WHERE rt.status = 'ACTIVE' AND rt.lastSeenAt < :threshold")
    List<RfidTag> findStaleTags(@Param("threshold") LocalDateTime threshold);

    /**
     * Check if a UID already exists
     * Returns true/false instead of the actual tag
     * More efficient than findByUid when you just need to check existence
     *
     * @param uid - RFID tag identifier
     * @return true if exists, false otherwise
     */
    boolean existsByUid(String uid);

}