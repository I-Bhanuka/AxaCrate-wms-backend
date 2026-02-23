package com.axacrate.wms.service;

import com.axacrate.wms.dto.MovementLogResponseDTO;
import com.axacrate.wms.entity.MovementLog;
import com.axacrate.wms.repository.MovementLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        int safeLimit = Math.min(limit, 100);

        List<MovementLog> logs = movementLogRepository.findRecentMovements(safeLimit);

        log.info("Movement logs retrieved: {}", logs.size());

        return logs.stream()
                .map(this::mapToDTO)
                .toList();
    }

    private MovementLogResponseDTO mapToDTO(MovementLog log) {
        // Safely get item info through tag — tag or item might not exist
        var tag  = log.getTag();
        var item = tag != null ? tag.getInventoryItem() : null;

        return MovementLogResponseDTO.builder()
                .id(log.getId())
                .fromZoneName(log.getFromZone() != null ? log.getFromZone().getName() : null)
                .toZoneName(log.getToZone()   != null ? log.getToZone().getName()   : null)
                .eventType(log.getEventType().getValue())
                .occurredAt(log.getOccurredAt())
                .hardwareType(log.getHardware() != null ? log.getHardware().getHardwareType() : null)
                .synced(log.getSynced())
                .itemSku(item != null ? item.getSku() : null)
                .itemName(item != null ? item.getName() : null)
                .build();
    }
}