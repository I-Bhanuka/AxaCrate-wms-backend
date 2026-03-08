package com.axacrate.wms.service;

import com.axacrate.wms.dto.GeofenceResponseDTO;
import com.axacrate.wms.entity.Alert;
import com.axacrate.wms.entity.RfidHardware;
import com.axacrate.wms.entity.RfidTag;
import com.axacrate.wms.exception.GeofenceViolationException;
import com.axacrate.wms.exception.UnauthorizedMovementException;
import com.axacrate.wms.repository.AlertRepository;
import com.axacrate.wms.repository.RfidHardwareRepository;
import com.axacrate.wms.repository.RfidTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeofenceService {

    private final RfidTagRepository      rfidTagRepository;
    private final RfidHardwareRepository rfidHardwareRepository;
    private final AlertRepository        alertRepository;

    @Transactional
    public GeofenceResponseDTO processGeofenceEvent(String tagUid, UUID hardwareId, String direction) {

        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        // Detect hardware
        RfidHardware hardware = rfidHardwareRepository.findById(hardwareId)
                .orElseThrow(() -> new GeofenceViolationException(
                        "Hardware not found: " + hardwareId, null, null, null));

        if (!hardware.isOperational())
            throw new GeofenceViolationException("Hardware [" + hardware.getName() + "] is not operational.", null, null, null);

        if (!hardware.isReader())
            throw new GeofenceViolationException("Hardware [" + hardware.getName() + "] is not a READER.", null, null, null);



        // Detect tag
        RfidTag tag = rfidTagRepository.findByUid(tagUid).orElse(null);
        if (tag == null) {
            log.warn("Unknown tag: {}", tagUid);
            saveAlert(Alert.AlertType.TAG_MISMATCH, Alert.Severity.CRITICAL);
            return deniedResponse("Unknown tag: " + tagUid, timestamp);
        }

        // Check tag is active
        if (!tag.isActive()) {
            log.warn("Tag {} is {}", tagUid, tag.getStatus());
            saveAlert(Alert.AlertType.UNAUTHORIZED_MOVEMENT, Alert.Severity.HIGH);
            throw new UnauthorizedMovementException("Tag [" + tagUid + "] is " + tag.getStatus() + ". Movement denied.");
        }

        return GeofenceResponseDTO.builder()
                .authorized(true)
                .statusMessage("Authorized")
                .serverTimestamp(timestamp)
                .build();
    }

    private void saveAlert(Alert.AlertType type, Alert.Severity severity) {
        alertRepository.save(Alert.builder()
                .alertType(type)
                .severity(severity)
                .alertStatus(Alert.AlertStatus.PENDING)
                .build());
    }

    private GeofenceResponseDTO deniedResponse(String message, String timestamp) {
        return GeofenceResponseDTO.builder()
                .authorized(false)
                .statusMessage("VIOLATION: " + message)
                .itemId(null)
                .serverTimestamp(timestamp)
                .build();
    }
}