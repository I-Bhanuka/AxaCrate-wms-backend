package com.axacrate.wms.dto;

/**
 * GeoFence
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor


public class GeofenceResponseDTO {
    private boolean authorized;
    private String  statusMessage;
    private String itemId;
    private String serverTimestamp;

}
