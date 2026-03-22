package com.axacrate.wms.service;

import com.axacrate.wms.dto.ZoneCreateDTO;
import com.axacrate.wms.dto.ZoneResponseDTO;
import com.axacrate.wms.dto.ZoneUpdateDTO;
import com.axacrate.wms.entity.Warehouse;
import com.axacrate.wms.entity.Zone;
import com.axacrate.wms.repository.InventoryItemRepository;
import com.axacrate.wms.repository.WarehouseRepository;
import com.axacrate.wms.repository.ZoneRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ZoneServiceTest — 4 essential tests
 *
 * Covers: create zone (happy path), duplicate name guard,
 * capacity-below-items guard, and disable-with-items guard.
 *
 * Run: mvn test -Dtest=ZoneServiceTest
 */
@ExtendWith(MockitoExtension.class)
class ZoneServiceTest {

    @Mock ZoneRepository          zoneRepository;
    @Mock WarehouseRepository     warehouseRepository;
    @Mock InventoryItemRepository inventoryItemRepo;

    @InjectMocks ZoneService zoneService;

    private Warehouse mainWarehouse;
    private Zone      writerZone, storageZone;

    @BeforeEach
    void setUp() {
        mainWarehouse = Warehouse.builder()
                .id(UUID.randomUUID()).name("Main Warehouse").build();

        writerZone = Zone.builder()
                .id(UUID.randomUUID()).name("Writer Zone")
                .zoneType(Zone.ZoneType.WRITER_ZONE).capacity(100)
                .status(Zone.ZoneStatus.ACTIVE).build();
        writerZone.setWarehouse(mainWarehouse);

        storageZone = Zone.builder()
                .id(UUID.randomUUID()).name("Storage Zone")
                .zoneType(Zone.ZoneType.STORAGE_ZONE).capacity(200)
                .status(Zone.ZoneStatus.ACTIVE).build();
        storageZone.setWarehouse(mainWarehouse);

        lenient().when(inventoryItemRepo.countItemsInZone(any())).thenReturn(0L);
    }

    // Z1 — Happy path: zone saved, warehouse name in response DTO
    @Test
    @DisplayName("Z1 — Create zone in existing warehouse returns correct DTO")
    void createZone_valid_returnsCorrectDTO() {
        when(warehouseRepository.findByName("Main Warehouse")).thenReturn(Optional.of(mainWarehouse));
        when(zoneRepository.findByNameAndWarehouseId("QC Zone", mainWarehouse.getId()))
                .thenReturn(Optional.empty());
        when(zoneRepository.save(any())).thenAnswer(i -> {
            Zone z = i.getArgument(0);
            z.setWarehouse(mainWarehouse);
            return z;
        });

        ZoneResponseDTO result = zoneService.createZone(ZoneCreateDTO.builder()
                .name("QC Zone").zoneType("QC_ZONE")
                .warehouseName("Main Warehouse").capacity(50).status("ACTIVE").build());

        assertThat(result.getName()).isEqualTo("QC Zone");
        assertThat(result.getWarehouseName()).isEqualTo("Main Warehouse");
    }

    // Z2 — Duplicate name per warehouse must be caught before save
    @Test
    @DisplayName("Z2 — Duplicate zone name in same warehouse throws, save never called")
    void createZone_duplicateName_throwsAndSkipsSave() {
        when(warehouseRepository.findByName("Main Warehouse")).thenReturn(Optional.of(mainWarehouse));
        when(zoneRepository.findByNameAndWarehouseId("Writer Zone", mainWarehouse.getId()))
                .thenReturn(Optional.of(writerZone));

        assertThatThrownBy(() -> zoneService.createZone(ZoneCreateDTO.builder()
                .name("Writer Zone").zoneType("WRITER_ZONE")
                .warehouseName("Main Warehouse").capacity(50).status("ACTIVE").build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists in warehouse");
        verify(zoneRepository, never()).save(any());
    }

    // Z3 — Reducing capacity below live item count would strand those items
    @Test
    @DisplayName("Z3 — Cannot reduce zone capacity below current item count")
    void updateZone_capacityBelowItemCount_throws() {
        when(zoneRepository.findById(storageZone.getId())).thenReturn(Optional.of(storageZone));
        when(inventoryItemRepo.countItemsInZone(storageZone.getId())).thenReturn(20L);

        assertThatThrownBy(() -> zoneService.updateZone(
                storageZone.getId(), ZoneUpdateDTO.builder().capacity(10).build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("20");
    }

    // Z4 — Disabling an occupied zone would orphan its inventory items
    @Test
    @DisplayName("Z4 — Cannot disable zone that has active inventory items")
    void disableZone_withItems_throws() {
        when(zoneRepository.findByWarehouseNameIgnoreCaseAndNameIgnoreCase(
                "Main Warehouse", "Storage Zone")).thenReturn(Optional.of(storageZone));
        when(inventoryItemRepo.countItemsInZone(storageZone.getId())).thenReturn(5L);

        assertThatThrownBy(() ->
                zoneService.disableZoneByWarehouseAndName("Main Warehouse", "Storage Zone"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5");
    }
}
