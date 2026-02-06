package com.axacrate.wms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rfid_hardware")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"zoneLocation", "movementLogs"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RfidHardware {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_location", unique = true)
    @JsonIgnore
    private Zone zoneLocation;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "hardware_type", nullable = false, length = 20)
    private HardwareType hardwareType;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "hardware_status", nullable = false, length = 50)
    private HardwareStatus hardwareStatus;

    @OneToMany(mappedBy = "hardware", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MovementLog> movementLogs = new ArrayList<>();

    // Custom setter - maintains bidirectional relationship
    public void setZoneLocation(Zone zone) {
        this.zoneLocation = zone;
        if (zone != null && zone.getRfidHardware() != this) {
            zone.setRfidHardware(this);
        }
    }

    public boolean isOperational() {
        return hardwareStatus == HardwareStatus.ACTIVE;
    }

    public boolean isReader() {
        return hardwareType == HardwareType.READER;
    }

    public boolean isWriter() {
        return hardwareType == HardwareType.WRITER;
    }

    public String getZoneName() {
        return zoneLocation != null ? zoneLocation.getName() : "UNASSIGNED";
    }

    public enum HardwareType {
        READER,
        WRITER
    }

    public enum HardwareStatus {
        ACTIVE,
        INACTIVE,
        FAULTY
    }
}