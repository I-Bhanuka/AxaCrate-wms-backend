package com.axacrate.wms.controller;

import com.axacrate.wms.dto.AlertResponseDTO;
import com.axacrate.wms.dto.ApiResponse;
import com.axacrate.wms.dto.CreateAlertDTO;
import com.axacrate.wms.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    /** GET /api/alerts — return all alerts (optionally filter by ?status= and/or ?zoneId=) */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertResponseDTO>>> getAlerts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID zoneId
    ) {
        List<AlertResponseDTO> alerts;
        if (zoneId != null) {
            alerts = alertService.getAlertsByZone(zoneId);
        } else if (status != null && !status.isBlank()) {
            alerts = alertService.getAlertsByStatus(status);
        } else {
            alerts = alertService.getAllAlerts();
        }

        return ResponseEntity.ok(
                ApiResponse.success(alerts, "Alerts retrieved successfully")
        );
    }

    /** GET /api/alerts/unresolved — return only unresolved alerts, priority sorted */
    @GetMapping("/unresolved")
    public ResponseEntity<ApiResponse<List<AlertResponseDTO>>> getUnresolvedAlerts() {
        List<AlertResponseDTO> alerts = alertService.getUnresolvedAlerts();
        return ResponseEntity.ok(
                ApiResponse.success(alerts, "Unresolved alerts retrieved successfully")
        );
    }

    /** POST /api/alerts — create a new alert */
    @PostMapping
    public ResponseEntity<ApiResponse<AlertResponseDTO>> createAlert(@Valid @RequestBody CreateAlertDTO dto) {
        AlertResponseDTO created = alertService.createAlert(dto);
        return new ResponseEntity<>(
                ApiResponse.success(created, "Alert created successfully"),
                HttpStatus.CREATED
        );
    }

    /** PUT /api/alerts/{id}/acknowledge — acknowledge an alert */
    @PutMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<AlertResponseDTO>> acknowledgeAlert(@PathVariable UUID id) {
        AlertResponseDTO updated = alertService.acknowledgeAlert(id);
        return ResponseEntity.ok(
                ApiResponse.success(updated, "Alert acknowledged successfully")
        );
    }

    /** PUT /api/alerts/{id}/resolve — resolve an alert */
    @PutMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<AlertResponseDTO>> resolveAlert(@PathVariable UUID id) {
        AlertResponseDTO updated = alertService.resolveAlert(id);
        return ResponseEntity.ok(
                ApiResponse.success(updated, "Alert resolved successfully")
        );
    }
}
