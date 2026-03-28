package com.axacrate.wms.service;

import com.axacrate.wms.dto.ReplaceTagRequestDTO;
import com.axacrate.wms.dto.TagHealthResponseDTO;
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

    // Measurement window — how far back we look to count reads
    // TODO: make configurable later
    private static final long   WINDOW_SECONDS      = 60;   // look back 60 seconds

    // Minimum acceptable read frequency
    // TODO: make configurable later
    private static final double MIN_READS_PER_SECOND = 0.1; // at least 1 read per 10 seconds

    // ── Called on every scan from RfidService ─────────────────────────────────

    @Transactional
    public TagHealthResponseDTO checkHealth(RfidTag tag) {
        OffsetDateTime windowStart = OffsetDateTime.now().minusSeconds(WINDOW_SECONDS);
        long readCount = movementLogRepository.countByTagIdSince(tag.getId(), windowStart);

        double readsPerSecond = (double) readCount / WINDOW_SECONDS;

        log.info("Tag {} | reads in last {}s: {} ({} reads/sec)",
                tag.getUid(), WINDOW_SECONDS, readCount, String.format("%.3f", readsPerSecond));

        boolean isUnhealthy = readsPerSecond < MIN_READS_PER_SECOND;
        boolean alertRaised = false;

        if (isUnhealthy && tag.isActive()) {
            log.warn("Tag {} is underperforming. reads/sec={}, min={}",
                    tag.getUid(), String.format("%.3f", readsPerSecond), MIN_READS_PER_SECOND);

            // Mark tag as INACTIVE
            tag.setStatus(RfidTag.RfidStatus.INACTIVE);
            rfidTagRepository.save(tag);

            alertRaised = true;
        }

        return buildResponse(tag, readCount, readsPerSecond, alertRaised);
    }

    // ── Get health status of all tags ─────────────────────────────────────────

    public List<TagHealthResponseDTO> getAllTagHealthStatuses() {
        OffsetDateTime windowStart = OffsetDateTime.now().minusSeconds(WINDOW_SECONDS);

        return rfidTagRepository.findAll()
                .stream()
                .map(tag -> {
                    long readCount = movementLogRepository.countByTagIdSince(tag.getId(), windowStart);
                    double readsPerSecond = (double) readCount / WINDOW_SECONDS;
                    return buildResponse(tag, readCount, readsPerSecond, false);
                })
                .collect(Collectors.toList());
    }

    // ── Replace unhealthy tag — transfer inventory to new tag ─────────────────

    @Transactional
    public TagHealthResponseDTO replaceTag(ReplaceTagRequestDTO request, AppUser resolvedBy) {

        RfidTag unhealthyTag = rfidTagRepository.findByUid(request.getUnhealthyTagUid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tag not found: " + request.getUnhealthyTagUid()));

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

        log.info("Tag replacement complete. Old={} New={} ResolvedBy={}",
                unhealthyTag.getUid(), newTag.getUid(), resolvedBy.getUsername());

        OffsetDateTime windowStart = OffsetDateTime.now().minusSeconds(WINDOW_SECONDS);
        long readCount = movementLogRepository.countByTagIdSince(newTag.getId(), windowStart);
        double readsPerSecond = (double) readCount / WINDOW_SECONDS;
        return buildResponse(newTag, readCount, readsPerSecond, false);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private TagHealthResponseDTO buildResponse(RfidTag tag, long readCount,
                                               double readsPerSecond, boolean alertRaised) {
        String healthStatus = (readsPerSecond >= MIN_READS_PER_SECOND && tag.isActive())
                ? "HEALTHY" : "UNHEALTHY";

        InventoryItem item = tag.getInventoryItem();

        return TagHealthResponseDTO.builder()
                .tagId(tag.getId())
                .tagUid(tag.getUid())
                .tagStatus(tag.getStatus().name())
                .healthStatus(healthStatus)
                .readsInWindow(readCount)
                .windowSeconds(WINDOW_SECONDS)
                .readsPerSecond(Math.round(readsPerSecond * 1000.0) / 1000.0) // 3 decimal places
                .minReadsPerSecond(MIN_READS_PER_SECOND)
                .inventoryItemId(item != null ? item.getId().toString() : null)
                .inventoryItemName(item != null ? item.getName() : null)
                .lastSeenZone(tag.getLastSeenZone() != null ? tag.getLastSeenZone().getName() : null)
                .lastSeenAt(tag.getLastSeenAt())
                .alertRaised(alertRaised)
                .build();
    }
}