package com.axacrate.wms.dto;
import lombok.Data;

@Data
public class DashboardReportRequestDTO {
    private Integer recentMovementLimit = 10;
    private Boolean includeLowStock = true;
    private Boolean includeRecentMovements = true;

}
