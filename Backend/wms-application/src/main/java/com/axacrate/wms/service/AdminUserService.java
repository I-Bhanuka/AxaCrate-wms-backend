package com.axacrate.wms.service;

import com.axacrate.wms.dto.UpdateUserRoleDTO;
import com.axacrate.wms.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.axacrate.wms.dto.UserRegistrationDTO;
import com.axacrate.wms.dto.UserResponseDTO;
import com.axacrate.wms.entity.AppUser;
import com.axacrate.wms.enums.Role;
import com.axacrate.wms.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    // ── Create ────────────────────────────────────────────────────────────────

    /**
     * Creates a new user. Only callable by an ADMIN.
     * The role is set by the admin at creation time — users cannot choose their own role.
     */
    @Transactional
    public UserResponseDTO createUser(UserRegistrationDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username [" + dto.getUsername() + "] is already taken.");
        }

        Role role = parseRole(dto.getRole());

        AppUser user = AppUser.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .username(dto.getUsername())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(role)
                .build();

        AppUser saved = userRepository.save(user);
        log.info("Admin created new user: {} with role: {}", saved.getUsername(), saved.getRole());

        return mapToUserResponse(saved);
    }


    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * Returns all users in the system, sorted by role then username.
     * Passwords are never included — UserResponseDTO contains safe fields only.
     */
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .sorted((a, b) -> {
                    int roleCompare = a.getRole().name().compareTo(b.getRole().name());
                    return roleCompare != 0 ? roleCompare : a.getUsername().compareTo(b.getUsername());
                })
                .map(this::mapToUserResponse)
                .toList();
    }

    /**
     * Returns a single user by their UUID.
     */
    public UserResponseDTO getUserById(UUID id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID [" + id + "] not found."));

        return mapToUserResponse(user);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    /**
     * Updates a user's role.
     *
     * Safety rule: an admin cannot change their own role. This prevents the
     * last admin from accidentally locking themselves out of the system.
     * If you need to change an admin's role, another admin must do it.
     */

    @Transactional
    public UserResponseDTO updateUserRole(
            UUID targetUserId, UpdateUserRoleDTO dto, UUID requestingAdminId){

        // Check if the same person is trying to change their own role
        if (targetUserId.equals(requestingAdminId)) {
            throw new IllegalArgumentException(
                    "You cannot change your own role. Ask another admin to do this.");
        }

        // Find the target user to update
        AppUser user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID [" + targetUserId + "] not found."));

        Role newRole = parseRole(dto.getRole());
        Role oldRole = user.getRole();

        // Update the user's role
        user.setRole(newRole);
        userRepository.save(user);

        log.info("Admin [{}] changed role of user [{}] from [{}] to [{}]",
                requestingAdminId, user.getUsername(), oldRole, newRole);

        return mapToUserResponse(user);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    /**
     * Permanently deletes a user.
     *
     * Same safety rule: an admin cannot delete themselves.
     */

    @Transactional
    public void deleteUser(UUID targetUserId, UUID requestingAdminId) {

        // Check if the same person is trying to delete themselves
        if (targetUserId.equals(requestingAdminId)) {
            throw new IllegalArgumentException(
                    "You cannot delete your own account");
        }

        // Find the target user to delete
        AppUser user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID [" + targetUserId + "] not found."));

        // Delete the user from the DB
        userRepository.delete(user);

        log.info("Admin [{}] deleted user [{}] with role [{}]",
                requestingAdminId, user.getUsername(), user.getRole());
    }


    // ── Helpers ───────────────────────────────────────────────────────────────

    private Role parseRole(String roleStr) {
        try {
            return Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid role [" + roleStr + "]. Must be one of: ADMIN, MANAGER, WORKER");
        }
    }

    private UserResponseDTO mapToUserResponse(AppUser user) {
        return new UserResponseDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getUsername(),
                user.getRole().name()
        );
    }
}
