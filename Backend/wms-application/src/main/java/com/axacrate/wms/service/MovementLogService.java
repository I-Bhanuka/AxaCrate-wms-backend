package com.axacrate.wms.service;

import com.axacrate.wms.dto.MovementLogResponseDTO;
import com.axacrate.wms.entity.MovementLog;
import com.axacrate.wms.repository.MovementLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovementLogService {

    private final MovementLogRepository movementLogRepository;

    @Transactional(readOnly = true)
    public List<MovementLogResponseDTO> getRecentMovements(int limit) {
        // Cap limit to prevent abuse
        int safeLimit = Math.max(1, Math.min(limit, 100));

        List<MovementLog> logs = movementLogRepository.findRecentMovements(
                MovementLog.EventType.MOVEMENT,
                PageRequest.of(0, safeLimit)
        );

        log.info("Movement logs retrieved: {}", logs.size());

        return logs.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovementLogResponseDTO> getRecentActivity(int limit) {
        // Cap limit to prevent abuse
        int safeLimit = Math.max(1, Math.min(limit, 100));

        List<MovementLog> logs = movementLogRepository.findRecentActivity(PageRequest.of(0, safeLimit));

        log.info("Activity logs retrieved: {}", logs.size());

        return logs.stream()
                .map(this::mapToDTO)
                .toList();
    }

    private MovementLogResponseDTO mapToDTO(MovementLog log) {
        var tag = log.getTag();
        var item = tag != null ? tag.getInventoryItem() : null;

        return MovementLogResponseDTO.builder()
                .id(log.getId())
                .itemName(item != null ? item.getName() : null)
                .itemSku(item != null ? item.getSku() : null)
                .fromZoneName(log.getFromZone() != null ? log.getFromZone().getName() : null)
                .toZoneName(log.getToZone() != null ? log.getToZone().getName() : null)
                .eventType(log.getEventType() != null ? log.getEventType().getValue() : null)
                .occurredAt(log.getOccurredAt())
                .hardwareType(log.getHardware() != null ? log.getHardware().getHardwareType() : null)
                .synced(Boolean.TRUE.equals(log.getSynced()))
                .build();
    }
}