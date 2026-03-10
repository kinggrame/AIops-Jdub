package com.aiops.action.service;

import com.aiops.action.model.Action;
import com.aiops.action.model.ActionExecution;
import com.aiops.action.repository.ActionRepository;
import com.aiops.action.repository.ActionExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ActionService {
    @Autowired private ActionRepository actionRepository;
    @Autowired private ActionExecutionRepository executionRepository;

    public List<Action> findAllActions() { return actionRepository.findAll(); }
    public Optional<Action> findActionById(Long id) { return actionRepository.findById(id); }
    
    @Transactional
    public Action createAction(Action action) {
        action.setCreatedAt(LocalDateTime.now());
        return actionRepository.save(action);
    }

    @Transactional
    public void deleteAction(Long id) { actionRepository.deleteById(id); }

    public List<ActionExecution> findAllExecutions() { return executionRepository.findAll(); }
    public List<ActionExecution> findExecutionsByStatus(ActionExecution.ExecutionStatus status) {
        return executionRepository.findByStatus(status);
    }

    @Transactional
    public ActionExecution execute(Long actionId, Map<String, Object> context) {
        Action action = actionRepository.findById(actionId).orElseThrow();
        
        ActionExecution execution = new ActionExecution();
        execution.setActionId(actionId);
        execution.setActionName(action.getName());
        execution.setServerId(context.get("serverId") != null ? ((Number)context.get("serverId")).longValue() : null);
        execution.setAlertId(context.get("alertId") != null ? ((Number)context.get("alertId")).longValue() : null);
        execution.setStatus(ActionExecution.ExecutionStatus.RUNNING);
        execution.setStartTime(LocalDateTime.now());
        execution = executionRepository.save(execution);

        try {
            // Execute based on action type
            String result = executeAction(action, context);
            execution.setStatus(ActionExecution.ExecutionStatus.SUCCESS);
            execution.setOutput(result);
        } catch (Exception e) {
            execution.setStatus(ActionExecution.ExecutionStatus.FAILED);
            execution.setErrorMessage(e.getMessage());
        }
        
        execution.setEndTime(LocalDateTime.now());
        execution.setDuration(java.time.Duration.between(execution.getStartTime(), execution.getEndTime()).toMillis());
        return executionRepository.save(execution);
    }

    private String executeAction(Action action, Map<String, Object> context) {
        // Placeholder - actual implementation would call Script/Skill executor
        return "Action executed: " + action.getName();
    }

    @Transactional
    public ActionExecution cancelExecution(Long executionId) {
        ActionExecution execution = executionRepository.findById(executionId).orElseThrow();
        execution.setStatus(ActionExecution.ExecutionStatus.CANCELLED);
        execution.setEndTime(LocalDateTime.now());
        return executionRepository.save(execution);
    }
}
