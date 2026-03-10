package com.aiops.action.repository;
import com.aiops.action.model.ActionExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActionExecutionRepository extends JpaRepository<ActionExecution, Long> {
    List<ActionExecution> findByActionId(Long actionId);
    List<ActionExecution> findByStatus(ActionExecution.ExecutionStatus status);
    List<ActionExecution> findByServerId(Long serverId);
}
