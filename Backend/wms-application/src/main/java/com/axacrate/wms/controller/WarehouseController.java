package com.axacrate.wms.controller;

import com.axacrate.wms.dto.ApiResponse;
import com.axacrate.wms.dto.WarehouseResponseDTO;
import com.axacrate.wms.entity.Warehouse;
import com.axacrate.wms.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseRepository warehouseRepository;

    // Getting all the zones in the database
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<WarehouseResponseDTO>> getAllWarehouses() {

        Warehouse warehouse = warehouseRepository.findByName("Main Warehouse").orElse(null);

        WarehouseResponseDTO test = WarehouseResponseDTO.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .build();

        return ResponseEntity.ok(
                ApiResponse.success(test, "Warehouses retrieved successfully")
        );

    }
}