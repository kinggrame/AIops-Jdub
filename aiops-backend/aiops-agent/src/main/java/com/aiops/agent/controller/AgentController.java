package com.aiops.agent.controller;

import com.aiops.agent.model.*;
import com.aiops.agent.service.AgentService;
import com.aiops.agent.service.AgentCoordinator;
import jakarta.annotation.PostConstruct;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/agents")
public class AgentController {
    private final AgentService agentService;
    private final AgentCoordinator agentCoordinator;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public AgentController(AgentService agentService, AgentCoordinator agentCoordinator) {
        this.agentService = agentService;
        this.agentCoordinator = agentCoordinator;
    }

    @PostConstruct
    public void init() {
        agentService.initializeDefaultAgents();
    }

    @GetMapping
    public List<AIAgent> list() { 
        return agentService.findAllAgents(); 
    }

    @GetMapping("/enabled")
    public List<AIAgent> enabled() { 
        return agentService.findEnabledAgents(); 
    }

    @GetMapping("/{id}")
    public AIAgent get(@PathVariable String id) { 
        return agentService.findAgentById(id); 
    }

    @PutMapping("/{id}")
    public AIAgent update(@PathVariable String id, @RequestBody AIAgent agent) {
        agent.setId(id);
        return agentService.saveAgent(agent);
    }

    @PostMapping("/chat")
    public AgentSession chat(@RequestBody Map<String, String> request) {
        String userId = request.getOrDefault("userId", "anonymous");
        String message = request.get("message");
        return agentService.chat(userId, message);
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam String message, @RequestParam(defaultValue = "anonymous") String userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        String sessionId = UUID.randomUUID().toString();

        executor.submit(() -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("start")
                    .data(Map.of("sessionId", sessionId, "status", "processing")));

                String result = agentCoordinator.coordinate(sessionId, message);

                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(Map.of("content", result)));

                emitter.send(SseEmitter.event()
                    .name("end")
                    .data(Map.of("sessionId", sessionId, "status", "completed")));

                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @GetMapping("/tools")
    public String listTools() {
        return agentCoordinator.getToolDefinitions();
    }

    @GetMapping("/sessions/{id}")
    public AgentSession getSession(@PathVariable String id) {
        return agentService.getSession(id);
    }

    @GetMapping("/sessions/user/{userId}")
    public List<AgentSession> getUserSessions(@PathVariable String userId) {
        return agentService.getUserSessions(userId);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public List<AgentMessage> getSessionMessages(@PathVariable String sessionId) {
        return agentService.getSessionMessages(sessionId);
    }
}
