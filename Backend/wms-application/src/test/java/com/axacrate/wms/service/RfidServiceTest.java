package com.axacrate.wms.service;

import com.axacrate.wms.dto.*;
import com.axacrate.wms.entity.*;
import com.axacrate.wms.exception.ResourceNotFoundException;
import com.axacrate.wms.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RfidServiceTest — 6 essential tests
 *
 * Covers: all 3 write-scan branches (new/unassigned/assigned), UI polling cache,
 * unknown-tag CRITICAL alert, and successful read side-effects.
 * Offline hardware and denied geofence are integration-level concerns and removed.
 */
@ExtendWith(MockitoExtension.class)
class RfidServiceTest {

    @Mock RfidTagRepository       rfidTagRepository;
    @Mock MovementLogRepository   movementLogRepository;
    @Mock ZoneRepository          zoneRepository;
    @Mock RfidHardwareRepository  hardwareRepository;
    @Mock InventoryItemRepository inventoryItemRepository;
    @Mock AlertRepository         alertRepository;
    @Mock GeofenceService         geofenceService;

    @InjectMocks RfidService rfidService;

    private Zone writerZone, unloadingZone, qcZone;
    private RfidHardware activeReader, activeWriter;
    private RfidTag unassignedTag, assignedTag;
    private InventoryItem linkedItem;

    private static final GeofenceResponseDTO AUTHORIZED =
            GeofenceResponseDTO.builder().authorized(true).statusMessage("OK").build();

    @BeforeEach
    void setUp() {
        writerZone    = zone(Zone.ZoneType.WRITER_ZONE,    "Writer Zone");
        unloadingZone = zone(Zone.ZoneType.UNLOADING_ZONE, "Unloading Zone");
        qcZone        = zone(Zone.ZoneType.QC_ZONE,        "QC Zone");
        activeReader  = hw("READER-QC",   RfidHardware.HardwareType.READER, RfidHardware.HardwareStatus.ACTIVE);
        activeWriter  = hw("Writer Zone", RfidHardware.HardwareType.WRITER, RfidHardware.HardwareStatus.ACTIVE);
        unassignedTag = tag("TAG-FREE",   null);
        linkedItem    = InventoryItem.builder().id(UUID.randomUUID()).sku("SKU-001")
                .name("Test Widget").quantity(10).currentZone(writerZone).build();
        assignedTag   = tag("TAG-USED", linkedItem);
        linkedItem.setRfidTag(assignedTag);
    }

    // R1 — New tag: UNASSIGNED status + TAG_REGISTERED event logged
    @Test
    @DisplayName("R1 — New tag write scan returns UNASSIGNED and logs TAG_REGISTERED event")
    void writeScan_newTag_returnsUnassignedAndLogsRegistration() {
        stubWriteZone();
        when(rfidTagRepository.findByUid("TAG-NEW")).thenReturn(Optional.empty());
        when(rfidTagRepository.save(any())).thenAnswer(i -> {
            RfidTag t = i.getArgument(0);
            return RfidTag.builder().id(UUID.randomUUID()).uid(t.getUid())
                    .status(t.getStatus()).lastSeenZone(t.getLastSeenZone()).build();
        });

        var response = rfidService.handleRfidWriteScan(req("TAG-NEW", "Writer Zone", "Writer Zone"));

        assertThat(response.getStatus()).isEqualTo("UNASSIGNED");
        assertThat(response.isAllowEdit()).isTrue();
        ArgumentCaptor<MovementLog> cap = ArgumentCaptor.forClass(MovementLog.class);
        verify(movementLogRepository).save(cap.capture());
        assertThat(cap.getValue().getEventType()).isEqualTo(MovementLog.EventType.TAG_REGISTERED);
    }

    // R2 — Assigned tag: ASSIGNED status with item details and allowEdit=false
    @Test
    @DisplayName("R2 — Assigned tag write scan returns ASSIGNED with item details, allowEdit=false")
    void writeScan_assignedTag_returnsAssignedWithDetails() {
        stubWriteZone();
        when(rfidTagRepository.findByUid("TAG-USED")).thenReturn(Optional.of(assignedTag));

        var response = rfidService.handleRfidWriteScan(req("TAG-USED", "Writer Zone", "Writer Zone"));

        assertThat(response.getStatus()).isEqualTo("ASSIGNED");
        assertThat(response.isAllowEdit()).isFalse();
        assertThat(response.getSku()).isEqualTo("SKU-001");
        assertThat(response.getInventoryItemId()).isEqualTo(linkedItem.getId());
    }

    // R3 — Latest scan cached for UI polling
    @Test
    @DisplayName("R3 — getLatestWriteScan returns the most recently cached scan")
    void getLatestWriteScan_returnsMostRecent() {
        stubWriteZone();
        when(rfidTagRepository.findByUid(anyString()))
                .thenAnswer(i -> Optional.of(tag(i.getArgument(0), null)));

        rfidService.handleRfidWriteScan(req("TAG-FIRST",  "Writer Zone", "Writer Zone"));
        rfidService.handleRfidWriteScan(req("TAG-SECOND", "Writer Zone", "Writer Zone"));

        assertThat(rfidService.getLatestWriteScan().getTagUid()).isEqualTo("TAG-SECOND");
    }

    // R4 — Unknown tag: CRITICAL alert + exception + no movement log
    @Test
    @DisplayName("R4 — Unknown tag read scan throws, saves UNKNOWN_TAG CRITICAL alert, no movement log")
    void readScan_unknownTag_throwsAndSavesCriticalAlert() {
        when(hardwareRepository.findByName("READER-QC")).thenReturn(Optional.of(activeReader));
        when(zoneRepository.findByNameIgnoreCase("QC Zone")).thenReturn(Optional.of(qcZone));
        when(rfidTagRepository.findByUid("TAG-GHOST")).thenReturn(Optional.empty());
        when(alertRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> rfidService.handleRfidRead(req("TAG-GHOST", "READER-QC", "QC Zone")))
                .isInstanceOf(ResourceNotFoundException.class);

        ArgumentCaptor<Alert> cap = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(cap.capture());
        assertThat(cap.getValue().getAlertType()).isEqualTo(Alert.AlertType.UNKNOWN_TAG);
        assertThat(cap.getValue().getSeverity()).isEqualTo(Alert.Severity.CRITICAL);
        verify(movementLogRepository, never()).save(any());
    }

    // R5 — Successful read: item zone, tag lastSeenZone, and MOVEMENT log all updated
    @Test
    @DisplayName("R5 — Successful read scan updates item zone, tag lastSeenZone, and creates MOVEMENT log")
    void readScan_success_updatesAllThreeSideEffects() {
        when(hardwareRepository.findByName("READER-QC")).thenReturn(Optional.of(activeReader));
        when(zoneRepository.findByNameIgnoreCase("QC Zone")).thenReturn(Optional.of(qcZone));
        when(rfidTagRepository.findByUid("TAG-USED")).thenReturn(Optional.of(assignedTag));
        when(geofenceService.processGeofenceEvent(any(), any(), any(), any())).thenReturn(AUTHORIZED);
        when(rfidTagRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(inventoryItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(movementLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        rfidService.handleRfidRead(req("TAG-USED", "READER-QC", "QC Zone"));

        ArgumentCaptor<InventoryItem> itemCap = ArgumentCaptor.forClass(InventoryItem.class);
        verify(inventoryItemRepository).save(itemCap.capture());
        assertThat(itemCap.getValue().getCurrentZone()).isEqualTo(qcZone);

        ArgumentCaptor<RfidTag> tagCap = ArgumentCaptor.forClass(RfidTag.class);
        verify(rfidTagRepository).save(tagCap.capture());
        assertThat(tagCap.getValue().getLastSeenZone()).isEqualTo(qcZone);

        ArgumentCaptor<MovementLog> logCap = ArgumentCaptor.forClass(MovementLog.class);
        verify(movementLogRepository).save(logCap.capture());
        assertThat(logCap.getValue().getEventType()).isEqualTo(MovementLog.EventType.MOVEMENT);
    }

    // R6 — Offline hardware: early abort, nothing written to DB
    @Test
    @DisplayName("R6 — Offline hardware aborts read with no DB writes")
    void readScan_offlineHardware_abortsWithNoWrites() {
        RfidHardware offlineHw = hw("READER-OFF", RfidHardware.HardwareType.READER,
                RfidHardware.HardwareStatus.INACTIVE);
        when(hardwareRepository.findByName("READER-OFF")).thenReturn(Optional.of(offlineHw));

        rfidService.handleRfidRead(req("TAG-ANY", "READER-OFF", "QC Zone"));

        verify(rfidTagRepository,       never()).findByUid(any());
        verify(movementLogRepository,   never()).save(any());
        verify(inventoryItemRepository, never()).save(any());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void stubWriteZone() {
        lenient().when(zoneRepository.findByNameAndZoneType("Writer Zone", Zone.ZoneType.WRITER_ZONE))
                .thenReturn(Optional.of(writerZone));
        lenient().when(zoneRepository.findByZoneType(Zone.ZoneType.UNLOADING_ZONE))
                .thenReturn(Optional.of(unloadingZone));
        lenient().when(hardwareRepository.findByName("Writer Zone"))
                .thenReturn(Optional.of(activeWriter));
        lenient().when(movementLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(rfidTagRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private RfidReadRequestDTO req(String tagId, String hw, String zone) {
        return new RfidReadRequestDTO(tagId, hw, zone);
    }

    private Zone zone(Zone.ZoneType type, String name) {
        return Zone.builder().id(UUID.randomUUID()).name(name).zoneType(type)
                .capacity(100).status(Zone.ZoneStatus.ACTIVE).build();
    }

    private RfidHardware hw(String name, RfidHardware.HardwareType type, RfidHardware.HardwareStatus status) {
        return RfidHardware.builder().id(UUID.randomUUID()).name(name)
                .hardwareType(type).hardwareStatus(status).build();
    }

    private RfidTag tag(String uid, InventoryItem item) {
        return RfidTag.builder().id(UUID.randomUUID()).uid(uid)
                .status(RfidTag.RfidStatus.ACTIVE).inventoryItem(item).build();
    }
}