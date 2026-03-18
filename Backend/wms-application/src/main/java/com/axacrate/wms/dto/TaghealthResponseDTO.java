package com.axacrate.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaghealthResponseDTO {

    private String tagUid;
    private String status;       // "HEALTHY" or "UNDERPERFORMING"
    private long   readsLastHour; // How many times it was scanned in the last hour
    private int    minRequired;  // The standard (hardcoded MIN_READS_PER_HOUR)
    private boolean alertRaised; // Whether an alert was saved to DB
}