package com.axacrate.wms.service;

import com.axacrate.wms.dto.GeofenceResponseDTO;
import com.axacrate.wms.entity.Alert;
import com.axacrate.wms.entity.RfidHardware;
import com.axacrate.wms.entity.RfidTag;
import com.axacrate.wms.entity.Zone;
import com.axacrate.wms.exception.GeofenceViolationException;
import com.axacrate.wms.repository.AlertRepository;
import com.axacrate.wms.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeofenceService {

    private final AlertRepository         alertRepository;
    private final InventoryItemRepository inventoryItemRepository;

    // ── Workflow transition rules ──────────────────────────────────────────────
    //
    // Standard path:  UNLOADING -> WRITER -> QC -> STORAGE -> DISPATCH
    // Allowed return: QC can reject items back to UNLOADING
    //
    // To add a new allowed transition, only this map needs to change.
    //
    private static final Map<Zone.ZoneType, Set<Zone.ZoneType>> ALLOWED_TRANSITIONS = Map.of(
            Zone.ZoneType.UNLOADING_ZONE, Set.of(Zone.ZoneType.WRITER_ZONE),
            Zone.ZoneType.WRITER_ZONE,    Set.of(Zone.ZoneType.QC_ZONE),
            Zone.ZoneType.QC_ZONE,        Set.of(Zone.ZoneType.STORAGE_ZONE,
                    Zone.ZoneType.UNLOADING_ZONE),
            Zone.ZoneType.STORAGE_ZONE,   Set.of(Zone.ZoneType.DISPATCH_ZONE),
            Zone.ZoneType.DISPATCH_ZONE,  Set.of()
    );

    // Human-readable labels for each transition (returned by GET /api/geofence/rules)
    private static final Map<String, String> TRANSITION_LABELS = Map.of(
            "UNLOADING_ZONE->WRITER_ZONE",  "Standard inbound",
            "WRITER_ZONE->QC_ZONE",         "Post-tagging inspection",
            "QC_ZONE->STORAGE_ZONE",        "QC approved",
            "QC_ZONE->UNLOADING_ZONE",      "QC rejected — return",
            "STORAGE_ZONE->DISPATCH_ZONE",  "Outbound dispatch"
    );

    private static final double CAPACITY_WARNING_THRESHOLD = 0.80;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Main geofence validation entry point. Called by RfidService BEFORE any DB writes.
     * Throws GeofenceViolationException on any rule failure — the caller's @Transactional
     * will roll back everything automatically.
     *
     * Rule chain:
     *   1. Hardware check   — reader operational?
     *   2. Tag check        — tag known and active?
     *   3. Workflow check   — zone sequence allowed?
     *   3b. Inbound spec    — (stub) item allowed into the system?
     *   3c. Outbound spec   — (stub) item cleared to leave?
     *   4. Capacity check   — room in destination zone?
     */
    @Transactional
    public GeofenceResponseDTO processGeofenceEvent(
            RfidTag tag,
            RfidHardware hardware,
            Zone fromZone,
            Zone toZone) {

        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        log.info("Geofence check — tag: {}, from: {}, to: {}",
                tag.getUid(),
                fromZone != null ? fromZone.getName() : "NONE (first scan)",
                toZone.getName());

        checkHardware(hardware, toZone);
        checkTag(tag, toZone);
        checkWorkflow(tag, fromZone, toZone);
        checkInboundSpec(tag, toZone);
        checkOutboundSpec(tag, toZone);
        checkCapacity(toZone);

        log.info("Geofence PASSED — tag: {} -> zone: {}", tag.getUid(), toZone.getName());

        return GeofenceResponseDTO.builder()
                .authorized(true)
                .statusMessage("Movement authorized: " + describeMove(fromZone, toZone))
                .serverTimestamp(timestamp)
                .build();
    }

    /**
     * Returns the full workflow rule set as a serializable list.
     * Called by GET /api/geofence/rules for the frontend.
     *
     * Each entry: { "from": "UNLOADING_ZONE", "to": "WRITER_ZONE", "label": "Standard inbound" }
     */
    public List<Map<String, String>> getWorkflowRules() {
        List<Map<String, String>> rules = new ArrayList<>();

        ALLOWED_TRANSITIONS.forEach((fromType, toTypes) ->
                toTypes.forEach(toType -> {
                    String key   = fromType.name() + "->" + toType.name();
                    String label = TRANSITION_LABELS.getOrDefault(key, "Allowed transition");
                    rules.add(Map.of(
                            "from",  fromType.name(),
                            "to",    toType.name(),
                            "label", label
                    ));
                })
        );

        return rules;
    }

    // ── Rule 1: Hardware check ────────────────────────────────────────────────

    private void checkHardware(RfidHardware hardware, Zone toZone) {
        if (!hardware.isOperational()) {
            log.warn("Hardware FAILED — {} not operational", hardware.getName());
            saveAlert(Alert.AlertType.HARDWARE_OFFLINE, Alert.Severity.HIGH,
                    "Reader [" + hardware.getName() + "] is offline. Movement in ["
                            + toZone.getName() + "] cannot be validated.", toZone);
            throw new GeofenceViolationException(
                    "Hardware [" + hardware.getName() + "] is not operational.",
                    null, null, toZone.getName());
        }
        if (!hardware.isReader()) {
            log.warn("Hardware FAILED — {} is a WRITER, not a READER", hardware.getName());
            saveAlert(Alert.AlertType.SYSTEM_ERROR, Alert.Severity.MEDIUM,
                    "Device [" + hardware.getName() + "] is a WRITER, not a READER. "
                            + "Movement event rejected.", toZone);
            throw new GeofenceViolationException(
                    "Hardware [" + hardware.getName() + "] is not a reader.",
                    null, null, toZone.getName());
        }
    }

    // ── Rule 2: Tag check ─────────────────────────────────────────────────────

    private void checkTag(RfidTag tag, Zone toZone) {
        if (!tag.isActive()) {
            log.warn("Tag FAILED — {} has status {}", tag.getUid(), tag.getStatus());
            saveAlert(Alert.AlertType.TAG_STATUS_ISSUE, Alert.Severity.HIGH,
                    "Tag [" + tag.getUid() + "] has status [" + tag.getStatus() + "] "
                            + "but was detected moving into [" + toZone.getName() + "]. "
                            + "This tag should not be in circulation.", toZone);
            throw new GeofenceViolationException(
                    "Tag [" + tag.getUid() + "] is " + tag.getStatus() + ". Movement denied.",
                    tag.getId(), null, toZone.getName());
        }
    }

    // ── Rule 3: Workflow check ────────────────────────────────────────────────

    private void checkWorkflow(RfidTag tag, Zone fromZone, Zone toZone) {

        // First scan — no previous zone. Only UNLOADING_ZONE is a valid entry point.
        if (fromZone == null) {
            if (toZone.getZoneType() != Zone.ZoneType.UNLOADING_ZONE) {
                log.warn("Workflow FAILED — first scan for {} landed in {} (expected UNLOADING_ZONE)",
                        tag.getUid(), toZone.getName());
                saveAlert(Alert.AlertType.WORKFLOW_VIOLATION, Alert.Severity.HIGH,
                        "Tag [" + tag.getUid() + "] first scanned in [" + toZone.getName()
                                + "]. New items must enter via UNLOADING_ZONE.", toZone);
                throw new GeofenceViolationException(
                        "First scan must be UNLOADING_ZONE, not [" + toZone.getName() + "].",
                        tag.getId(), "NONE", toZone.getName());
            }
            return;
        }

        // Same-zone re-read — RFID noise, not an error
        if (fromZone.getId().equals(toZone.getId())) {
            log.debug("Same-zone re-read for {} in {}. Skipping.", tag.getUid(), toZone.getName());
            return;
        }

        // Standard transition check
        Set<Zone.ZoneType> allowed = ALLOWED_TRANSITIONS.getOrDefault(fromZone.getZoneType(), Set.of());
        if (!allowed.contains(toZone.getZoneType())) {
            log.warn("Workflow FAILED — {} -> {} not allowed for tag {}",
                    fromZone.getName(), toZone.getName(), tag.getUid());
            saveAlert(Alert.AlertType.UNAUTHORIZED_MOVEMENT, Alert.Severity.CRITICAL,
                    "Tag [" + tag.getUid() + "] attempted unauthorized move: ["
                            + fromZone.getName() + "] -> [" + toZone.getName()
                            + "]. Not permitted by workflow rules.", toZone);
            throw new GeofenceViolationException(
                    "Unauthorized movement: [" + fromZone.getName() + "] -> [" + toZone.getName() + "].",
                    tag.getId(), fromZone.getName(), toZone.getName());
        }
    }

    // ── Rule 3b: Inbound spec check ───────────────────────────────────────────
    //
    // Fires when an item moves into WRITER_ZONE (official system entry point).
    // Implement this when a PurchaseOrder entity is available. It should check:
    //   - A PO exists for this supplier / item category
    //   - The PO receiving window is currently open
    //   - The PO expected quantity has not already been fulfilled
    //
    private void checkInboundSpec(RfidTag tag, Zone toZone) {
        if (toZone.getZoneType() != Zone.ZoneType.WRITER_ZONE) return;

        // TODO: implement when PurchaseOrder entity is available
        // PurchaseOrder po = purchaseOrderRepository.findOpenForTag(tag).orElse(null);
        // if (po == null) { saveAlert(...); throw new GeofenceViolationException(...); }
        // if (po.isExpired()) { saveAlert(...); throw new GeofenceViolationException(...); }
        // if (po.isFulfilled()) { saveAlert(...); throw new GeofenceViolationException(...); }

        log.debug("Inbound spec check passed for tag {} (PO system not yet implemented)", tag.getUid());
    }

    // ── Rule 3c: Outbound spec check ──────────────────────────────────────────
    //
    // Fires when an item moves into DISPATCH_ZONE (leaves the warehouse).
    // Implement this when a DispatchOrder entity is available. It should check:
    //   - A dispatch order exists for this item
    //   - The item visited QC_ZONE and was not rejected (check movement log)
    //   - The dispatch window is currently open
    //   - The item is not flagged as hold / quarantine
    //
    private void checkOutboundSpec(RfidTag tag, Zone toZone) {
        if (toZone.getZoneType() != Zone.ZoneType.DISPATCH_ZONE) return;

        // TODO: implement when DispatchOrder entity is available
        // DispatchOrder order = dispatchOrderRepository.findOpenForTag(tag).orElse(null);
        // if (order == null) { saveAlert(...); throw new GeofenceViolationException(...); }
        // if (order.isOnHold()) { saveAlert(...); throw new GeofenceViolationException(...); }
        // boolean passedQc = movementLogRepository.didVisitZoneType(tag, ZoneType.QC_ZONE);
        // if (!passedQc) { saveAlert(...); throw new GeofenceViolationException(...); }

        log.debug("Outbound spec check passed for tag {} (dispatch order system not yet implemented)", tag.getUid());
    }

    // ── Rule 4: Capacity check ────────────────────────────────────────────────

    private void checkCapacity(Zone toZone) {
        int capacity = toZone.getCapacity();
        if (capacity <= 0) return; // 0 = unlimited

        long currentCount = inventoryItemRepository.countItemsInZone(toZone.getId());

        if (currentCount >= capacity) {
            log.warn("Capacity FAILED — zone {} full ({}/{})", toZone.getName(), currentCount, capacity);
            saveAlert(Alert.AlertType.ZONE_CAPACITY_EXCEEDED, Alert.Severity.HIGH,
                    "Zone [" + toZone.getName() + "] is at full capacity ("
                            + currentCount + "/" + capacity + "). Movement blocked.", toZone);
            throw new GeofenceViolationException(
                    "Zone [" + toZone.getName() + "] is at full capacity.",
                    null, null, toZone.getName());
        }

        double fillRatio = (double) currentCount / capacity;
        if (fillRatio >= CAPACITY_WARNING_THRESHOLD) {
            log.warn("Capacity WARNING — zone {} at {}% ({}/{})",
                    toZone.getName(), (int)(fillRatio * 100), currentCount, capacity);
            saveAlert(Alert.AlertType.ZONE_CAPACITY_WARNING, Alert.Severity.MEDIUM,
                    "Zone [" + toZone.getName() + "] is " + (int)(fillRatio * 100) + "% full ("
                            + currentCount + "/" + capacity + "). Consider redistributing.", toZone);
            // Warning only — movement still allowed
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void saveAlert(Alert.AlertType type, Alert.Severity severity,
                           String message, Zone zone) {
        alertRepository.save(Alert.builder()
                .alertType(type)
                .severity(severity)
                .alertStatus(Alert.AlertStatus.PENDING)
                .message(message)
                .zone(zone)
                .build());
        log.info("Alert saved — type: {}, severity: {}, zone: {}",
                type, severity, zone != null ? zone.getName() : "none");
    }

    private String describeMove(Zone from, Zone to) {
        return (from != null ? from.getName() : "ENTRY") + " -> " + to.getName();
    }
}