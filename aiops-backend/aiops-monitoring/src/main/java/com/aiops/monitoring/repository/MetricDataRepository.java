package com.aiops.monitoring.repository;

import com.aiops.monitoring.model.MetricData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MetricDataRepository extends JpaRepository<MetricData, Long> {

    List<MetricData> findByServerId(Long serverId);

    List<MetricData> findByServerIdAndMetricName(Long serverId, String metricName);

    List<MetricData> findByServerIdOrderByTimestampDesc(Long serverId, Pageable pageable);

    @Query("SELECT m FROM MetricData m WHERE m.serverId = :serverId AND m.metricName = :metricName AND m.timestamp >= :startTime ORDER BY m.timestamp DESC")
    List<MetricData> findByServerIdAndMetricNameAndTimeRange(
            @Param("serverId") Long serverId,
            @Param("metricName") String metricName,
            @Param("startTime") LocalDateTime startTime);

    @Query("SELECT m FROM MetricData m WHERE m.serverId = :serverId AND m.timestamp >= :startTime ORDER BY m.timestamp DESC")
    List<MetricData> findByServerIdAndTimeRange(
            @Param("serverId") Long serverId,
            @Param("startTime") LocalDateTime startTime);

    @Query("SELECT m FROM MetricData m WHERE m.serverId = :serverId AND m.metricName = :metricName ORDER BY m.timestamp DESC")
    List<MetricData> findLatestByServerIdAndMetricName(
            @Param("serverId") Long serverId,
            @Param("metricName") String metricName,
            Pageable pageable);
}
