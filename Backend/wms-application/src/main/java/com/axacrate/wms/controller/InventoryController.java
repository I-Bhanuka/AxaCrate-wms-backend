package com.axacrate.wms.controller;

import com.axacrate.wms.dto.*;
import com.axacrate.wms.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
                ApiResponse.success("Inventory item created", null)
        );
    }

    @GetMapping //GET /api/inventory for getting all inventory items
    public ResponseEntity<ApiResponse<Page<InventoryItemResponseDTO>>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt, desc") String sort,
            @RequestParam(required = false) UUID zoneId,
            @RequestParam(required = false) Integer minQuantity,
            @RequestParam(required = false) Integer maxQuantity,
            @RequestParam(required = false) String status // status can be "assigned", "unassigned", "damaged" (This is for future )
    ){
        //validate size limit to prevent excessive queries
        if (size > 100) {
            size = 100;
        }
        Page<InventoryItemResponseDTO> items = inventoryService.getAllItems(page, size, sort, zoneId, minQuantity, maxQuantity, status);

        return ResponseEntity.ok(
                ApiResponse.success(items,"Inventory items retrieved successfully")
        );
    }

    @GetMapping("/dashboard") // GET /api/inventory/dashboard for getting inventory summary for dashboard
    public ResponseEntity<ApiResponse<InventoryDashboardDTO>>getDashboard() {

         //Delegate to service to get dashboard summary
         InventoryDashboardDTO dashboard = inventoryService.getDashboardSummary();

         //Return response with dashboard data
        return ResponseEntity.ok(
                ApiResponse.success(dashboard, "Inventory dashboard summary retrieved successfully")
        );
    }

    @GetMapping("/{id}") // GET /api/inventory/{id} for getting inventory item details by id
    public ResponseEntity<ApiResponse<InventoryItemResponseDTO>> getItemById(@PathVariable UUID id) {

        InventoryItemResponseDTO item = inventoryService.getItemById(id);

        return ResponseEntity.ok(
                ApiResponse.success(item, "Inventory item details retrieved successfully")
        );
    }

    @GetMapping("/sku/{sku}") // GET /api/inventory/sku/{sku} for getting inventory item details by SKU
    public ResponseEntity<ApiResponse<InventoryItemResponseDTO>> getItemBySku(@PathVariable String sku) {

        InventoryItemResponseDTO item = inventoryService.getItemBySku(sku);

        return ResponseEntity.ok(
                ApiResponse.success(item, "Inventory item details retrieved successfully")
        );
     }

     @PutMapping("/{sku}") // PUT /api/inventory/sku/{sku} for updating inventory item details by SKU
     public ResponseEntity<ApiResponse> updateItemBySku(@PathVariable String sku, @Valid @RequestBody InventoryUpdateRequestDTO requestDTO) {

           InventoryUpdateRequestDTO updated = inventoryService.updateItemBySku(sku, requestDTO);

            return ResponseEntity.ok(
                    ApiResponse.success(updated, "Inventory item updated successfully")
            );
     }


    @DeleteMapping("/{sku}") // DELETE /api/inventory/{sku} for deleting a specific inventory item by sku
    public ResponseEntity<ApiResponse> deleteItemBySku(@PathVariable String sku) {

        inventoryService.deleteItemBySku(sku);

        return ResponseEntity.ok(
                ApiResponse.success("Inventory item deleted successfully", null)
        );
    }


}
