package com.axacrate.wms.dto;

import lombok.*;


/**
 * DTO for JWT Response
 */
@Data
@AllArgsConstructor
public class JwtResponseDTO {
    private String token;
    private String username;
    private String role;
}