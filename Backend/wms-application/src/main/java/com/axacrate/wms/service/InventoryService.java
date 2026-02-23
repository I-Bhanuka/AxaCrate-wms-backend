package com.axacrate.wms.service;

import com.axacrate.wms.dto.InventoryItemRequestDTO;
import com.axacrate.wms.dto.InventoryItemResponseDTO;
import com.axacrate.wms.dto.InventoryDashboardDTO;
import com.axacrate.wms.entity.InventoryItem;
import com.axacrate.wms.entity.RfidTag;
import com.axacrate.wms.entity.Zone;
import com.axacrate.wms.exception.BusinessRuleViolationException;
import com.axacrate.wms.exception.ResourceNotFoundException;
import com.axacrate.wms.repository.InventoryItemRepository;
import com.axacrate.wms.repository.RfidTagRepository;
import com.axacrate.wms.repository.ZoneRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepo;
    private final RfidTagRepository rfidTagRepository;
    private final ZoneRepository zoneRepository;

    @Autowired
    public InventoryService(InventoryItemRepository inventoryItemRepo, RfidTagRepository rfidTagRepository, ZoneRepository zoneRepository) {
        this.inventoryItemRepo = inventoryItemRepo;
        this.rfidTagRepository = rfidTagRepository;
        this.zoneRepository = zoneRepository;
    }

    @Transactional
    public void createInventoryItem(InventoryItemRequestDTO requestDTO) {


        // Find RFID Tag entity by UID
        RfidTag rfidTag = rfidTagRepository.findByUid(requestDTO.getRfidTag()).
                orElseThrow(() -> new ResourceNotFoundException("RFID Tag not found"));

        // Check if RFID Tag is already assigned to an inventory item
        if (rfidTag.getInventoryItem() != null) {
            throw new BusinessRuleViolationException("RFID Tag already assigned");
        }

        log.info("RFID Tag found: {}", rfidTag.getUid());

        // Find Writer Zone
        Zone writerZone = zoneRepository.findByNameAndZoneType(requestDTO.getZoneName() , Zone.ZoneType.WRITER_ZONE).
                orElseThrow(() -> new IllegalArgumentException("Writer zone not found"));

        log.info("Zone found: {}", writerZone);

        // Create Inventory Item entity
        InventoryItem entity = InventoryItem.builder()
                .sku(requestDTO.getSku())
                .name(requestDTO.getName())
                .quantity(requestDTO.getQuantity())
                .currentZone(writerZone)
                .rfidTag(rfidTag)
                .build();

        // save to rfid tag the inventory item assignment
        rfidTag.setInventoryItem(entity);

        // Save entity to database
        inventoryItemRepo.save(entity);

        //TODO:
        // later send data to item id to esp32 for rfid tag assignment
    }

    @Transactional(readOnly = true)
    public Page<InventoryItemResponseDTO> getAllItems(
            int page,
            int size,
            String sortString,
            UUID zoneId,
            Integer minQuantity,
            Integer maxQuantity,
            String status
    ) {
        log.info("Searching for inventory items: page={}, size={}, sort={}", page, size, sortString);

        // Parse sort parameter
        Sort sort = parseSortParameter(sortString);

        // Create Pageable object
        Pageable pageable = PageRequest.of(page, size, sort);

        // Build dynamic query based on filters
        // This allows combining multiple filters

        Specification<InventoryItem> spec = Specification.where((Specification<InventoryItem>) (root, query, cb) -> cb.conjunction());

        // Filter by zoneId
        if (zoneId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("currentZone").get("id"), zoneId));
        }

        // Filter by minQuantity
        if (minQuantity != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("quantity"), minQuantity));
        }

        // Filter by maxQuantity
        if (maxQuantity != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("quantity"), maxQuantity));
        }

        // Filter by status (assigned/unassigned/damaged/out of stock)
        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));

        }

        // Execute query with specifications and pagination+
        Page<InventoryItem> items = inventoryItemRepo.findAll(spec, pageable);

        log.info("Retrieved {} items out of {} total",
                items.getTotalElements(), items.getTotalElements());

        // Map entities to DTOs
        return items.map(item ->
                InventoryItemResponseDTO.builder()
                        .id(item.getId())
                        .sku(item.getSku())
                        .name(item.getName())
                        .currentZoneName(item.getCurrentZoneName())
                        .currentZoneId(item.getCurrentZone().getId())
                        .rfidTagUid(item.getRfidTag() != null ? item.getRfidTag().getUid() : null)
                        .rfidTagStatus(item.getRfidTag() != null ? item.getRfidTag().getStatus() : null)
                        .createdAt(item.getCreatedAt())
                        .quantity(item.getQuantity())
                        .build());
    }

    // Helper methods

    /**
     * Parse sort parameter from string to Sort object
     * Format: "createdAt, desc"
     */
    private Sort parseSortParameter(String sortString) {
        String[] split = sortString.split(",");

        // below lines to avoid index out of bound error
        String fields = split.length > 0 ? split[0] : "createdAt";
        String direction = split.length > 1 ? split[1] : "desc";

        // validate field name to avoid sql injection
        List<String> validFields = List.of("sku", "name", "quantity", "createdAt", "updatedAt");

        if (!validFields.contains(fields)) {
            log.warn("Invalid sort parameter: {}", fields);
            fields = "createdAt"; // default field
        }

        // Parse direction
        Sort.Direction dir = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        return Sort.by(dir, fields);

    }

    @Transactional(readOnly = true)
    public InventoryDashboardDTO getDashboardSummary() {
        log.info("Fetching inventory dashboard summary");

        // Get all inventory items
        List<InventoryItem> allItems = inventoryItemRepo.findAll();

        // Calculate total items count
        long totalItems = allItems.size();
        log.info("Total items in inventory: {}", totalItems);

        // Calculate total quantity
        long totalQuantity = allItems.stream()
                .mapToLong(InventoryItem::getQuantity)
                .sum();
        log.info("Total quantity in inventory: {}", totalQuantity);

        // Count low stock items (threshold = 5, adjust as needed)
        int lowStockThreshold = 5;
        long lowStockCount = allItems.stream()
                .filter(item -> item.getQuantity() < lowStockThreshold)
                .count();
        log.info("Low stock items count: {}", lowStockCount);

        // Group items by zone
        Map<String, Long> itemsByZone = allItems.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        InventoryItem::getCurrentZoneName,
                        java.util.stream.Collectors.counting()
                ));
        log.info("Items grouped by zone: {}", itemsByZone);

        // Get recent items (last 5 items, sorted by creation date)
        List<InventoryItemResponseDTO> recentItems = allItems.stream()
                .sorted((a, b) -> b.getId().compareTo(a.getId())) // Simple sort by ID (assuming ID is generated in order)
                .limit(5)
                .map(item -> InventoryItemResponseDTO.builder()
                        .id(item.getId())
                        .sku(item.getSku())
                        .name(item.getName())
                        .currentZoneName(item.getCurrentZoneName())
                        .currentZoneId(item.getCurrentZone().getId())
                        .rfidTagUid(item.getRfidTag() != null ? item.getRfidTag().getUid() : null)
                        .rfidTagStatus(item.getRfidTag() != null ? item.getRfidTag().getStatus() : null)
                        .createdAt(item.getCreatedAt())
                        .quantity(item.getQuantity())
                        .build())
                .toList();
        log.info("Retrieved {} recent items", recentItems.size());

        // Build and return DTO
        return InventoryDashboardDTO.builder()
                .totalItems(totalItems)
                .totalQuantity(totalQuantity)
                .lowStockCount(lowStockCount)
                .itemsByZone(itemsByZone)
                .recentItems(recentItems)
                .build();
    }
}
