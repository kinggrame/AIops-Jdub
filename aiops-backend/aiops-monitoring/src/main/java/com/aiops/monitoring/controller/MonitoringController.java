package com.aiops.monitoring.controller;

import com.aiops.monitoring.dto.CreateServerRequest;
import com.aiops.monitoring.dto.MetricReportRequest;
import com.aiops.monitoring.dto.ServerDTO;
import com.aiops.monitoring.model.MetricData;
import com.aiops.monitoring.model.TargetServer;
import com.aiops.monitoring.service.MonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/servers")
public class MonitoringController {

    @Autowired
    private MonitoringService monitoringService;

    @GetMapping
    public ResponseEntity<List<ServerDTO>> list(
            @RequestParam(required = false) String status) {
        
        List<TargetServer> servers;
        if (status != null && !status.isEmpty()) {
            servers = monitoringService.findServersByStatus(
                    TargetServer.ServerStatus.valueOf(status.toUpperCase()));
        } else {
            servers = monitoringService.findAllServers();
        }
        
        List<ServerDTO> dtos = servers.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServerDTO> get(@PathVariable Long id) {
        return monitoringService.findServerById(id)
                .map(server -> {
                    ServerDTO dto = toDTO(server);
                    dto.setLatestMetrics(monitoringService.getLatestMetrics(id));
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ServerDTO> create(@RequestBody CreateServerRequest request) {
        TargetServer server = monitoringService.createServer(request);
        return ResponseEntity.ok(toDTO(server));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServerDTO> update(
            @PathVariable Long id,
            @RequestBody CreateServerRequest request) {
        TargetServer server = monitoringService.updateServer(id, request);
        return ResponseEntity.ok(toDTO(server));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        monitoringService.deleteServer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics(@PathVariable Long id) {
        Map<String, Object> metrics = monitoringService.getLatestMetrics(id);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/{id}/metrics/history")
    public ResponseEntity<List<MetricData>> getMetricsHistory(
            @PathVariable Long id,
            @RequestParam(required = false) String metric,
            @RequestParam(required = false, defaultValue = "1h") String timeRange) {
        
        List<MetricData> history = monitoringService.getMetricHistory(id, metric, timeRange);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/metrics/report")
    public ResponseEntity<Void> reportMetrics(@RequestBody MetricReportRequest request) {
        monitoringService.reportMetrics(request);
        return ResponseEntity.ok().build();
    }

    private ServerDTO toDTO(TargetServer server) {
        ServerDTO dto = new ServerDTO();
        dto.setId(server.getId());
        dto.setName(server.getName());
        dto.setDescription(server.getDescription());
        dto.setEndpoint(server.getEndpoint());
        dto.setGroupName(server.getGroupName());
        dto.setTags(server.getTags());
        dto.setStatus(server.getStatus().name());
        dto.setAgentId(server.getAgentId());
        
        if (server.getCreatedAt() != null) {
            dto.setCreatedAt(server.getCreatedAt().toString());
        }
        
        return dto;
    }
}
