package com.axacrate.wms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "movement_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovementLog {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private RfidTag tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_zone_id")
    private Zone fromZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_zone_id")
    private Zone toZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hardware_id", nullable = false)
    private RfidHardware hardware;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private EventType eventType;

    @CreationTimestamp
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "synced", nullable = false)
    @Builder.Default
    private Boolean synced = false;

    public String getMovementDescription() {
        String from = fromZone != null ? fromZone.getName() : "UNKNOWN";
        String to = toZone != null ? toZone.getName() : "UNKNOWN";
        return String.format("%s: %s → %s", eventType.getValue(), from, to);
    }

    public void markAsSynced() {
        this.synced = true;
    }

    public enum EventType {
        IN_MOVEMENT("IN_Movement"),
        QC_MOVEMENT("QC_Movement"),
        STORE_MOVEMENT("STORE_Movement"),
        OUT_MOVEMENT("OUT_Movement");

        private final String value;

        EventType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}