package com.axacrate.wms.dto;

import com.axacrate.wms.dto.MovementLogResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardReportResponseDTO {
    private OffsetDateTime generatedAt;

    private Long totalItems;
    private Long totalQuantity;
    private Long lowStockCount;
    private Long activeZones;

    private List<LowStockItemDTO> lowStockItems;
    private List<MovementLogResponseDTO> recentMovements;
}
