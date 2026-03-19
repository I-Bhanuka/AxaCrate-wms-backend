package com.axacrate.wms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dispatch_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispatchOrder {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "reference_number", nullable = false, unique = true, length = 100)
    private String referenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private AppUser createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DispatchStatus status = DispatchStatus.PENDING;

    @Column(name = "expected_dispatch_date")
    private LocalDate expectedDispatchDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "dispatchOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DispatchOrderItem> items = new ArrayList<>();

    public boolean isActive() {
        return status == DispatchStatus.PENDING || status == DispatchStatus.APPROVED;
    }

    public enum DispatchStatus {
        PENDING,
        APPROVED,
        DISPATCHED,
        CANCELLED
    }
}