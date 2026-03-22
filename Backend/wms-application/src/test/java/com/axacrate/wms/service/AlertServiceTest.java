package com.axacrate.wms.service;

import com.axacrate.wms.dto.AlertResponseDTO;
import com.axacrate.wms.dto.CreateAlertDTO;
import com.axacrate.wms.entity.*;
import com.axacrate.wms.enums.Role;
import com.axacrate.wms.repository.AlertRepository;
import com.axacrate.wms.repository.ZoneRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AlertServiceTest — 3 essential tests
 *
 * Covers: create alert (happy path + zone name), invalid alertType string,
 * and resolving an alert via SecurityContext.
 *
 * Run: mvn test -Dtest=AlertServiceTest
 */
@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock AlertRepository alertRepository;
    @Mock ZoneRepository  zoneRepository;

    @InjectMocks AlertService alertService;

    private Zone    storageZone;
    private AppUser managerUser;

    @BeforeEach
    void setUp() {
        storageZone = Zone.builder()
                .id(UUID.randomUUID()).name("Storage Zone")
                .zoneType(Zone.ZoneType.STORAGE_ZONE).capacity(200)
                .status(Zone.ZoneStatus.ACTIVE).build();

        managerUser = AppUser.builder()
                .id(UUID.randomUUID()).firstName("Alice").lastName("Mgr")
                .phoneNumber("0771234567").username("alice.mgr")
                .passwordHash("hashed").role(Role.MANAGER).build();

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    // A1 — Happy path: alert saved with PENDING status, zone name in response
    @Test
    @DisplayName("A1 — Create alert returns PENDING status with zone name populated")
    void createAlert_valid_returnsPendingWithZoneName() {
        when(zoneRepository.findById(storageZone.getId())).thenReturn(Optional.of(storageZone));
        when(alertRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AlertResponseDTO result = alertService.createAlert(CreateAlertDTO.builder()
                .alertType("LOW_STOCK").severity("LOW")
                .message("Running low").zoneId(storageZone.getId()).build());

        assertThat(result.getAlertStatus()).isEqualTo("PENDING");
        assertThat(result.getZoneName()).isEqualTo("Storage Zone");
    }

    // A2 — Invalid enum string must fail fast, not silently save a broken alert
    @Test
    @DisplayName("A2 — Invalid alertType string throws IllegalArgumentException")
    void createAlert_invalidType_throws() {
        assertThatThrownBy(() -> alertService.createAlert(CreateAlertDTO.builder()
                .alertType("BAD_TYPE").severity("LOW").message("x").build()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // A3 — Resolve must record RESOLVED status and the resolving user from SecurityContext
    @Test
    @DisplayName("A3 — Resolve alert sets RESOLVED status and records resolving username")
    void resolveAlert_setsResolvedStatusAndUsername() {
        Alert alert = Alert.builder().id(UUID.randomUUID())
                .alertType(Alert.AlertType.LOW_STOCK).severity(Alert.Severity.LOW)
                .alertStatus(Alert.AlertStatus.PENDING).message("low")
                .createdAt(OffsetDateTime.now()).build();

        when(alertRepository.findById(alert.getId())).thenReturn(Optional.of(alert));
        when(alertRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        managerUser, null, managerUser.getAuthorities()));

        AlertResponseDTO result = alertService.resolveAlert(alert.getId());

        assertThat(result.getAlertStatus()).isEqualTo("RESOLVED");
        assertThat(result.getResolvedByUsername()).isEqualTo("alice.mgr");
    }
}
