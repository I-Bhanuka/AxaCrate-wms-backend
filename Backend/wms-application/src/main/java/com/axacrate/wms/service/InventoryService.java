package com.axacrate.wms.service;

import com.axacrate.wms.dto.InventoryItemRequestDTO;
import com.axacrate.wms.entity.InventoryItem;
import com.axacrate.wms.entity.RfidTag;
import com.axacrate.wms.entity.Zone;
import com.axacrate.wms.repository.InventoryItemRepository;
import com.axacrate.wms.repository.RfidTagRepository;
import com.axacrate.wms.repository.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

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


    public void createInventoryItem(InventoryItemRequestDTO requestDTO) {


        // Find RFID Tag entity by UID
        RfidTag rfidTag = rfidTagRepository.findByUid(requestDTO.getRfidTag()).
                orElseThrow(() -> new IllegalArgumentException("RFID Tag not found"));

        log.info("RFID Tag found: " + rfidTag.getUid());

        // Find Writer Zone
        Zone zoneId = zoneRepository.findByZoneType(Zone.ZoneType.WRITER_ZONE).
                orElseThrow(() -> new IllegalArgumentException("Writer zone not found"));

        log.info("Zone found: " + zoneId);

        // Create Inventory Item entity
        InventoryItem entity = InventoryItem.builder()
                .sku(requestDTO.getSku())
                .name(requestDTO.getName())
                .quantity(requestDTO.getQuantity())
                .currentZone(zoneId)
                .rfidTag(rfidTag)
                .build();

        // Save entity to database
        inventoryItemRepo.save(entity);

        //TODO:
        // later send data to item id to esp32 for rfid tag assignment
    }
}
