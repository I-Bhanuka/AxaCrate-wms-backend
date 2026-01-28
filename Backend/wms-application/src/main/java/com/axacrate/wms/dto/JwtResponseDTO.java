package com.rfidwms.model.dto;

import lombok.*;

import java.util.UUID;

/**
 * DTO for JWT Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponseDTO {
    private String token;
    private String type = "Bearer";
    private UUID userId;
    private String username;
    private String role;
    private String fullName;
}