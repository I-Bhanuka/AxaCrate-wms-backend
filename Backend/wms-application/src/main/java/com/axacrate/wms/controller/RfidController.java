package com.axacrate.wms.controller;

import com.axacrate.wms.dto.ApiResponse;
import com.axacrate.wms.dto.RfidReadRequestDTO;
import com.axacrate.wms.service.RfidService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity; // Full https response (status + body)
import org.springframework.web.bind.annotation.*; // Imports RestController and other notations

@RestController // Marks that this class handles http requests and automatically make the return object json
@RequestMapping("/api/rfid") // Base URL for all end points in this controller (eg: POST /api/rfid/read)
public class RfidController {

    private final RfidService rfidService; // Make a dependency on the service

    public RfidController(RfidService rfidService) {
        this.rfidService = rfidService;
    } // Spring will inject the service

    @PostMapping("/read")
    public ResponseEntity<ApiResponse> readRfid(@Valid @RequestBody RfidReadRequestDTO request) {

        // Delegate to service
        rfidService.handleRfidRead(request);

        // Return simple response
        return ResponseEntity.ok(
                new ApiResponse("success", "RFID data received")
        );
    }
}
