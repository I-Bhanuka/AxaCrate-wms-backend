package com.axacrate.wms.repository;

import com.axacrate.wms.entity.DispatchOrder;
import com.axacrate.wms.entity.RfidTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DispatchOrderRepository extends JpaRepository<DispatchOrder, UUID> {

    // Main query used by GeofenceService on every scan
    // Checks if this tag is in any PENDING/APPROVED dispatch order
    @Query("""
        SELECT d FROM DispatchOrder d
        JOIN d.items i
        WHERE i.rfidTag = :tag
        AND d.status IN ('PENDING', 'APPROVED')
        AND i.status = 'PENDING'
    """)
    Optional<DispatchOrder> findActiveOrderByTag(@Param("tag") RfidTag tag);

    Optional<DispatchOrder> findByReferenceNumber(String referenceNumber);
}