package com.axacrate.wms.controller;
import com.axacrate.wms.dto.ApiResponse;
import com.axacrate.wms.dto.DashboardReportRequestDTO;
import com.axacrate.wms.dto.DashboardReportResponseDTO;
import com.axacrate.wms.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardReportResponseDTO>> generateDashboardReport(
            @RequestBody(required = false) DashboardReportRequestDTO request
    ) {
        if (request == null) {
            request = new DashboardReportRequestDTO();
        }

        DashboardReportResponseDTO report = reportService.generateDashboardReport(request);

        return ResponseEntity.ok(
                ApiResponse.success(report, "Dashboard report generated successfully")
        );
    }

    @PostMapping("/dashboard/export/csv")
    public ResponseEntity<byte[]> exportDashboardReportCsv(
            @RequestBody(required = false) DashboardReportRequestDTO request
    ) {
        if (request == null) {
            request = new DashboardReportRequestDTO();
        }

        byte[] csvBytes = reportService.exportDashboardReportCsv(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dashboard-report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }
}
