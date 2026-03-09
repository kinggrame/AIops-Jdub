package com.aiops.command.approval;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Primary
public class JpaApprovalService implements ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final ObjectMapper objectMapper;

    public JpaApprovalService(ApprovalRepository approvalRepository, ObjectMapper objectMapper) {
        this.approvalRepository = approvalRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public ApprovalRequest create(String agentId, String command, Map<String, Object> params, String reason) {
        String approvalId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        
        ApprovalEntity entity = new ApprovalEntity(
                approvalId, agentId, command, 
                toJson(params), reason, "pending", 
                null, now, null);
        
        approvalRepository.save(entity);
        return toApprovalRequest(entity);
    }

    @Override
    public ApprovalRequest approve(String approvalId, String reviewer) {
        Optional<ApprovalEntity> opt = approvalRepository.findById(approvalId);
        if (opt.isEmpty()) return null;
        
        ApprovalEntity entity = opt.get();
        entity.setStatus("approved");
        entity.setReviewer(reviewer);
        entity.setReviewedAt(Instant.now());
        approvalRepository.save(entity);
        return toApprovalRequest(entity);
    }

    @Override
    public ApprovalRequest reject(String approvalId, String reviewer) {
        Optional<ApprovalEntity> opt = approvalRepository.findById(approvalId);
        if (opt.isEmpty()) return null;
        
        ApprovalEntity entity = opt.get();
        entity.setStatus("rejected");
        entity.setReviewer(reviewer);
        entity.setReviewedAt(Instant.now());
        approvalRepository.save(entity);
        return toApprovalRequest(entity);
    }

    @Override
    public List<ApprovalRequest> list() {
        return approvalRepository.findAll().stream()
                .map(this::toApprovalRequest)
                .collect(Collectors.toList());
    }

    public Optional<ApprovalRequest> findById(String approvalId) {
        return approvalRepository.findById(approvalId).map(this::toApprovalRequest);
    }

    public List<ApprovalRequest> findByAgentId(String agentId) {
        return approvalRepository.findByAgentId(agentId).stream()
                .map(this::toApprovalRequest)
                .collect(Collectors.toList());
    }

    public List<ApprovalRequest> findPending() {
        return approvalRepository.findByStatus("pending").stream()
                .map(this::toApprovalRequest)
                .collect(Collectors.toList());
    }

    private ApprovalRequest toApprovalRequest(ApprovalEntity entity) {
        Map<String, Object> params = fromJson(entity.getParams());
        return new ApprovalRequest(
                entity.getApprovalId(),
                entity.getAgentId(),
                entity.getCommand(),
                params,
                entity.getReason(),
                entity.getStatus(),
                entity.getReviewer(),
                entity.getCreatedAt(),
                entity.getReviewedAt()
        );
    }

    private String toJson(Map<String, Object> params) {
        try {
            return params != null ? objectMapper.writeValueAsString(params) : null;
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Map<String, Object> fromJson(String json) {
        if (json == null) return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }
}
