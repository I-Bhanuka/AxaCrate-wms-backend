package com.axacrate.wms.service;

import com.axacrate.wms.dto.RfidReadRequestDTO;

import org.springframework.stereotype.Service;

@Service
public class RfidService {

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
        // For testing, just log the data
        System.out.println("RFID WRITE SCAN RECEIVED");
        System.out.println("Tag ID: " + request.getTagId());
        System.out.println("Reader ID: " + request.getReaderId());
        System.out.println(" ");

        // Later on,
        // - find tag
        // - check assignment status
        // - return appropriate response to controller


        // TODO:
        // It will check 3 possibilities when it receives the tag UID from the ESP32:
        //    a. If the tag has data of an inventory item. Then the response status "ASSIGNED" -> UI will show update
        //    b. If the tag is not registered. Register a new item. Then the response status "NEW_TAG" -> UI will show "New tag" then empty form
        //    c. If the tag is registered but not assigned to an inventory item. Then the response status "UNASSIGNED" -> UI will show the empty form
    }
}
