package com.aiops.monitoring.repository;

import com.aiops.monitoring.model.MetricConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetricConfigRepository extends JpaRepository<MetricConfig, Long> {

    List<MetricConfig> findByServerId(Long serverId);

    List<MetricConfig> findByEnabled(Boolean enabled);

    Optional<MetricConfig> findByServerIdAndMetricName(Long serverId, String metricName);

    List<MetricConfig> findByServerIdAndEnabled(Long serverId, Boolean enabled);
}
