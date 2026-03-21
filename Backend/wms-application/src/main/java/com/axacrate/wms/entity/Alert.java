package com.axacrate.wms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "alert")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 100)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 50)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_status", nullable = false, length = 50)
    private AlertStatus alertStatus;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    // The zone where the alert occurred (e.g. the destination zone of a violation)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private AppUser resolvedBy;

    // ── Lifecycle helpers ────────────────────────────────────────────────────

    public void resolve(AppUser user) {
        this.alertStatus = AlertStatus.RESOLVED;
        this.resolvedAt  = OffsetDateTime.now();
        this.resolvedBy  = user;
    }

    public void acknowledge() {
        this.alertStatus = AlertStatus.ACKNOWLEDGED;
    }

    public boolean isCritical() {
        return severity == Severity.CRITICAL;
    }

    public boolean isResolved() {
        return alertStatus == AlertStatus.RESOLVED;
    }

    // ── Enums ────────────────────────────────────────────────────────────────

    public enum AlertType {

        // ── Security alerts (CRITICAL) ───────────────────────────────────────

        // An RFID tag was detected that has no record in the database.
        // Could indicate a counterfeit tag, a tag that was never registered,
        // or a physical security breach (someone bringing in an unknown item).
        UNKNOWN_TAG,

        // A tag tried to enter a zone it is not allowed to enter based on
        // workflow or category rules (e.g. WRITER_ZONE → DISPATCH_ZONE).
        UNAUTHORIZED_MOVEMENT,

        // Movement was physically blocked (gate locked / alarm triggered).
        // Created after the violation is confirmed and the block is enacted.
        BLOCKED_MOVEMENT,

        // ── Operational alerts (HIGH / MEDIUM) ───────────────────────────────

        // A tag with status INACTIVE or LOST was detected moving in the warehouse.
        // Indicates a decommissioned or reported-lost tag is still in circulation.
        TAG_STATUS_ISSUE,

        // A tag was detected but is not assigned to any inventory item.
        // Happens when a tag was registered but item creation was never completed.
        TAG_MISMATCH,

        // An RFID reader or writer has stopped responding / gone offline.
        HARDWARE_OFFLINE,

        // A general system or configuration error (e.g. missing zone, bad data).
        SYSTEM_ERROR,

        // ── Capacity alerts (HIGH / MEDIUM) ──────────────────────────────────

        // A zone has reached 80% of its capacity — warning threshold.
        ZONE_CAPACITY_WARNING,

        // A zone has reached 100% of its capacity — movement into it is blocked.
        ZONE_CAPACITY_EXCEEDED,

        // An item moved through zones in the wrong sequence
        // (e.g. jumped from UNLOADING directly to DISPATCH, skipping QC).
        WORKFLOW_VIOLATION,

        // ── Inventory alerts (LOW) ───────────────────────────────────────────

        // An item's quantity has dropped to or below the low-stock threshold (<= 10).
        LOW_STOCK,

        // A physical count does not match the system record.
        STOCK_DISCREPANCY,

        // ── Legacy / compatibility ────────────────────────────────────────────

        // Kept for backwards compatibility with existing movement log entries.
        OFFLINE_READ,
        SYNC_FAILURE
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum AlertStatus {
        PENDING,
        ACKNOWLEDGED,
        RESOLVED
    }
}
