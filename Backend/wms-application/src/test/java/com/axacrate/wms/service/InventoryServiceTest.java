package com.axacrate.wms.service;

import com.axacrate.wms.dto.InventoryItemRequestDTO;
import com.axacrate.wms.entity.*;
import com.axacrate.wms.exception.BusinessRuleViolationException;
import com.axacrate.wms.exception.ResourceNotFoundException;
import com.axacrate.wms.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * InventoryServiceTest — 5 essential tests
 *
 * Covers: item creation (valid tag, already-assigned tag, unknown tag),
 * sort-field injection guard, and delete-unlinks-tag side effect.
 *
 * Run: mvn test -Dtest=InventoryServiceTest
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock InventoryItemRepository inventoryItemRepo;
    @Mock RfidTagRepository       rfidTagRepository;
    @Mock ZoneRepository          zoneRepository;
    @Mock RfidService             rfidService;


    @InjectMocks InventoryService inventoryService;

    private Zone          writerZone;
    private RfidTag       unassignedTag, assignedTag;
    private InventoryItem linkedItem;

    @BeforeEach
    void setUp() {
        writerZone = Zone.builder()
                .id(UUID.randomUUID()).name("Writer Zone")
                .zoneType(Zone.ZoneType.WRITER_ZONE).capacity(100)
                .status(Zone.ZoneStatus.ACTIVE).build();

        linkedItem = InventoryItem.builder()
                .id(UUID.randomUUID()).sku("SKU-001")
                .name("Test Widget").quantity(10).currentZone(writerZone).build();

        unassignedTag = RfidTag.builder()
                .id(UUID.randomUUID()).uid("TAG-FREE")
                .status(RfidTag.RfidStatus.ACTIVE).build();

        assignedTag = RfidTag.builder()
                .id(UUID.randomUUID()).uid("TAG-USED")
                .status(RfidTag.RfidStatus.ACTIVE).inventoryItem(linkedItem).build();
        linkedItem.setRfidTag(assignedTag);
    }

    // I1 — Happy path: item saved, tag linked
    @Test
    @DisplayName("I1 — Create item with valid unassigned tag saves item and links tag")
    void createItem_validTag_savesAndLinksTag() {
        when(rfidTagRepository.findByUid("TAG-FREE")).thenReturn(Optional.of(unassignedTag));
        when(zoneRepository.findByNameAndZoneType("Writer Zone", Zone.ZoneType.WRITER_ZONE))
                .thenReturn(Optional.of(writerZone));
        when(inventoryItemRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        inventoryService.createInventoryItem(
                new InventoryItemRequestDTO("SKU-NEW", "New Widget", 5, "TAG-FREE", "Writer Zone"));

        ArgumentCaptor<InventoryItem> cap = ArgumentCaptor.forClass(InventoryItem.class);
        verify(inventoryItemRepo).save(cap.capture());
        assertThat(cap.getValue().getSku()).isEqualTo("SKU-NEW");
        assertThat(unassignedTag.getInventoryItem()).isNotNull();
    }

    // I2 — Already-assigned tag must be rejected before any save
    @Test
    @DisplayName("I2 — Already-assigned tag throws BusinessRuleViolationException, save skipped")
    void createItem_assignedTag_throwsAndSkipsSave() {
        when(rfidTagRepository.findByUid("TAG-USED")).thenReturn(Optional.of(assignedTag));

        assertThatThrownBy(() -> inventoryService.createInventoryItem(
                new InventoryItemRequestDTO("SKU-X", "Widget", 1, "TAG-USED", "Writer Zone")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("already assigned");
        verify(inventoryItemRepo, never()).save(any());
    }

    // I3 — Unknown tag UID must throw, not silently create a dangling item
    @Test
    @DisplayName("I3 — Unknown tag UID throws ResourceNotFoundException")
    void createItem_unknownTag_throwsNotFound() {
        when(rfidTagRepository.findByUid("TAG-GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.createInventoryItem(
                new InventoryItemRequestDTO("SKU-X", "Widget", 1, "TAG-GHOST", "Writer Zone")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // I4 — Injected sort field must fall back to createdAt, not reach the DB
    @Test
    @DisplayName("I4 — Invalid sort field falls back to createdAt (SQL injection guard)")
    void getAllItems_invalidSort_fallsBackToCreatedAt() {
        when(inventoryItemRepo.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        assertThatNoException().isThrownBy(() ->
                inventoryService.getAllItems(0, 10, "injected_field,asc", null, null, null, null));

        ArgumentCaptor<Pageable> cap = ArgumentCaptor.forClass(Pageable.class);
        verify(inventoryItemRepo).findAll(any(Specification.class), cap.capture());
        assertThat(cap.getValue().getSort().getOrderFor("createdAt")).isNotNull();
    }

    // I5 — Delete must unassign the tag first, otherwise the tag is permanently orphaned
    @Test
    @DisplayName("I5 — Delete item unassigns its RFID tag before deletion")
    void deleteItem_unassignsTagFirst() {
        when(inventoryItemRepo.findBySku("SKU-001")).thenReturn(Optional.of(linkedItem));
        when(rfidTagRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        inventoryService.deleteItemBySku("SKU-001");

        ArgumentCaptor<RfidTag> cap = ArgumentCaptor.forClass(RfidTag.class);
        verify(rfidTagRepository).save(cap.capture());
        assertThat(cap.getValue().getInventoryItem()).isNull();
        verify(inventoryItemRepo).delete(linkedItem);
    }
}
