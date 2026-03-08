package com.axacrate.wms.controller;

import com.axacrate.wms.dto.ApiResponse;
import com.axacrate.wms.dto.GeofenceEventDTO;
import com.axacrate.wms.dto.GeofenceResponseDTO;
import com.axacrate.wms.service.GeofenceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/geofence")
public class GeofenceController {

    private final GeofenceService geofenceService;

    public GeofenceController(GeofenceService geofenceService) {
        this.geofenceService = geofenceService;
    }

    // Called by ESP32 reader when a tag is detected at a geofence zone
    @PostMapping("/event")
    public ResponseEntity<ApiResponse<GeofenceResponseDTO>> handleEvent(
            @Valid @RequestBody GeofenceEventDTO request) {

        GeofenceResponseDTO response = geofenceService.processGeofenceEvent(
                request.getTagUid(),
                request.getHardwareId(),
                request.getDirection()
        );

        return ResponseEntity.ok(
                ApiResponse.success(response, "Geofence event processed")
        );
    }
}