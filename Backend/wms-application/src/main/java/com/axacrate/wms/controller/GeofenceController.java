package com.axacrate.wms.controller;

import com.axacrate.wms.dto.ApiResponse;
import com.axacrate.wms.service.GeofenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/geofence")
@RequiredArgsConstructor
public class GeofenceController {

    private final GeofenceService geofenceService;

    /**
     * GET /api/geofence/rules
     *
     * Returns the complete workflow rule set as a list of allowed transitions.
     * Used by the frontend geofence page to render the zone flow diagram
     * and the rules reference table.
     *
     * Each entry in the list represents one allowed transition:
     * {
     *   "from": "UNLOADING_ZONE",
     *   "to":   "WRITER_ZONE",
     *   "label": "Standard inbound"
     * }
     *
     * No request body or parameters needed — rules are defined in GeofenceService.
     */
    @GetMapping("/rules")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getWorkflowRules() {
        List<Map<String, String>> rules = geofenceService.getWorkflowRules();
        return ResponseEntity.ok(
                ApiResponse.success(rules, "Geofence workflow rules retrieved successfully")
        );
    }
}