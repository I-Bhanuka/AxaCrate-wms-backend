package com.axacrate.wms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rfid_tag")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfidTag {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "uid", nullable = false, unique = true, length = 255)
    private String uid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id")
    private InventoryItem inventoryItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private RfidStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_seen_zone_id")
    private Zone lastSeenZone;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MovementLog> movementLogs = new ArrayList<>();

    public void updateLastSeen(Zone zone) {
        this.lastSeenAt = LocalDateTime.now();
        this.lastSeenZone = zone;
    }

    public boolean isActive() {
        return status == RfidStatus.ACTIVE;
    }

    public enum RfidStatus {
        ACTIVE,
        INACTIVE,
        LOST
    }
}