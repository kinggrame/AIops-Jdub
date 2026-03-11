package com.aiops.monitoring.service;

import com.aiops.monitoring.dto.CreateServerRequest;
import com.aiops.monitoring.dto.MetricReportRequest;
import com.aiops.monitoring.model.MetricData;
import com.aiops.monitoring.model.TargetServer;
import com.aiops.monitoring.repository.MetricDataRepository;
import com.aiops.monitoring.repository.TargetServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MonitoringService {

    @Autowired
    private TargetServerRepository serverRepository;

    @Autowired
    private MetricDataRepository metricDataRepository;

    // Server CRUD

    public List<TargetServer> findAllServers() {
        return serverRepository.findAll();
    }

    public Optional<TargetServer> findServerById(Long id) {
        return serverRepository.findById(id);
    }

    public List<TargetServer> findServersByStatus(TargetServer.ServerStatus status) {
        return serverRepository.findByStatus(status);
    }

    @Transactional
    public TargetServer createServer(CreateServerRequest request) {
        TargetServer server = new TargetServer();
        server.setName(request.getName());
        server.setDescription(request.getDescription());
        server.setEndpoint(request.getEndpoint());
        server.setGroupName(request.getGroupName());
        server.setTags(request.getTags());
        server.setAgentId(request.getAgentId());
        server.setStatus(TargetServer.ServerStatus.OFFLINE);
        
        return serverRepository.save(server);
    }

    @Transactional
    public TargetServer updateServer(Long id, CreateServerRequest request) {
        TargetServer server = serverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Server not found: " + id));
        
        if (request.getName() != null) server.setName(request.getName());
        if (request.getDescription() != null) server.setDescription(request.getDescription());
        if (request.getEndpoint() != null) server.setEndpoint(request.getEndpoint());
        if (request.getGroupName() != null) server.setGroupName(request.getGroupName());
        if (request.getTags() != null) server.setTags(request.getTags());
        
        server.setUpdatedAt(LocalDateTime.now());
        return serverRepository.save(server);
    }

    @Transactional
    public void deleteServer(Long id) {
        serverRepository.deleteById(id);
    }

    @Transactional
    public TargetServer updateServerStatus(Long id, TargetServer.ServerStatus status) {
        TargetServer server = serverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Server not found: " + id));
        
        server.setStatus(status);
        server.setUpdatedAt(LocalDateTime.now());
        return serverRepository.save(server);
    }

    // Metric Report

    @Transactional
    public void reportMetrics(MetricReportRequest request) {
        if (request.getMetrics() == null) return;
        
        LocalDateTime timestamp = request.getTimestamp() != null 
                ? LocalDateTime.parse(request.getTimestamp(), DateTimeFormatter.ISO_DATE_TIME)
                : LocalDateTime.now();
        
        for (Map.Entry<String, Object> entry : request.getMetrics().entrySet()) {
            MetricData data = new MetricData();
            data.setServerId(request.getServerId());
            data.setMetricName(entry.getKey());
            
            Object value = entry.getValue();
            if (value instanceof Number) {
                data.setValue(((Number) value).doubleValue());
            } else {
                data.setValue(0.0);
            }
            
            data.setTimestamp(timestamp);
            
            metricDataRepository.save(data);
        }
        
        // Update server status to online
        if (request.getServerId() != null) {
            updateServerStatus(request.getServerId(), TargetServer.ServerStatus.ONLINE);
        }
    }

    // Metric Query

    public Map<String, Object> getLatestMetrics(Long serverId) {
        List<MetricData> latest = metricDataRepository.findLatestByServerIdAndMetricName(
                serverId, 
                "cpu.usage", 
                PageRequest.of(0, 1));
        
        Map<String, Object> result = new HashMap<>();
        
        // Get common metrics
        String[] commonMetrics = {"cpu.usage", "memory.usage", "disk.usage", "load.average"};
        
        for (String metric : commonMetrics) {
            List<MetricData> data = metricDataRepository.findLatestByServerIdAndMetricName(
                    serverId, metric, PageRequest.of(0, 1));
            
            if (!data.isEmpty()) {
                result.put(metric, data.get(0).getValue());
            }
        }
        
        return result;
    }

    public List<MetricData> getMetricHistory(Long serverId, String metricName, String timeRange) {
        LocalDateTime startTime = parseTimeRange(timeRange);
        
        if (metricName != null && !metricName.isEmpty()) {
            return metricDataRepository.findByServerIdAndMetricNameAndTimeRange(
                    serverId, metricName, startTime);
        }
        
        return metricDataRepository.findByServerIdAndTimeRange(serverId, startTime);
    }

    private LocalDateTime parseTimeRange(String timeRange) {
        if (timeRange == null || timeRange.isEmpty()) {
            return LocalDateTime.now().minusHours(1);
        }
        
        if (timeRange.endsWith("h")) {
            int hours = Integer.parseInt(timeRange.substring(0, timeRange.length() - 1));
            return LocalDateTime.now().minusHours(hours);
        } else if (timeRange.endsWith("d")) {
            int days = Integer.parseInt(timeRange.substring(0, timeRange.length() - 1));
            return LocalDateTime.now().minusDays(days);
        } else if (timeRange.endsWith("m")) {
            int minutes = Integer.parseInt(timeRange.substring(0, timeRange.length() - 1));
            return LocalDateTime.now().minusMinutes(minutes);
        }
        
        return LocalDateTime.now().minusHours(1);
    }
}
