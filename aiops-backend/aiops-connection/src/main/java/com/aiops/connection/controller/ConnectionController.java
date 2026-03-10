package com.aiops.connection.controller;

import com.aiops.connection.client.AgentClient;
import com.aiops.connection.dto.*;
import com.aiops.connection.model.AgentConnection;
import com.aiops.connection.service.ConnectionService;
import com.aiops.connection.service.PairingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/connections")
public class ConnectionController {

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private PairingService pairingService;

    @Autowired
    private AgentClient agentClient;

    @GetMapping
    public ResponseEntity<List<ConnectionDTO>> list(
            @RequestParam(required = false) String status) {
        
        List<AgentConnection> connections;
        if (status != null && !status.isEmpty()) {
            connections = connectionService.findByStatus(AgentConnection.ConnectionStatus.valueOf(status.toUpperCase()));
        } else {
            connections = connectionService.findAll();
        }
        
        List<ConnectionDTO> dtos = connections.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConnectionDTO> get(@PathVariable String id) {
        return connectionService.findById(id)
                .map(conn -> ResponseEntity.ok(toDTO(conn)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ConnectionDTO> create(@RequestBody CreateConnectionRequest request) {
        AgentConnection conn = connectionService.create(request);
        return ResponseEntity.ok(toDTO(conn));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConnectionDTO> update(
            @PathVariable String id,
            @RequestBody CreateConnectionRequest request) {
        AgentConnection conn = connectionService.update(id, request);
        return ResponseEntity.ok(toDTO(conn));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        connectionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ConnectionDTO> approve(@PathVariable String id) {
        // TODO: 获取当前用户ID
        AgentConnection conn = connectionService.approve(id, 1L);
        return ResponseEntity.ok(toDTO(conn));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ConnectionDTO> reject(@PathVariable String id) {
        AgentConnection conn = connectionService.reject(id);
        return ResponseEntity.ok(toDTO(conn));
    }

    @PostMapping("/{id}/disconnect")
    public ResponseEntity<ConnectionDTO> disconnect(@PathVariable String id) {
        AgentConnection conn = connectionService.disconnect(id);
        return ResponseEntity.ok(toDTO(conn));
    }

    @PostMapping("/{id}/connect")
    public ResponseEntity<ConnectionDTO> connect(@PathVariable String id) {
        return connectionService.findById(id)
                .map(conn -> {
                    // 调用Agent进行配对
                    PairingRequest request = new PairingRequest();
                    request.setPairingToken(conn.getPairingToken());
                    request.setServerUrl(pairingService.getServerUrl());
                    request.setEndpoint(conn.getEndpoint());
                    
                    PairingResponse response = agentClient.pair(conn.getEndpoint(), request);
                    
                    if ("APPROVED".equals(response.getStatus())) {
                        connectionService.updateLastConnected(id);
                        return ResponseEntity.ok(toDTO(connectionService.findById(id).get()));
                    } else {
                        return ResponseEntity.badRequest().<ConnectionDTO>body(toDTO(conn));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<Boolean> testConnection(@PathVariable String id) {
        return connectionService.findById(id)
                .map(conn -> ResponseEntity.ok(agentClient.testConnection(conn.getEndpoint())))
                .orElse(ResponseEntity.notFound().build());
    }

    private ConnectionDTO toDTO(AgentConnection conn) {
        ConnectionDTO dto = new ConnectionDTO();
        dto.setId(conn.getId());
        dto.setName(conn.getName());
        dto.setDescription(conn.getDescription());
        dto.setEndpoint(conn.getEndpoint());
        dto.setGroupName(conn.getGroupName());
        dto.setTags(conn.getTags());
        dto.setStatus(conn.getStatus().name());
        dto.setPairingToken(conn.getPairingToken());
        dto.setAuthorizationToken(conn.getAuthorizationToken());
        
        if (conn.getLastConnectedAt() != null) {
            dto.setLastConnectedAt(conn.getLastConnectedAt().toString());
        }
        if (conn.getCreatedAt() != null) {
            dto.setCreatedAt(conn.getCreatedAt().toString());
        }
        
        return dto;
    }
}
