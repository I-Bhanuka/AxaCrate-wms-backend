package com.axacrate.wms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReplaceTagRequestDTO {

    @NotBlank(message = "Unhealthy tag UID is required")
    private String unhealthyTagUid;  // the tag being replaced

    @NotBlank(message = "New tag UID is required")
    private String newTagUid;        // the new healthy tag to assign

    @NotBlank(message = "Alert ID is required")
    private String alertId;          // the health alert to resolve after replacement
}