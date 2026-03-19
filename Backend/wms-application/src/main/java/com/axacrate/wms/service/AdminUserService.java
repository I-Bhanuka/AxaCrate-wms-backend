package com.axacrate.wms.service;

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
