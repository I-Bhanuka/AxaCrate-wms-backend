package com.axacrate.wms.service;

import com.axacrate.wms.dto.InventoryItemRequestDTO;
import com.axacrate.wms.entity.InventoryItem;
import com.axacrate.wms.entity.RfidTag;
import com.axacrate.wms.repository.InventoryItemRepository;
import com.axacrate.wms.repository.RfidTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;


@Service
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepo;
    private final RfidTagRepository rfidTagRepository;

    @Autowired
    public InventoryService(InventoryItemRepository inventoryItemRepo, RfidTagRepository rfidTagRepository) {
        this.inventoryItemRepo = inventoryItemRepo;
        this.rfidTagRepository = rfidTagRepository;
    }


    public void createInventoryItem(InventoryItemRequestDTO requestDTO) {

        System.out.println("Creating inventory item...");
        System.out.println("Inventory SKU: " + requestDTO.getSku());
        System.out.println("Inventory Name: " + requestDTO.getName());
        System.out.println("Inventory Quantity: " + requestDTO.getQuantity());
        System.out.println("Inventory item created successfully.");




        // later send to repository to save in DB
        // later send data to item id to esp32 for rfid tag assignment
    }
}
