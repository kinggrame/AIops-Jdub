package com.aiops.audit.service;

import com.aiops.audit.model.AuditLog;
import com.aiops.audit.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLog log(String action, String resource, String resourceId, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setResource(resource);
        log.setResourceId(resourceId);
        log.setDetails(details);
        return auditLogRepository.save(log);
    }

    public void logFailure(String action, String resource, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setResource(resource);
        log.setDetails(details);
        log.setResult(AuditLog.AuditResult.FAILURE);
        auditLogRepository.save(log);
    }

    public Page<AuditLog> findAll(Pageable pageable) { return auditLogRepository.findAll(pageable); }
    public Page<AuditLog> findByUsername(String username, Pageable pageable) { 
        return auditLogRepository.findByUsername(username, pageable); 
    }
    public List<AuditLog> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByCreatedAtBetween(start, end);
    }
    public List<AuditLog> search(String keyword) {
        return auditLogRepository.findByActionContaining(keyword);
    }
}
