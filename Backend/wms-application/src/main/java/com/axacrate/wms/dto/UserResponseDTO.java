package com.axacrate.wms.dto;

import java.util.UUID;

import lombok.*;

@Data
@AllArgsConstructor
public class UserResponseDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String username;
    private String role;
}

