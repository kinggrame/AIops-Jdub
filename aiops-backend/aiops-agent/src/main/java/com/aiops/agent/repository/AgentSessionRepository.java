package com.aiops.agent.repository;

import com.aiops.agent.model.AgentSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AgentSessionRepository extends JpaRepository<AgentSession, String> {
    List<AgentSession> findByUserIdOrderByCreatedAtDesc(String userId);
}
