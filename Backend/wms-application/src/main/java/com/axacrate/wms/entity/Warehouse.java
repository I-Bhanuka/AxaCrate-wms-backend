package com.axacrate.wms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Warehouse Entity - Represents a physical warehouse
 *
 * Database Table: warehouse
 * Relationships: One warehouse has many zones
 */
@Entity
@Table(name = "warehouse")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "total_capacity")
    private Integer totalCapacity;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default // Builder will initialize the array with default values. This will help to prevent NullPointerException
    private List<Zone> zones = new ArrayList<>();

    // Keep helper methods
    public void addZone(Zone zone) {
        zones.add(zone);
        zone.setWarehouse(this);
    }

    public void removeZone(Zone zone) {
        zones.remove(zone);
        zone.setWarehouse(null);
    }

    @PrePersist
    @PreUpdate
    private void validateCapacity() {
        if (totalCapacity != null && totalCapacity < 0) {
            throw new IllegalArgumentException("Total capacity cannot be negative");
        }
    }
}