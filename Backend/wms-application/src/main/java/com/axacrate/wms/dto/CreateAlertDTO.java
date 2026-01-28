package com.rfidwms.model.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for Creating Alert
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAlertDTO {

    @NotNull(message = "Alert type is required")
    private String alertType;

    @NotNull(message = "Severity is required")
    private String severity;

    @NotBlank(message = "Message is required")
    private String message;
}