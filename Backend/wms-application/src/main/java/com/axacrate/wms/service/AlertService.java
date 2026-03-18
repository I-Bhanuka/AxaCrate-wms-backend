package com.axacrate.wms.service;

import com.axacrate.wms.dto.AlertResponseDTO;
import com.axacrate.wms.dto.CreateAlertDTO;
import com.axacrate.wms.entity.Alert;
import com.axacrate.wms.entity.AppUser;
import com.axacrate.wms.entity.Zone;
import com.axacrate.wms.exception.ResourceNotFoundException;
import com.axacrate.wms.repository.AlertRepository;
import com.axacrate.wms.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final ZoneRepository  zoneRepository;

    /** Return all alerts ordered by creation date descending */
    @Transactional(readOnly = true)
    public List<AlertResponseDTO> getAllAlerts() {
        return alertRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::mapToDTO)
                .toList();
    }

    /** Return only unresolved alerts, priority sorted */
    @Transactional(readOnly = true)
    public List<AlertResponseDTO> getUnresolvedAlerts() {
        return alertRepository.findAllUnresolvedAlerts().stream()
                .map(this::mapToDTO)
                .toList();
    }

    /** Return alerts filtered by status */
    @Transactional(readOnly = true)
    public List<AlertResponseDTO> getAlertsByStatus(String status) {
        Alert.AlertStatus alertStatus = Alert.AlertStatus.valueOf(status.toUpperCase());
        return alertRepository.findByAlertStatus(alertStatus).stream()
                .map(this::mapToDTO)
                .toList();
    }

    /** Return all alerts for a specific zone, newest first */
    @Transactional(readOnly = true)
    public List<AlertResponseDTO> getAlertsByZone(UUID zoneId) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found: " + zoneId));
        return alertRepository.findByZoneOrderByCreatedAtDesc(zone).stream()
                .map(this::mapToDTO)
                .toList();
    }

    /** Return count of critical pending alerts */
    @Transactional(readOnly = true)
    public long countCriticalPending() {
        return alertRepository.countCriticalPendingAlerts();
    }

    /** Create a new alert, optionally linked to a zone */
    @Transactional
    public AlertResponseDTO createAlert(CreateAlertDTO dto) {
        Alert.AlertType alertType = Alert.AlertType.valueOf(dto.getAlertType().toUpperCase());
        Alert.Severity severity   = Alert.Severity.valueOf(dto.getSeverity().toUpperCase());

        Zone zone = null;
        if (dto.getZoneId() != null) {
            zone = zoneRepository.findById(dto.getZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone not found: " + dto.getZoneId()));
        }

        Alert alert = Alert.builder()
                .alertType(alertType)
                .severity(severity)
                .alertStatus(Alert.AlertStatus.PENDING)
                .message(dto.getMessage())
                .zone(zone)
                .build();

        Alert saved = alertRepository.save(alert);
        log.info("Alert created: id={}, type={}, severity={}, zone={}",
                saved.getId(), saved.getAlertType(), saved.getSeverity(),
                zone != null ? zone.getName() : "none");
        return mapToDTO(saved);
    }

    /** Acknowledge an alert */
    @Transactional
    public AlertResponseDTO acknowledgeAlert(UUID id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + id));
        alert.acknowledge();
        Alert saved = alertRepository.save(alert);
        log.info("Alert acknowledged: id={}", id);
        return mapToDTO(saved);
    }

    /** Resolve an alert, recording the resolving user */
    @Transactional
    public AlertResponseDTO resolveAlert(UUID id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + id));
        AppUser resolvedBy = getCurrentUser();
        alert.resolve(resolvedBy);
        Alert saved = alertRepository.save(alert);
        log.info("Alert resolved: id={}", id);
        return mapToDTO(saved);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AlertResponseDTO mapToDTO(Alert alert) {
        return AlertResponseDTO.builder()
                .id(alert.getId())
                .alertType(alert.getAlertType().name())
                .severity(alert.getSeverity().name())
                .alertStatus(alert.getAlertStatus().name())
                .message(alert.getMessage())
                .zoneId(alert.getZone() != null ? alert.getZone().getId() : null)
                .zoneName(alert.getZone() != null ? alert.getZone().getName() : null)
                .createdAt(alert.getCreatedAt())
                .resolvedAt(alert.getResolvedAt())
                .resolvedByUsername(alert.getResolvedBy() != null ? alert.getResolvedBy().getUsername() : null)
                .build();
    }

    private AppUser getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUser appUser) {
            return appUser;
        }
        return null;
    }
}
