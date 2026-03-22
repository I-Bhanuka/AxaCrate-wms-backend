package com.axacrate.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagHealthResponseDTO {
    private UUID          tagId;
    private String        tagUid;
    private String        tagStatus;        // ACTIVE, INACTIVE, LOST
    private String        healthStatus;     // HEALTHY, UNHEALTHY
    private long          readsLastHour;    // how many times scanned in last hour
    private int           minRequired;      // hardcoded standard
    private String        inventoryItemId;
    private String        inventoryItemName;
    private String        lastSeenZone;
    private LocalDateTime lastSeenAt;
    private boolean       alertRaised;
}