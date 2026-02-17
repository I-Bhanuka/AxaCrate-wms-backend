package com.axacrate.wms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "zone")
@Getter  // Generate only getters
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"warehouse", "rfidHardware", "inventoryItems"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Zone {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Setter
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "zone_type", nullable = false, length = 20)
    private ZoneType zoneType;

    @Builder.Default
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ZoneStatus status = ZoneStatus.ACTIVE;

    @Setter
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @JsonIgnore
    private Warehouse warehouse;

    @OneToOne(mappedBy = "zoneLocation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private RfidHardware rfidHardware;

    @OneToMany(mappedBy = "currentZone", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InventoryItem> inventoryItems = new ArrayList<>();

    // Custom setter - maintains bidirectional relationship
    public void setWarehouse(Warehouse warehouse) {
        if (this.warehouse != null) {
            this.warehouse.getZones().remove(this);
        }
        this.warehouse = warehouse;
        if (warehouse != null && !warehouse.getZones().contains(this)) {
            warehouse.getZones().add(this);
        }
    }

    // Custom setter - maintains bidirectional relationship
    public void setRfidHardware(RfidHardware hardware) {
        this.rfidHardware = hardware;
        if (hardware != null && hardware.getZoneLocation() != this) {
            hardware.setZoneLocation(this);
        }
    }

    public boolean hasHardware() {
        return rfidHardware != null;
    }

    public String getHardwareType() {
        return rfidHardware != null ? rfidHardware.getHardwareType().toString() : "NONE";
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative");
        }
        if (warehouse == null) {
            throw new IllegalStateException("Zone must belong to a warehouse");
        }
    }

    public enum ZoneType {
        UNLOADING_ZONE("UNLOADING_ZONE"),
        WRITER_ZONE("WRITER_ZONE"),
        QC_ZONE("QC_ZONE"),
        STORAGE_ZONE("STORAGE_ZONE"),
        DISPATCH_ZONE("DISPATCH_ZONE");

        private final String value;

        ZoneType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum ZoneStatus {
        ACTIVE,
        INACTIVE
    }
}