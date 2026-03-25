package com.axacrate.wms.service;

import com.axacrate.wms.dto.ReplaceTagRequestDTO;
import com.axacrate.wms.dto.TagHealthResponseDTO;
import com.axacrate.wms.entity.Alert;
import com.axacrate.wms.entity.AppUser;
import com.axacrate.wms.entity.InventoryItem;
import com.axacrate.wms.entity.RfidTag;
import com.axacrate.wms.exception.ResourceNotFoundException;
import com.axacrate.wms.repository.AlertRepository;
import com.axacrate.wms.repository.MovementLogRepository;
import com.axacrate.wms.repository.RfidTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagHealthService {

    private final RfidTagRepository     rfidTagRepository;
    private final MovementLogRepository movementLogRepository;
    private final AlertRepository       alertRepository;

    // Hardcoded standard — minimum reads expected per hour
    // TODO: make configurable later
    private static final int MIN_READS_PER_HOUR = 1;

    // ── Called on every scan from RfidService ─────────────────────────────────

    @Transactional
    public TagHealthResponseDTO checkHealth(RfidTag tag) {
        OffsetDateTime oneHourAgo = OffsetDateTime.now().minusHours(1);
        long readCount = movementLogRepository.countByTagIdSince(tag.getId(), oneHourAgo);

        log.info("Tag {} | reads in last hour: {}", tag.getUid(), readCount);

        boolean isUnhealthy = readCount < MIN_READS_PER_HOUR;
        boolean alertRaised = false;

        if (isUnhealthy && tag.isActive()) {
            log.warn("Tag {} is underperforming. reads={}, min={}", tag.getUid(), readCount, MIN_READS_PER_HOUR);

            // Mark tag as INACTIVE
            tag.setStatus(RfidTag.RfidStatus.INACTIVE);
            rfidTagRepository.save(tag);

            // Raise OFFLINE_READ alert
            alertRepository.save(Alert.builder()
                    .alertType(Alert.AlertType.OFFLINE_READ)
                    .severity(Alert.Severity.MEDIUM)
                    .alertStatus(Alert.AlertStatus.PENDING)
                    .message("Tag [" + tag.getUid() + "] is underperforming. " +
                            "reads=" + readCount + ", min=" + MIN_READS_PER_HOUR + "/hr. " +
                            "Tag marked INACTIVE. Please assign a replacement tag.")
                    .build());

            alertRaised = true;
        }

        return buildResponse(tag, readCount, alertRaised);
    }

    //── Get health status of all active tags ──────────────────────────────────

    public List<TagHealthResponseDTO> getAllTagHealthStatuses() {
        OffsetDateTime oneHourAgo = OffsetDateTime.now().minusHours(1);

        return rfidTagRepository.findAll()
                .stream()
                .map(tag -> {
                    long readCount = movementLogRepository.countByTagIdSince(tag.getId(), oneHourAgo);
                    return buildResponse(tag, readCount, false);
                })
                .collect(Collectors.toList());
    }

    // ── Replace unhealthy tag — transfer inventory to new tag ─────────────────
    // Staff resolves the issue by scanning a new healthy tag and transferring data

    @Transactional
    public TagHealthResponseDTO replaceTag(ReplaceTagRequestDTO request, AppUser resolvedBy) {

        // Find unhealthy tag
        RfidTag unhealthyTag = rfidTagRepository.findByUid(request.getUnhealthyTagUid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tag not found: " + request.getUnhealthyTagUid()));

        // Find new tag
        RfidTag newTag = rfidTagRepository.findByUid(request.getNewTagUid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "New tag not found: " + request.getNewTagUid()));

        // Transfer inventory item from old tag to new tag
        InventoryItem item = unhealthyTag.getInventoryItem();
        if (item != null) {
            unhealthyTag.setInventoryItem(null);
            newTag.setInventoryItem(item);
            rfidTagRepository.save(unhealthyTag);
            rfidTagRepository.save(newTag);
            log.info("Inventory [{}] transferred from tag [{}] to tag [{}]",
                    item.getName(), unhealthyTag.getUid(), newTag.getUid());
        }

        // Mark unhealthy tag as LOST
        unhealthyTag.setStatus(RfidTag.RfidStatus.LOST);
        rfidTagRepository.save(unhealthyTag);

        // Resolve the alert — save who resolved it
        Alert alert = alertRepository.findById(UUID.fromString(request.getAlertId()))
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + request.getAlertId()));
        alert.resolve(resolvedBy);
        alertRepository.save(alert);

        log.info("Tag replacement complete. Old={} New={} ResolvedBy={}",
                unhealthyTag.getUid(), newTag.getUid(), resolvedBy.getUsername());

        OffsetDateTime oneHourAgo = OffsetDateTime.now().minusHours(1);
        long readCount = movementLogRepository.countByTagIdSince(newTag.getId(), oneHourAgo);
        return buildResponse(newTag, readCount, false);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private TagHealthResponseDTO buildResponse(RfidTag tag, long readCount, boolean alertRaised) {
        String healthStatus = (readCount >= MIN_READS_PER_HOUR && tag.isActive())
                ? "HEALTHY" : "UNHEALTHY";

        InventoryItem item = tag.getInventoryItem();

        return TagHealthResponseDTO.builder()
                .tagId(tag.getId())
                .tagUid(tag.getUid())
                .tagStatus(tag.getStatus().name())
                .healthStatus(healthStatus)
                .readsLastHour(readCount)
                .minRequired(MIN_READS_PER_HOUR)
                .inventoryItemId(item != null ? item.getId().toString() : null)
                .inventoryItemName(item != null ? item.getName() : null)
                .lastSeenZone(tag.getLastSeenZone() != null ? tag.getLastSeenZone().getName() : null)
                .lastSeenAt(tag.getLastSeenAt())
                .alertRaised(alertRaised)
                .build();
    }
}