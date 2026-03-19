package com.axacrate.wms.controller;

import com.axacrate.wms.dto.ApiResponse;
import com.axacrate.wms.dto.UpdateUserRoleDTO;
import com.axacrate.wms.entity.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.axacrate.wms.dto.UserRegistrationDTO;
import com.axacrate.wms.dto.UserResponseDTO;
import com.axacrate.wms.service.AdminUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

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

    // ── GET /api/admin/users ──────────────────────────────────────────────────
    // List all users. Only ADMIN can see the full user list.

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {

        List<UserResponseDTO> users = adminUserService.getAllUsers();

        return  ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(users, "Users list retrieved successfully"));
    }

    // ── GET /api/admin/users/{id} ─────────────────────────────────────────────
    // Get a single user by ID.

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(
            @PathVariable UUID id) {

        UserResponseDTO user = adminUserService.getUserById(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(user, "User retrieved successfully"));
    }

    // ── PUT /api/admin/users/{id}/role ────────────────────────────────────────
    // Change a user's role. Admin cannot change their own role.
    // The requesting admin's UUID is taken from their JWT (via Authentication),
    // not from the request body — so it can't be spoofed.

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRoleDTO dto,
            Authentication authentication) {

        // Get the requesting admin from their JWT (via Authentication)
        AppUser requestingAdmin  = (AppUser) authentication.getPrincipal();

        UserResponseDTO updatedUser = adminUserService.updateUserRole(id, dto, requestingAdmin.getId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(updatedUser, "User role updated successfully"));
    }

    // ── DELETE /api/admin/users/{id} ──────────────────────────────────────────
    // Permanently delete a user. Admin cannot delete themselves.

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable UUID id,
            Authentication authentication) {

        // Get the requesting admin from their JWT (via Authentication)
        AppUser requestingAdmin  = (AppUser) authentication.getPrincipal();

        adminUserService.deleteUser(id, requestingAdmin.getId());

        return  ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(null, "User deleted successfully"));
    }

}
