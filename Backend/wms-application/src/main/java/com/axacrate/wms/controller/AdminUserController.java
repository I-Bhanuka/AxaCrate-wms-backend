package com.axacrate.wms.controller;

import com.axacrate.wms.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.axacrate.wms.dto.UserRegistrationDTO;
import com.axacrate.wms.dto.UserResponseDTO;
import com.axacrate.wms.service.AdminUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/users") 
@RequiredArgsConstructor
@Validated
public class AdminUserController {

    private final AdminUserService adminUserService;

    // ── POST /api/admin/users ─────────────────────────────────────────────────
    // Create a new user. Only ADMIN can call this.

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(
            @Valid @RequestBody UserRegistrationDTO dto) {

        UserResponseDTO createdUser = adminUserService.createUser(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdUser, "User created successfully"));
    }
}
