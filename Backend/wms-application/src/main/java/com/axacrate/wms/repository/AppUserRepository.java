package com.axacrate.wms.repository;

import com.axacrate.wms.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * App User Repository - Database access for AppUser entity
 *
 * Provides methods to:
 * - Find users by username
 * - Find users by role
 * - Check username existence
 */
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    /**
     * Find user by username (for login)
     * Username is unique, so returns single user
     * SQL: SELECT * FROM app_user WHERE username = ?
     *
     * @param username - Username
     * @return Optional containing user if found
     */
    Optional<AppUser> findByUsername(String username);

    /**
     * Find all users with a specific role
     * SQL: SELECT * FROM app_user WHERE role = ?
     *
     * Example: Find all ADMINs, MANAGERs, or WORKERs
     *
     * @param role - User role
     * @return List of users
     */
    List<AppUser> findByRole(AppUser.UserRole role);

    /**
     * Check if username already exists
     * Used during registration to prevent duplicates
     * More efficient than findByUsername for existence check
     *
     * @param username - Username to check
     * @return true if exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Find all users with managerial access (ADMIN or MANAGER)
     * Useful for sending alerts to decision-makers
     *
     * Uses IN clause in JPQL
     *
     * @return List of admin and manager users
     */
    @Query("SELECT u FROM AppUser u WHERE u.role IN ('ADMIN', 'MANAGER')")
    List<AppUser> findAllManagerialUsers();
}