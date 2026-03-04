package com.axacrate.wms.controller;

import com.axacrate.wms.dto.ApiResponse;
import com.axacrate.wms.dto.ZoneCreateDTO;
import com.axacrate.wms.dto.ZoneResponseDTO;
import com.axacrate.wms.dto.ZoneUpdateDTO;
import com.axacrate.wms.service.ZoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService zoneService;

    // Creates a new zone in the system
    @PostMapping
    public ResponseEntity<ApiResponse<ZoneResponseDTO>> createZone(@Valid @RequestBody ZoneCreateDTO dto) {
        ZoneResponseDTO createdZone = zoneService.createZone(dto);
        return new ResponseEntity<>(
                ApiResponse.success(createdZone, "Zone created successfully"),
                HttpStatus.CREATED
        );
    }

    // Getting all the zones in the database
    @GetMapping
    public ResponseEntity<ApiResponse<List<ZoneResponseDTO>>> getAllZones() {
        List<ZoneResponseDTO> zones = zoneService.getAllZones();
        return ResponseEntity.ok(
                ApiResponse.success(zones, "Zones retrieved successfully")
        );
    }

    // Retrieving a specific zone by its ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ZoneResponseDTO>> getZoneById(@PathVariable("id") UUID id) {
        ZoneResponseDTO zone = zoneService.getZoneById(id);
        return ResponseEntity.ok(
                ApiResponse.success(zone, "Zone retrieved successfully")
        );
    }

    // Retrieving a specific zone by its name
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<ZoneResponseDTO>> getZoneByName(@PathVariable String name) {
        ZoneResponseDTO zone = zoneService.getZoneByName(name);
        return ResponseEntity.ok(
                ApiResponse.success(zone, "Zone retrieved successfully")
        );
    }

    // Retrieving a specific zone by its id and then updating the information
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ZoneResponseDTO>> updateZone(
            @PathVariable UUID id,
            @RequestBody ZoneUpdateDTO dto) {
        ZoneResponseDTO updatedZone = zoneService.updateZone(id, dto);
        return ResponseEntity.ok(
                ApiResponse.success(updatedZone, "Zone updated successfully")
        );
    }

    // Updating the zone related details
    @PatchMapping("/warehouse/{warehouseName}/name/{name}")
    public ResponseEntity<ApiResponse<ZoneResponseDTO>> updateZoneByWarehouseAndName(
            @PathVariable String warehouseName,
            @PathVariable String name,
            @RequestBody ZoneUpdateDTO dto) {
        ZoneResponseDTO updatedZone =
                zoneService.updateZoneByWarehouseAndName(warehouseName, name, dto);
        return ResponseEntity.ok(
                ApiResponse.success(updatedZone, "Zone updated successfully")
        );
    }
}
