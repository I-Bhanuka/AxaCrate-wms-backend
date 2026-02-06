package com.axacrate.wms.service;

import com.axacrate.wms.dto.RfidReadRequestDTO;

import com.axacrate.wms.entity.MovementLog;
import com.axacrate.wms.entity.RfidHardware;
import com.axacrate.wms.entity.RfidTag;
import com.axacrate.wms.entity.Zone;
import com.axacrate.wms.repository.MovementLogRepository;
import com.axacrate.wms.repository.RfidHardwareRepository;
import com.axacrate.wms.repository.RfidTagRepository;
import com.axacrate.wms.repository.ZoneRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class RfidService {

    // These repositories need to be injected into the service to perform database operations. Otherwise NPE
    private final RfidTagRepository rfidTagRepository;
    private final MovementLogRepository movementLogRepository;
    private final ZoneRepository zoneRepository;
    private final RfidHardwareRepository hardwareRepository;

    public RfidService(RfidTagRepository rfidTagRepository,
                       MovementLogRepository movementLogRepository,
                       ZoneRepository zoneRepository,
                       RfidHardwareRepository hardwareRepository){
        // Injecting repositories through constructor injection. Spring will automatically provide the implementations at runtime.
        this.rfidTagRepository = rfidTagRepository;
        this.movementLogRepository = movementLogRepository;
        this.zoneRepository = zoneRepository;
        this.hardwareRepository = hardwareRepository;
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


    public void handleRfidWriteScan(RfidReadRequestDTO request) {
        // The method will check 3 possibilities when it receives the tag UID from the ESP32:
        //    a. If the tag has data of an inventory item. Then the response status "ASSIGNED" -> UI will show update
        //    b. If the tag is not registered. Register a new item. Then the response status "NEW_TAG" -> UI will show "New tag" then empty form
        //    c. If the tag is registered but not assigned to an inventory item. Then the response status "UNASSIGNED" -> UI will show the empty form

        // Find the tag
        RfidTag rfidTag = rfidTagRepository.findByUid(request.getTagId()).orElse(null);

        Zone writerZone = getWriterZone();

        // Create a new tag in DB if not there
        if (rfidTag == null) {
            rfidTag = rfidTagRepository.save(createTag(request.getTagId()));
            log.info("New tag created!");
            // Query the movement log
            logEvent(rfidTag, request.getReaderId(), writerZone, MovementLog.EventType.TAG_REGISTERED);
            log.info("Write scan event logged for new tag!");
            return;
        }

         // If tag already exists and there no inventory item assigned to it
        if (rfidTag.getInventoryItem() == null) {
            // UNASSIGNED TAG
            log.info("Unassigned tag scanned!");
            logEvent(rfidTag, request.getReaderId(), writerZone, MovementLog.EventType.UNASSIGNED);
            log.info("Tag registered event logged for unassigned tag!");
        } else {
            // ASSIGNED TAG
            log.info("Assigned tag scanned!");
            logEvent(rfidTag, request.getReaderId(), writerZone, MovementLog.EventType.ASSIGNED);
            log.info("Movement event logged for assigned tag!");
        }
    }

    // Helper Methods
    private RfidTag createTag(String uid){
        return RfidTag.builder().
                uid(uid).
                status(RfidTag.RfidStatus.ACTIVE).
                lastSeenAt(LocalDateTime.now()).
                lastSeenZone(getWriterZone()).
                build();
    }

    private Zone getWriterZone() {
        // Fetch WRITER_ZONE object from DB
        return zoneRepository.findByZoneType(Zone.ZoneType.WRITER_ZONE)
                .orElseThrow(() -> new RuntimeException("WRITER_ZONE not found"));
    }

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

    private void logEvent(RfidTag tag, String readerName, Zone zone, MovementLog.EventType eventType) {
        // This method will log the movement event into the MovementLog table

        MovementLog log = MovementLog.builder().
                tag(tag).
                fromZone(null).toZone(zone).
                hardware(hardwareCheck(readerName)).
                eventType(eventType).
                synced(false).
                build();

        movementLogRepository.save(log);
    }
}
