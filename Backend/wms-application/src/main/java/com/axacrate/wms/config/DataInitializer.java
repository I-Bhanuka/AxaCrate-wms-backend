package com.axacrate.wms.config;

import com.axacrate.wms.entity.*;
import com.axacrate.wms.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Data Initializer
 *
 * Runs automatically when application starts
 * Creates initial data:
 * - Default warehouse
 * - All zones (UNLOADING, WRITER, QC, STORAGE, DISPATCH)
 * - RFID hardware in each zone
 * - Admin user
 *
 * This only runs ONCE on first startup (checks if data exists)
 */
@Transactional
@Component // Makes this a Spring-managed bean
@RequiredArgsConstructor // Lombok generates constructor with all final fields (dependency injection)
@Slf4j //  Lombok provides logger (log.info(), log.warn(), ... )
public class DataInitializer implements CommandLineRunner { // CommandLineRunner - Interface that runs code after Spring Boot starts

    // Inject all repositories (using Lombok @RequiredArgsConstructor)
    private final WarehouseRepository warehouseRepository;
    private final ZoneRepository zoneRepository;
    private final RfidHardwareRepository rfidHardwareRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * This method runs when application starts
     *
     * @param args - Command line arguments (not used)
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("===========================================");
        log.info("Starting data initialization...");
        log.info("===========================================");

        initializeWarehouse();
        initializeAdminUser();

        log.info("===========================================");
        log.info("Data initialization completed successfully!");
        log.info("===========================================");
    }

    /**
     * Create default warehouse with all zones and hardware
     */
    private void initializeWarehouse() {
        Optional<Warehouse> existingWarehouse = warehouseRepository.findByName("Main Warehouse");

        Warehouse warehouse;
        if (existingWarehouse.isPresent()) {
            warehouse = existingWarehouse.get();
            log.info("✓ Warehouse already exists. Checking zones and hardware...");

            // Check each zone, create if missing
            createZoneIfNotExists(warehouse, "Unloading Bay", Zone.ZoneType.UNLOADING_ZONE, 100);
            Zone writerZone = createZoneIfNotExists(warehouse, "Tag Writing Station", Zone.ZoneType.WRITER_ZONE, 50);
            Zone qcZone = createZoneIfNotExists(warehouse, "Quality Control", Zone.ZoneType.QC_ZONE, 200);
            Zone storageZone = createZoneIfNotExists(warehouse, "Main Storage", Zone.ZoneType.STORAGE_ZONE, 500);
            Zone dispatchZone = createZoneIfNotExists(warehouse, "Dispatch Area", Zone.ZoneType.DISPATCH_ZONE, 100);

            // Create hardware for zones if missing
            createRfidHardwareIfNotExists(writerZone, RfidHardware.HardwareType.WRITER);
            createRfidHardwareIfNotExists(qcZone, RfidHardware.HardwareType.READER);
            createRfidHardwareIfNotExists(storageZone, RfidHardware.HardwareType.READER);
            createRfidHardwareIfNotExists(dispatchZone, RfidHardware.HardwareType.READER);

            return;
        }

        log.info("Creating Main Warehouse...");

        // Create warehouse
        warehouse = new Warehouse();
        warehouse.setName("Main Warehouse");
        warehouse.setAddress("123 Industrial Zone, Colombo, Sri Lanka");
        warehouse.setTotalCapacity(10000);

        warehouse = warehouseRepository.save(warehouse);
        log.info("✓ Created warehouse: {}", warehouse.getName());

        // Create zones
        Zone unloadingZone = createZone(warehouse, "Unloading Bay", Zone.ZoneType.UNLOADING_ZONE, 100);
        Zone writerZone = createZone(warehouse, "Tag Writing Station", Zone.ZoneType.WRITER_ZONE, 50);
        Zone qcZone = createZone(warehouse, "Quality Control", Zone.ZoneType.QC_ZONE, 200);
        Zone storageZone = createZone(warehouse, "Main Storage", Zone.ZoneType.STORAGE_ZONE, 500);
        Zone dispatchZone = createZone(warehouse, "Dispatch Area", Zone.ZoneType.DISPATCH_ZONE, 100);

        // Create RFID hardware for each zone (except unloading - no hardware there)
        createRfidHardware(writerZone, RfidHardware.HardwareType.WRITER, "Tag writer for incoming pallets");
        createRfidHardware(qcZone, RfidHardware.HardwareType.READER, "QC zone reader");
        createRfidHardware(storageZone, RfidHardware.HardwareType.READER, "Storage zone reader");
        createRfidHardware(dispatchZone, RfidHardware.HardwareType.READER, "Dispatch gate reader");

        log.info("✓ Warehouse setup completed with {} zones and {} hardware devices", 5, 4);
    }

    /**
     * Create a zone and save to database
     *
     * @param warehouse - Parent warehouse
     * @param name - Zone name
     * @param zoneType - Type of zone
     * @param capacity - Maximum capacity
     * @return Created zone
     */
    private Zone createZone(Warehouse warehouse, String name, Zone.ZoneType zoneType, int capacity) {
        Zone zone = new Zone();
        zone.setName(name);
        zone.setZoneType(zoneType);
        zone.setCapacity(capacity);
        zone.setWarehouse(warehouse);  // This also adds zone to warehouse.zones list

        zone = zoneRepository.save(zone);
        log.info("  ✓ Created zone: {} ({})", zone.getName(), zone.getZoneType());
        return zone;
    }

    /**
     * Create RFID hardware and assign to zone
     *
     * @param zone - Zone where hardware is located
     * @param hardwareType - READER or WRITER
     * @param description - Description (for logging)
     */
    private void createRfidHardware(Zone zone, RfidHardware.HardwareType hardwareType, String description) {
        RfidHardware hardware = new RfidHardware();
        hardware.setName(zone.getName() + "-" + hardwareType.name());
        hardware.setHardwareType(hardwareType);
        hardware.setHardwareStatus(RfidHardware.HardwareStatus.ACTIVE);
        hardware.setZoneLocation(zone);  // Assign to zone

        rfidHardwareRepository.save(hardware);
        log.info("  ✓ Created {} in zone: {} ({})", hardwareType, zone.getName(), description);
    }

    /**
     * Create default admin user
     */
    private void initializeAdminUser() {
        // Check if admin already exists
        if (appUserRepository.existsByUsername("admin")) {
            log.info("✓ Admin user already exists. Skipping creation.");
            return;
        }

        log.info("Creating admin user...");

        AppUser admin = new AppUser();
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setPhoneNumber("0771234567");
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));  // Hash the password!
        admin.setRole(AppUser.UserRole.ADMIN);

        appUserRepository.save(admin);

        log.info("✓ Created admin user");
        log.info("  Username: admin");
        log.info("  Password: admin123");
        log.warn("  WARNING: Please change the default admin password in production!");
    }


    // Helper methods to check if zones and hardware already exist (to avoid duplicates on restart)
    private Zone createZoneIfNotExists(Warehouse warehouse, String name, Zone.ZoneType zoneType, int capacity) {
        return zoneRepository.findByNameAndWarehouseId(name, warehouse.getId())
                .orElseGet(() -> createZone(warehouse, name, zoneType, capacity));
    }

    private void createRfidHardwareIfNotExists(Zone zone, RfidHardware.HardwareType hardwareType) {
        boolean exists = rfidHardwareRepository
                .findByZoneAndType(zone.getId(), hardwareType)
                .isPresent();

        if (exists) {
            return;
        }
        createRfidHardware(zone, hardwareType, "Auto-created hardware");
    }

}