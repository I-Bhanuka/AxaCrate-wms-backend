package com.axacrate.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for RFID Write Scan operations
 * Used to communicate tag status back to the UI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RfidWriteScanResponseDTO {

    /**
     * The RFID tag UID (unique identifier)
     */
    private String tagUid;

    /**
     * Tag status from database (ACTIVE, INACTIVE, LOST)
     */
    private String tagStatus;

    /**
     * Scan result status for UI logic
     * Possible values:
     * - "UNASSIGNED" : Tag exists but no inventory item (allow form entry)
     * - "ASSIGNED"   : Tag has inventory item (show data, disable form OR redirect to update/delete)
     */
    private String status;

    /**
     * Message to display to user
     */
    private String message;

    /**
     * Whether UI should allow editing
     * - true  : Show empty form (UNASSIGNED)
     * - false : Show existing data and redirect options (ASSIGNED)
     */
    private boolean allowEdit;

    // ═══════════════════════════════════════════════════
    // Fields populated ONLY if status = "ASSIGNED"
    // ═══════════════════════════════════════════════════

    /**
     * Inventory item ID (if assigned)
     */
    private UUID inventoryItemId;

    /**
     * SKU of the assigned item (if assigned)
     */
    private String sku;

    /**
     * Name of the assigned item (if assigned)
     */
    private String itemName;

    /**
     * Quantity of the assigned item (if assigned)
     */
    private Integer quantity;

    /**
     * Current zone name of the assigned item (if assigned)
     */
    private String currentZone;
}