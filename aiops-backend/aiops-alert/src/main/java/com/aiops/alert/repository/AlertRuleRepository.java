package com.aiops.alert.repository;

import com.aiops.alert.model.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {
    List<AlertRule> findByStatus(AlertRule.AlertRuleStatus status);
    List<AlertRule> findByMetricName(String metricName);
    List<AlertRule> findByServerId(Long serverId);
    List<AlertRule> findByServerGroup(String serverGroup);
    List<AlertRule> findByStatusAndMetricName(AlertRule.AlertRuleStatus status, String metricName);
}
