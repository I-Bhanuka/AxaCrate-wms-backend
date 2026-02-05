package com.axacrate.wms.controller;

import com.axacrate.wms.dto.ApiResponse;
import com.axacrate.wms.dto.InventoryItemRequestDTO;
import com.axacrate.wms.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // Marks that this class handles http requests and automatically make the return object json
@RequestMapping("/api/inventory") // Base URL for all end points in this controller (eg: POST /api/inventory/...)
public class InventoryController {

    public final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    } // Spring will inject the service

    @PostMapping("/create") // POST /api/inventory/create
    public ResponseEntity<ApiResponse> createInventoryItem(@Valid @RequestBody InventoryItemRequestDTO requestDTO ) {

        // ResponseEntity<ApiResponse> allows us to return HTTP status codes along with the response body
        // @RequestBody tells Spring to map the incoming JSON to the DTO object
        // @Valid tells Spring to validate the DTO based on annotations in the DTO class


        // Delegate to service
        inventoryService.createInventoryItem(requestDTO);

        // Return simple response
        return ResponseEntity.ok(
                new ApiResponse("success", "Inventory item created")
        );
    }
    // Other endpoints like update, delete, get can be added similarly
}
