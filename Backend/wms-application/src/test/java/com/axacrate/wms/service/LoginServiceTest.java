package com.axacrate.wms.service;

import com.axacrate.wms.dto.UpdateUserRoleDTO;
import com.axacrate.wms.dto.UserRegistrationDTO;
import com.axacrate.wms.dto.UserResponseDTO;
import com.axacrate.wms.entity.AppUser;
import com.axacrate.wms.enums.Role;
import com.axacrate.wms.repository.AppUserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * LoginServiceTest — 4 essential tests
 *
 * Covers the user/auth layer: creating a user (password encoding + role mapping),
 * duplicate username guard, self-role-change guard, and self-delete guard.
 *
 * Note: The actual JWT login flow (POST /api/auth/login) is best verified
 * with Postman integration tests since it requires the full Spring Security
 * filter chain. These unit tests cover the AdminUserService business rules.
 *
 * Run: mvn test -Dtest=LoginServiceTest
 */
@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock AppUserRepository userRepository;
    @Mock PasswordEncoder   passwordEncoder;

    @InjectMocks AdminUserService adminUserService;

    private UUID adminId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
    }

    // L1 — Happy path: password must be encoded, role mapped, DTO returned
    @Test
    @DisplayName("L1 — Create user encodes password and maps role correctly")
    void createUser_encodesPasswordAndMapsRole() {
        when(userRepository.existsByUsername("jane")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$encoded");
        when(userRepository.save(any())).thenAnswer(i -> {
            AppUser u = i.getArgument(0);
            return AppUser.builder()
                    .id(UUID.randomUUID())
                    .firstName(u.getFirstName()).lastName(u.getLastName())
                    .phoneNumber(u.getPhoneNumber()).username(u.getUsername())
                    .passwordHash(u.getPassword()).role(u.getRole()).build();
        });

        UserResponseDTO result = adminUserService.createUser(
                new UserRegistrationDTO("Jane", "Smith", "0761112233", "jane", "secret123", "WORKER"));

        assertThat(result.getRole()).isEqualTo("WORKER");
        ArgumentCaptor<AppUser> cap = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(cap.capture());
        assertThat(cap.getValue().getPassword()).isEqualTo("$encoded");
        assertThat(cap.getValue().getPassword()).doesNotContain("secret123");
    }

    // L2 — Duplicate username must be blocked before any DB write
    @Test
    @DisplayName("L2 — Duplicate username throws IllegalArgumentException, save never called")
    void createUser_duplicateUsername_throwsAndSkipsSave() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThatThrownBy(() -> adminUserService.createUser(
                new UserRegistrationDTO("X", "X", "0761112234", "admin", "pass", "WORKER")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already taken");
        verify(userRepository, never()).save(any());
    }

    // L3 — Admin self-role-change is blocked to prevent accidental lockout
    @Test
    @DisplayName("L3 — Admin cannot change their own role (self-lockout guard)")
    void updateRole_selfChange_throws() {
        assertThatThrownBy(() ->
                adminUserService.updateUserRole(adminId, new UpdateUserRoleDTO("WORKER"), adminId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot change your own role");
        verify(userRepository, never()).findById(any());
    }

    // L4 — Admin self-delete is blocked for the same safety reason
    @Test
    @DisplayName("L4 — Admin cannot delete their own account")
    void deleteUser_selfDelete_throws() {
        assertThatThrownBy(() -> adminUserService.deleteUser(adminId, adminId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot delete your own account");
    }
}
