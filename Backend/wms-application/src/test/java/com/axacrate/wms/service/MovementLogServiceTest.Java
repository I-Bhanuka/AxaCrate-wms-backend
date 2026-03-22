package com.axacrate.wms.service;

import com.axacrate.wms.dto.MovementLogResponseDTO;
import com.axacrate.wms.entity.*;
import com.axacrate.wms.repository.MovementLogRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * MovementLogServiceTest — 2 essential tests
 *
 * Covers: DTO field mapping correctness, and limit capping (abuse prevention).
 *
 * Run: mvn test -Dtest=MovementLogServiceTest
 */
@ExtendWith(MockitoExtension.class)
class MovementLogServiceTest {

    @Mock MovementLogRepository movementLogRepository;

    @InjectMocks MovementLogService movementLogService;

    private Zone         writerZone, qcZone;
    private RfidHardware reader;
    private InventoryItem linkedItem;
    private MovementLog  movementLog;

    @BeforeEach
    void setUp() {
        writerZone = Zone.builder().id(UUID.randomUUID()).name("Writer Zone")
                .zoneType(Zone.ZoneType.WRITER_ZONE).capacity(100)
                .status(Zone.ZoneStatus.ACTIVE).build();

        qcZone = Zone.builder().id(UUID.randomUUID()).name("QC Zone")
                .zoneType(Zone.ZoneType.QC_ZONE).capacity(50)
                .status(Zone.ZoneStatus.ACTIVE).build();

        reader = RfidHardware.builder().id(UUID.randomUUID()).name("READER-QC")
                .hardwareType(RfidHardware.HardwareType.READER)
                .hardwareStatus(RfidHardware.HardwareStatus.ACTIVE).build();

        linkedItem = InventoryItem.builder().id(UUID.randomUUID()).sku("SKU-001")
                .name("Test Widget").quantity(10).currentZone(writerZone).build();

        RfidTag logTag = RfidTag.builder().id(UUID.randomUUID()).uid("TAG-001")
                .status(RfidTag.RfidStatus.ACTIVE).inventoryItem(linkedItem).build();

        movementLog = MovementLog.builder().id(UUID.randomUUID()).tag(logTag)
                .fromZone(writerZone).toZone(qcZone).hardware(reader)
                .eventType(MovementLog.EventType.MOVEMENT).synced(false)
                .occurredAt(OffsetDateTime.now()).build();
    }

    // M1 — All key fields must map correctly from the entity to the DTO
    @Test
    @DisplayName("M1 — getRecentMovements maps all key DTO fields correctly")
    void getRecentMovements_mapsFieldsCorrectly() {
        when(movementLogRepository.findRecentMovements(
                eq(MovementLog.EventType.MOVEMENT), any()))
                .thenReturn(List.of(movementLog));

        MovementLogResponseDTO dto = movementLogService.getRecentMovements(10).get(0);

        assertThat(dto.getItemName()).isEqualTo("Test Widget");
        assertThat(dto.getFromZoneName()).isEqualTo("Writer Zone");
        assertThat(dto.getToZoneName()).isEqualTo("QC Zone");
        assertThat(dto.getEventType()).isEqualTo("Movement");
        assertThat(dto.getSynced()).isFalse();
    }

    // M2 — Limit capping prevents unbounded DB queries coming from the frontend
    @Test
    @DisplayName("M2 — Limit 0 capped to 1, limit 200 capped to 100 (abuse prevention)")
    void getRecentMovements_limitCapping_bothBoundaries() {
        when(movementLogRepository.findRecentMovements(any(), any())).thenReturn(List.of());
        ArgumentCaptor<PageRequest> cap = ArgumentCaptor.forClass(PageRequest.class);

        movementLogService.getRecentMovements(0);
        verify(movementLogRepository).findRecentMovements(any(), cap.capture());
        assertThat(cap.getValue().getPageSize()).isEqualTo(1);

        clearInvocations(movementLogRepository);

        movementLogService.getRecentMovements(200);
        verify(movementLogRepository).findRecentMovements(any(), cap.capture());
        assertThat(cap.getValue().getPageSize()).isEqualTo(100);
    }
}