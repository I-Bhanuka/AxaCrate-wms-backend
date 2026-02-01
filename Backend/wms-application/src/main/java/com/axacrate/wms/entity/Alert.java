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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private AppUser resolvedBy;

    public void resolve(AppUser user) {
        this.alertStatus = AlertStatus.RESOLVED;
        this.resolvedAt = OffsetDateTime.now();
        this.resolvedBy = user;
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

    public enum AlertType {
        UNAUTHORIZED_MOVEMENT("UNAUTHORIZED MOVEMENT"),
        TAG_MISMATCH("TAG_MISMATCH"),
        OFFLINE_READ("OFFLINE_READ"),
        SYNC_FAILURE("SYNC_FAILURE");

        private final String value;

        AlertType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
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