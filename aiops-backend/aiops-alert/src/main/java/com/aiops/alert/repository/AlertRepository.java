package com.aiops.alert.repository;

import com.aiops.alert.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByStatus(Alert.AlertStatus status);
    List<Alert> findByServerId(Long serverId);
    List<Alert> findByRuleId(Long ruleId);
    List<Alert> findByRuleIdAndStatus(Long ruleId, Alert.AlertStatus status);
    List<Alert> findBySeverity(Alert.AlertSeverity severity);
    List<Alert> findByStatusOrderByFiredAtDesc(Alert.AlertStatus status);
}
