package com.axacrate.wms.service;

import com.axacrate.wms.dto.MovementLogResponseDTO;
import com.axacrate.wms.dto.DashboardReportRequestDTO;
import com.axacrate.wms.dto.DashboardReportResponseDTO;
import com.axacrate.wms.dto.LowStockItemDTO;
import com.axacrate.wms.entity.InventoryItem;
import com.axacrate.wms.repository.InventoryItemRepository;
import com.axacrate.wms.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final InventoryItemRepository inventoryRepository;
    private final ZoneRepository zoneRepository;
    private final MovementLogService movementLogService;

    @Transactional(readOnly = true)
    public DashboardReportResponseDTO generateDashboardReport(DashboardReportRequestDTO request) {
        long totalItems = inventoryRepository.count();

        Long totalQuantity = inventoryRepository.sumAllQuantities();
        if (totalQuantity == null) {
            totalQuantity = 0L;
        }

        List<InventoryItem> lowStockEntities = inventoryRepository.findLowStockItems();
        long lowStockCount = lowStockEntities.size();

        long activeZones = zoneRepository.count();

        List<LowStockItemDTO> lowStockItems = new ArrayList<>();
        if (Boolean.TRUE.equals(request.getIncludeLowStock())) {
            lowStockItems = lowStockEntities.stream()
                    .map(this::mapLowStockItem)
                    .toList();
        }

        List<MovementLogResponseDTO> recentMovements = new ArrayList<>();
        if (Boolean.TRUE.equals(request.getIncludeRecentMovements())) {
            int limit = request.getRecentMovementLimit() != null ? request.getRecentMovementLimit() : 10;
            recentMovements = movementLogService.getRecentMovements(limit);
        }

        return DashboardReportResponseDTO.builder()
                .generatedAt(OffsetDateTime.now())
                .totalItems(totalItems)
                .totalQuantity(totalQuantity)
                .lowStockCount(lowStockCount)
                .activeZones(activeZones)
                .lowStockItems(lowStockItems)
                .recentMovements(recentMovements)
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] exportDashboardReportCsv(DashboardReportRequestDTO request) {
        DashboardReportResponseDTO report = generateDashboardReport(request);

        StringBuilder csv = new StringBuilder();

        csv.append("Dashboard Report\n");
        csv.append("Generated At,").append(report.getGeneratedAt()).append("\n\n");

        csv.append("Summary\n");
        csv.append("Metric,Value\n");
        csv.append("Total Items,").append(report.getTotalItems()).append("\n");
        csv.append("Total Quantity,").append(report.getTotalQuantity()).append("\n");
        csv.append("Low Stock Count,").append(report.getLowStockCount()).append("\n");
        csv.append("Active Zones,").append(report.getActiveZones()).append("\n\n");

        csv.append("Low Stock Items\n");
        csv.append("SKU,Name,Quantity,Reorder Threshold,Zone\n");
        for (LowStockItemDTO item : report.getLowStockItems()) {
            csv.append(safe(item.getSku())).append(",")
                    .append(safe(item.getName())).append(",")
                    .append(item.getQuantity()).append(",")
                    .append(item.getReorderThreshold()).append(",")
                    .append(safe(item.getZoneName())).append("\n");
        }

        csv.append("\nRecent Movements\n");
        csv.append("Item Name,SKU,From Zone,To Zone,Event Type,Hardware Type,Synced,Occurred At\n");
        for (MovementLogResponseDTO movement : report.getRecentMovements()) {
            csv.append(safe(movement.getItemName())).append(",")
                    .append(safe(movement.getItemSku())).append(",")
                    .append(safe(movement.getFromZoneName())).append(",")
                    .append(safe(movement.getToZoneName())).append(",")
                    .append(safe(movement.getEventType())).append(",")
                    .append(movement.getHardwareType() != null ? movement.getHardwareType() : "").append(",")
                    .append(movement.getSynced()).append(",")
                    .append(movement.getOccurredAt()).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private LowStockItemDTO mapLowStockItem(InventoryItem item) {
        return LowStockItemDTO.builder()
                .sku(item.getSku())
                .name(item.getName())
                .quantity(item.getQuantity())
                .reorderThreshold(item.getReorderThreshold())
                .zoneName(item.getCurrentZone() != null ? item.getCurrentZone().getName() : null)
                .build();
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}