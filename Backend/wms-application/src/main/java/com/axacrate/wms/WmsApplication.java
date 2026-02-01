package com.axacrate.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * RFID-based Warehouse Management System
 * Main Application Entry Point
 *
 * This application manages:
 * - Warehouse zones and inventory
 * - RFID tag reading and writing via ESP32
 * - Movement tracking and geofencing
 * - Alert generation for unauthorized movements
 * - User authentication and authorization
 *
 * @author RFID WMS Team
 * @version 1.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class WmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsApplication.class, args);
        System.out.println("===========================================");
        System.out.println("RFID WMS Backend Server Started Successfully");
        System.out.println("API Documentation: http://localhost:8080");
        System.out.println("===========================================");
    }

}
