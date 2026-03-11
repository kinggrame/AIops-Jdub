package com.aiops.connection.service;

import com.aiops.connection.dto.CreateConnectionRequest;
import com.aiops.connection.model.AgentConnection;
import com.aiops.connection.repository.ConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConnectionService {

    @Autowired
    private ConnectionRepository repository;

    @Autowired
    private PairingService pairingService;

    public List<AgentConnection> findAll() {
        return repository.findAll();
    }

    public Optional<AgentConnection> findById(String id) {
        return repository.findById(id);
    }

    public List<AgentConnection> findByStatus(AgentConnection.ConnectionStatus status) {
        return repository.findByStatus(status);
    }

    public Optional<AgentConnection> findByToken(String token) {
        Optional<AgentConnection> byAuth = repository.findByAuthorizationToken(token);
        if (byAuth.isPresent()) {
            return byAuth;
        }
        return repository.findByPairingToken(token);
    }

    public Optional<AgentConnection> findByAuthorizationToken(String token) {
        return repository.findByAuthorizationToken(token);
    }

    @Transactional
    public AgentConnection create(CreateConnectionRequest request) {
        AgentConnection conn = new AgentConnection();
        conn.setId(UUID.randomUUID().toString());
        conn.setName(request.getName());
        conn.setDescription(request.getDescription());
        conn.setEndpoint(request.getEndpoint());
        conn.setPairingToken(request.getPairingToken());
        conn.setGroupName(request.getGroupName());
        conn.setTags(request.getTags());
        conn.setStatus(AgentConnection.ConnectionStatus.PENDING);
        conn.setCreatedAt(LocalDateTime.now());
        
        return repository.save(conn);
    }

    @Transactional
    public AgentConnection update(String id, CreateConnectionRequest request) {
        AgentConnection conn = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Connection not found: " + id));
        
        if (request.getName() != null) {
            conn.setName(request.getName());
        }
        if (request.getDescription() != null) {
            conn.setDescription(request.getDescription());
        }
        if (request.getEndpoint() != null) {
            conn.setEndpoint(request.getEndpoint());
        }
        if (request.getGroupName() != null) {
            conn.setGroupName(request.getGroupName());
        }
        if (request.getTags() != null) {
            conn.setTags(request.getTags());
        }
        
        conn.setUpdatedAt(LocalDateTime.now());
        return repository.save(conn);
    }

    @Transactional
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Transactional
    public AgentConnection approve(String id, Long approvedBy) {
        AgentConnection conn = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Connection not found: " + id));
        
        conn.setStatus(AgentConnection.ConnectionStatus.CONNECTED);
        conn.setApprovedBy(approvedBy);
        conn.setApprovedAt(LocalDateTime.now());
        conn.setUpdatedAt(LocalDateTime.now());
        
        return repository.save(conn);
    }

    @Transactional
    public AgentConnection reject(String id) {
        AgentConnection conn = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Connection not found: " + id));
        
        conn.setStatus(AgentConnection.ConnectionStatus.REJECTED);
        conn.setUpdatedAt(LocalDateTime.now());
        
        return repository.save(conn);
    }

    @Transactional
    public AgentConnection disconnect(String id) {
        AgentConnection conn = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Connection not found: " + id));
        
        conn.setStatus(AgentConnection.ConnectionStatus.DISCONNECTED);
        conn.setUpdatedAt(LocalDateTime.now());
        
        return repository.save(conn);
    }

    @Transactional
    public AgentConnection updateHeartbeat(String id) {
        AgentConnection conn = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Connection not found: " + id));
        
        conn.setLastHeartbeatAt(LocalDateTime.now());
        
        return repository.save(conn);
    }

    @Transactional
    public AgentConnection updateLastConnected(String id) {
        AgentConnection conn = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Connection not found: " + id));
        
        conn.setLastConnectedAt(LocalDateTime.now());
        
        return repository.save(conn);
    }
}
