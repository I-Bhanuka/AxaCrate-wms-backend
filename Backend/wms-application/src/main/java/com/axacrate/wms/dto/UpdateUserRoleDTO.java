package com.axacrate.wms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for updating a user's role.
 * Used by PUT /api/admin/users/{id}/role
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRoleDTO {

    @NotBlank(message = "Role is required")
    private String role; // ADMIN, MANAGER, or WORKER
}