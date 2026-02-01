package com.axacrate.wms.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for User Login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}