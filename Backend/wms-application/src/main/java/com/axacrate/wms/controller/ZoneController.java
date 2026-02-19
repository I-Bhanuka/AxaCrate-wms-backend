package com.axacrate.wms.controller;

import com.axacrate.wms.dto.ZoneCreateDTO;
import com.axacrate.wms.dto.ZoneResponseDTO;
import com.axacrate.wms.service.ZoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/zone")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService zoneService;

    // Creates a new zone in the system
    @PostMapping("/create")
    public ResponseEntity<ZoneResponseDTO> createZone(@Valid @RequestBody ZoneCreateDTO dto) {
        ZoneResponseDTO createdZone = zoneService.createZone(dto);
        return new ResponseEntity<>(createdZone, HttpStatus.CREATED);
    }

    // Getting all the zones in the database
    @GetMapping("/all")
    public ResponseEntity<List<ZoneResponseDTO>> getAllZones() {
        return ResponseEntity.ok(zoneService.getAllZones());
    }

    // Retrieving a specific zone by its ID
    @GetMapping("/{id}")
    public ResponseEntity<ZoneResponseDTO> getZoneById(@PathVariable("id") UUID id) {
        ZoneResponseDTO zone = zoneService.getZoneById(id);
        return ResponseEntity.ok(zone);
    }

}
