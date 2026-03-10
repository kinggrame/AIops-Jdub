package com.aiops.agent.memory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AgentMemoryRepository extends JpaRepository<AgentMemory, String> {
    List<AgentMemory> findBySessionIdOrderByLastUpdatedDesc(String sessionId);
    List<AgentMemory> findByUserIdOrderByLastUpdatedDesc(String userId);
}
