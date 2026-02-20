package com.axacrate.wms.service;

import com.axacrate.wms.dto.ZoneCreateDTO;
import com.axacrate.wms.dto.ZoneResponseDTO;
import com.axacrate.wms.entity.Warehouse;
import com.axacrate.wms.entity.Zone;
import com.axacrate.wms.repository.InventoryItemRepository;
import com.axacrate.wms.repository.WarehouseRepository;
import com.axacrate.wms.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.axacrate.wms.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ZoneService {
    private final ZoneRepository zoneRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository inventoryItemRepository;
    // private final RfidHardwareRepository rfidHardwareRepository;

    // Creating a zone
    @Transactional
    public ZoneResponseDTO createZone(ZoneCreateDTO dto) {

        // Checking if the warehouse exists
        Warehouse warehouse = warehouseRepository.findByName(dto.getWarehouseName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Warehouse not found: " + dto.getWarehouseName())
                );

        // Checking is the zone name already exists in the warehouse
        zoneRepository.findByNameAndWarehouseId(dto.getName(), warehouse.getId())
                .ifPresent(existingZone -> {
                    throw new IllegalArgumentException(
                            "Zone with name '" + dto.getName() + "' already exists in warehouse: " + warehouse.getName()
                    );
                });

        // Convert zone type from DTO
        Zone.ZoneType zoneTypeEnum = Zone.ZoneType.valueOf(dto.getZoneType());

        // Convert status from DTO
        Zone.ZoneStatus statusEnum = "INACTIVE".equalsIgnoreCase(dto.getStatus()) ? Zone.ZoneStatus.INACTIVE : Zone.ZoneStatus.ACTIVE;

        // Build zone entity
        Zone zone = Zone.builder()
                .name(dto.getName())
                .zoneType(zoneTypeEnum)
                .capacity(dto.getCapacity())
                .warehouse(warehouse)
                .status(statusEnum)
                .build();

        // Save zone
        Zone savedZone = zoneRepository.save(zone);

        // Return response DTO
        return mapToResponseDTO(savedZone);
    }

    // Retrieving all the zones and mapping them to the ZoneResponseDTO
    public List<ZoneResponseDTO> getAllZones() {

        List<Zone> zones = zoneRepository.findAll();

        return zones.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    // Retrieving the zone by the Zone ID
    public ZoneResponseDTO getZoneById(UUID zoneId) {
        // Throwing an exception if not the zone is not found using ID
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + zoneId));

        // Mapping the zone entity to the DTO
        return mapToResponseDTO(zone);
    }

    // Retrieving the zone by the Zone name
    public ZoneResponseDTO getZoneByName(String name) {

        Zone zone = zoneRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with name: " + name));

        return mapToResponseDTO(zone);
    }

    // Mapper to map the zone entity to the ZoneResponseDTO
    private ZoneResponseDTO mapToResponseDTO(Zone zone) {
        Long itemCount = inventoryItemRepository.countItemsInZone(zone.getId());

        return ZoneResponseDTO.builder()
                .id(zone.getId())
                .name(zone.getName())
                .zoneType(zone.getZoneType().name())
                .warehouseName(zone.getWarehouse().getName())
                .capacity(zone.getCapacity())
                .status(zone.getStatus().name())
                .currentItemCount(itemCount != null ? itemCount.intValue() : 0)
                .hasHardware(zone.hasHardware())
                .hardwareName(zone.getHardwareName())
                .hardwareType(zone.getHardwareType())
                .hardwareStatus(zone.getHardwareStatus())
                .build();
    }
}
