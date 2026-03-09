package com.aiops.command.approval;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<ApprovalEntity, String> {
    List<ApprovalEntity> findByStatus(String status);
    List<ApprovalEntity> findByAgentId(String agentId);
}
