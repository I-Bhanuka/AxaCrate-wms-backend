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
    private String        tagStatus;           // ACTIVE, INACTIVE, LOST
    private String        healthStatus;        // HEALTHY, UNHEALTHY
    private long          readsInWindow;       // how many reads in the measurement window
    private long          windowSeconds;       // the measurement window in seconds
    private double        readsPerSecond;      // calculated: readsInWindow / windowSeconds
    private double        minReadsPerSecond;   // hardcoded minimum standard
    private String        inventoryItemId;
    private String        inventoryItemName;
    private String        lastSeenZone;
    private LocalDateTime lastSeenAt;
    private boolean       alertRaised;
}