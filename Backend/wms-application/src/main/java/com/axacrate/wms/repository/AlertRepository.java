package com.axacrate.wms.repository;

import com.axacrate.wms.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Alert Repository - Database access for Alert entity
 *
 * Provides methods to:
 * - Find alerts by status
 * - Find alerts by severity
 * - Count critical alerts
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {

    /**
     * Find all alerts with a specific status
     * SQL: SELECT * FROM alert WHERE alert_status = ?
     *
     * Example: Find all PENDING, ACKNOWLEDGED, or RESOLVED alerts
     *
     * @param status - Alert status
     * @return List of alerts
     */
    List<Alert> findByAlertStatus(Alert.AlertStatus status);

    /**
     * Find all alerts with a specific severity
     * SQL: SELECT * FROM alert WHERE severity = ?
     *
     * Example: Find all CRITICAL alerts
     *
     * @param severity - Alert severity
     * @return List of alerts
     */
    List<Alert> findBySeverity(Alert.Severity severity);

    /**
     * Find all alerts of a specific type
     * SQL: SELECT * FROM alert WHERE alert_type = ?
     *
     * Example: Find all UNAUTHORIZED_MOVEMENT alerts
     *
     * @param alertType - Alert type
     * @return List of alerts
     */
    List<Alert> findByAlertType(Alert.AlertType alertType);

    /**
     * Find all unresolved alerts, sorted by severity and time
     * Shows CRITICAL alerts first, then by creation time
     *
     * Useful for alert dashboard
     *
     * @return List of unresolved alerts, priority sorted
     */
    @Query("SELECT a FROM Alert a WHERE a.alertStatus != 'RESOLVED' ORDER BY a.severity DESC, a.createdAt DESC")
    List<Alert> findAllUnresolvedAlerts();

    /**
     * Find alerts within a date range
     * SQL: SELECT * FROM alert WHERE created_at BETWEEN ? AND ?
     *
     * Useful for generating reports
     *
     * @param start - Start datetime
     * @param end - End datetime
     * @return List of alerts
     */
    @Query("SELECT a FROM Alert a WHERE a.createdAt BETWEEN :start AND :end")
    List<Alert> findByDateRange(@Param("start") OffsetDateTime start,
                                @Param("end") OffsetDateTime end);

    /**
     * Count critical pending alerts
     * Used for dashboard metrics
     *
     * @return Number of critical pending alerts
     */
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.alertStatus = 'PENDING' AND a.severity = 'CRITICAL'")
    Long countCriticalPendingAlerts();
}