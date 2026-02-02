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
    }
}
