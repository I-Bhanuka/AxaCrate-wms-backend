package com.axacrate.wms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "dispatch_order_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispatchOrderItem {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatch_order_id", nullable = false)
    private DispatchOrder dispatchOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfid_tag_id", nullable = false)
    private RfidTag rfidTag;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ItemDispatchStatus status = ItemDispatchStatus.PENDING;

    public enum ItemDispatchStatus {
        PENDING,
        DISPATCHED,
        CANCELLED
    }
}