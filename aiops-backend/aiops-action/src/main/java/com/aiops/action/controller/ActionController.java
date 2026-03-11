package com.aiops.action.controller;

import com.aiops.action.model.Action;
import com.aiops.action.model.ActionExecution;
import com.aiops.action.service.ActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ActionController {
    @Autowired private ActionService actionService;

    @GetMapping("/actions")
    public ResponseEntity<List<Action>> listActions() {
        return ResponseEntity.ok(actionService.findAllActions());
    }

    @PostMapping("/actions")
    public ResponseEntity<Action> createAction(@RequestBody Action action) {
        return ResponseEntity.ok(actionService.createAction(action));
    }

    @DeleteMapping("/actions/{id}")
    public ResponseEntity<Void> deleteAction(@PathVariable Long id) {
        actionService.deleteAction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/actions/executions")
    public ResponseEntity<List<ActionExecution>> listExecutions(@RequestParam(required = false) String status) {
        if (status != null) {
            return ResponseEntity.ok(actionService.findExecutionsByStatus(ActionExecution.ExecutionStatus.valueOf(status.toUpperCase())));
        }
        return ResponseEntity.ok(actionService.findAllExecutions());
    }

    @PostMapping("/actions/{id}/execute")
    public ResponseEntity<ActionExecution> executeAction(@PathVariable Long id, @RequestBody Map<String, Object> context) {
        return ResponseEntity.ok(actionService.execute(id, context));
    }

    @PostMapping("/actions/executions/{id}/cancel")
    public ResponseEntity<ActionExecution> cancelExecution(@PathVariable Long id) {
        return ResponseEntity.ok(actionService.cancelExecution(id));
    }
}
