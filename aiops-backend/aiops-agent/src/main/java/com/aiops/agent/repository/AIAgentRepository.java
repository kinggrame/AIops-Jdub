package com.aiops.agent.repository;

import com.aiops.agent.model.AIAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AIAgentRepository extends JpaRepository<AIAgent, String> {
    List<AIAgent> findByEnabledTrue();
    Optional<AIAgent> findByType(AIAgent.AgentType type);
}
