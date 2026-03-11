package com.aiops.action.repository;
import com.aiops.action.model.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActionRepository extends JpaRepository<Action, Long> {
    List<Action> findByStatus(Action.ActionStatus status);
    List<Action> findByRuleId(Long ruleId);
    List<Action> findByType(Action.ActionType type);
}
