package com.axacrate.wms.service;

import com.axacrate.wms.dto.RfidReadRequestDTO;
import com.axacrate.wms.dto.RfidWriteScanResponseDTO;
import com.axacrate.wms.exception.ResourceNotFoundException;

import com.axacrate.wms.entity.*;
import com.axacrate.wms.repository.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
@Service
public class RfidService {

    // These repositories need to be injected into the service to perform database operations. Otherwise NPE
    private final RfidTagRepository rfidTagRepository;
    private final MovementLogRepository movementLogRepository;
    private final ZoneRepository zoneRepository;
    private final RfidHardwareRepository hardwareRepository;
    private final InventoryItemRepository inventoryItemRepository;

    Deque<RfidWriteScanResponseDTO> latestScans;


    public RfidService(RfidTagRepository rfidTagRepository,
                       MovementLogRepository movementLogRepository,
                       ZoneRepository zoneRepository,
                       RfidHardwareRepository hardwareRepository,
                       InventoryItemRepository inventoryItemRepository) {
        // Injecting repositories through constructor injection. Spring will automatically provide the implementations at runtime.
        this.rfidTagRepository = rfidTagRepository;
        this.movementLogRepository = movementLogRepository;
        this.zoneRepository = zoneRepository;
        this.hardwareRepository = hardwareRepository;
        this.inventoryItemRepository = inventoryItemRepository;

        this.latestScans = new ConcurrentLinkedDeque<>();;
    }

    public void handleRfidRead(RfidReadRequestDTO request) {

        // For testing, just log the data
        System.out.println("RFID READ RECEIVED");
        System.out.println("Tag ID: " + request.getTagId());
        System.out.println("Reader ID: " + request.getReaderId());
        System.out.println(" ");

        // Later on,
        // - find reader
        // - find tag
        // - validate movement
        // - log movement
        // - raise alerts

        log.info("Read scan received from reader: {}, for tag: {}", request.getReaderId(), request.getTagId());

        // Find the tag
        RfidTag rfidTag = rfidTagRepository.findByUid(request.getTagId()).orElse(null);

        // Find the item
        InventoryItem item = rfidTag != null ? rfidTag.getInventoryItem() : null;

        Zone readerZone = zoneRepository.findByNameIgnoreCase(request.getReaderId()).
                orElseThrow(() -> new ResourceNotFoundException("Reader zone not found"));

        if (rfidTag == null) {
            log.warn("Tag not found for UID: {}. Read event ignored.", request.getTagId());
            return; // Or you can choose to log this as an unregistered tag event
        }

        log.info("Tag found: {}. Processing read event.", rfidTag.getUid());

        // Change the tag's last seen location and time
        rfidTag.setLastSeenAt(LocalDateTime.now());
        rfidTag.setLastSeenZone(readerZone);
        rfidTagRepository.save(rfidTag);

        // Create a movement log entry
        MovementLog movementLog = MovementLog.builder()
                .tag(rfidTag)
                .fromZone(item.getCurrentZone())
                .toZone(readerZone)
                .hardware(hardwareCheck(request.getReaderId()))
                .eventType(MovementLog.EventType.MOVEMENT)
                .synced(false)
                .build();

        movementLogRepository.save(movementLog);
        log.info("Movement event logged for tag: {}. From zone: {} to zone: {}.", rfidTag.getUid(), item.getCurrentZone() != null ? item.getCurrentZone().getName() : "UNASSIGNED", readerZone.getName());

        // Change item's current zone if item is assigned to the tag
        if (item != null) {
            item.setCurrentZone(readerZone);
            // Save the item entity if you have an inventory item repository
            inventoryItemRepository.save(item);
            log.info("Inventory item {} moved to zone {}.", item.getName(), readerZone.getName());
        }






        // TODO:
        // This method can be separated for movement where it will,
        // 1. Find RFID Tag
        // 2. Update its last seen location based on reader
        // 3. Log movement event
        // 4. Check for geofencing violations
        // 5. Raise alerts if necessary
        // 6. Update inventory status if needed
        // 7. Return success / failure
    }

    @Transactional
    public RfidWriteScanResponseDTO handleRfidWriteScan(RfidReadRequestDTO request) {
        // The method will check 3 possibilities when it receives the tag UID from the ESP32:
        //    a. If the tag has data of an inventory item. Then the response status "ASSIGNED" -> UI will show update
        //    b. If the tag is not registered. Register a new item. Then the response status "NEW_TAG" -> UI will show "New tag" then empty form
        //    c. If the tag is registered but not assigned to an inventory item. Then the response status "UNASSIGNED" -> UI will show the empty form

        log.info("Write scan received from reader: {}, for tag: {}", request.getReaderId(), request.getTagId());

        // Find the tag
        RfidTag rfidTag = rfidTagRepository.findByUid(request.getTagId()).orElse(null);

        Zone writerZone = getWriterZone(request.getReaderId());

        // Create a new tag in DB if not there
        if (rfidTag == null) {
            log.info("New tag detected: {}. Creating...", request.getTagId());
            rfidTag = rfidTagRepository.save(createTag(request.getTagId(), request.getReaderId()));
            log.info("New tag created: {}", rfidTag.getUid());
            // Query the movement log
            logEvent(rfidTag, request.getReaderId(), writerZone, MovementLog.EventType.TAG_REGISTERED);
            log.info("Movement log logged a new tag registration!");
        } else {

            // If tag already exists and there no inventory item assigned to it
            if (rfidTag.getInventoryItem() == null) {
                // UNASSIGNED TAG
                log.info("Unassigned tag scanned: {}. Logging unassigned event.", rfidTag.getUid());
                logEvent(rfidTag, request.getReaderId(), writerZone, MovementLog.EventType.UNASSIGNED);
                log.info("Movement event logged for unassigned tag!");
            } else {
                // ASSIGNED TAG
                log.info("Assigned tag scanned: {}. Logging assigned event.", rfidTag.getUid());
                logEvent(rfidTag, request.getReaderId(), writerZone, MovementLog.EventType.ASSIGNED);
                log.info("Movement event logged for assigned tag!");
            }
        }

        // Build the response based on the tag status and inventory item assignment
        RfidWriteScanResponseDTO response = buildWriteScanResponse(rfidTag, writerZone);

        // Cache the results for UI to retrieve via /write-latest endpoint
        latestScans.addLast(response);
        log.info("Write scan response cached for tag: {}", rfidTag.getUid());

        log.info("Write scan response: {}", response.getStatus());
        return response;
    }

    public RfidWriteScanResponseDTO getLatestWriteScan() {
        // This method will return the latest write scan result for the UI to display.

        // For simplicity, we will return the latest scan for a single reader (assuming only one writer zone for now)

        if (latestScans.isEmpty()) {
            log.warn("No write scans found in cache.");
            return null; // Or return a default response indicating no scans
        }

        // Get the latest scan (for simplicity, we take the first entry in the map)
        RfidWriteScanResponseDTO latestScan = latestScans.peekLast();
        log.info("Latest write scan retrieved: {}", latestScan.getTagUid());
        return latestScan;
    }

    // Helper Methods

    // HELPER: Create new RFID tag entry in the database
    private RfidTag createTag(String uid, String readerName){
        return RfidTag.builder().
                uid(uid).
                status(RfidTag.RfidStatus.ACTIVE).
                lastSeenAt(LocalDateTime.now()).
                lastSeenZone(getWriterZone(readerName)).
                build();
    }

    // HELPER: Get the writer zone information
    private Zone getWriterZone(String readerName) {
        // Fetch WRITER_ZONE object from DB
        return zoneRepository.findByNameAndZoneType(readerName, Zone.ZoneType.WRITER_ZONE)
                .orElseThrow(() -> new ResourceNotFoundException("WRITER_ZONE not found"));
    }

    // HELPER: Check hardware availability and status
    private RfidHardware hardwareCheck(String readerName) {
        // This method can be used for both read and write events

        // This method will:
        // 1. Find the hardware based on reader name from the request
        // 2. Check if the hardware is active and operational
        // 3. If not, log an error and return without processing the event

        RfidHardware hardware = hardwareRepository.findByName
                (readerName).orElse(null);

        // Creating a new hardware if not found
        if (hardware == null) {
            hardware = hardwareRepository.save(createHardware(readerName, RfidHardware.HardwareType.WRITER));
            log.info("New Hardware created! Name: {}", readerName);
        }

        // Check if hardware is operational
        if (!hardware.isOperational()) {
            log.error("Hardware {} is not operational. Event processing aborted.", readerName);
            return null;
        }

        return hardware;
    }

    // HELPER: Create new hardware entry
    private RfidHardware createHardware(String name, RfidHardware.HardwareType type) {
        // This method will create a new writer hardware entry.

        return RfidHardware.builder().
                id(null).
                name(name).
                hardwareType(type).
                hardwareStatus(RfidHardware.HardwareStatus.ACTIVE).
                zoneLocation(null).
                build();
    }

    // HELPER: Log movement event
    private void logEvent(RfidTag tag, String readerName, Zone zone, MovementLog.EventType eventType) {
        // This method will log the movement event into the MovementLog table

        MovementLog log = MovementLog.builder().
                tag(tag).
                fromZone(null).
                toZone(zone).
                hardware(hardwareCheck(readerName)).
                eventType(eventType).
                synced(false).
                build();

        movementLogRepository.save(log);

        // Update the last seen location and save the tag entity after logging the event
        tag.setLastSeenAt(LocalDateTime.now());
        tag.setLastSeenZone(zone);
        rfidTagRepository.save(tag);
    }

    // HELPER: Build Write Scan Response
    private RfidWriteScanResponseDTO buildWriteScanResponse(RfidTag tag, Zone writerZone) {
        RfidWriteScanResponseDTO.RfidWriteScanResponseDTOBuilder builder = RfidWriteScanResponseDTO.builder()
                .tagUid(tag.getUid())
                .tagStatus(tag.getStatus().toString());

        if (tag.getInventoryItem() == null) {
            return builder
                    .status("UNASSIGNED")
                    .message("Tag is unassigned. You can create a new inventory item.")
                    .allowEdit(true)
                    .currentZone(writerZone.getName())
                    .build();
        } else {
            InventoryItem item = tag.getInventoryItem();
            return builder
                    .status("ASSIGNED")
                    .message("Tag is assigned to an inventory item.")
                    .allowEdit(false)
                    .inventoryItemId(item.getId())
                    .sku(item.getSku())
                    .itemName(item.getName())
                    .quantity(item.getQuantity())
                    .currentZone(writerZone.getName())
                    .build();
        }
    }
}
