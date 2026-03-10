package com.aiops.mcp.server;

import com.aiops.mcp.service.MCPService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api/mcp")
public class MCPProtocolController {

    private final MCPServerRunner mcpServer;
    private final MCPService mcpService;
    private final Map<String, BlockingQueue<Map<String, Object>>> sessions = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MCPProtocolController(MCPServerRunner mcpServer, MCPService mcpService) {
        this.mcpServer = mcpServer;
        this.mcpService = mcpService;
    }

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sse() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        String sessionId = UUID.randomUUID().toString();
        BlockingQueue<Map<String, Object>> queue = new LinkedBlockingQueue<>();
        sessions.put(sessionId, queue);

        executor.submit(() -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("initialized")
                    .data(Map.of("protocolVersion", "2024-11-05", "sessionId", sessionId)));

                emitter.send(SseEmitter.event()
                    .name("tools")
                    .data(Map.of("tools", mcpServer.listTools())));

                while (true) {
                    Map<String, Object> message = queue.poll(30, TimeUnit.SECONDS);
                    if (message != null) {
                        emitter.send(SseEmitter.event()
                            .name("message")
                            .data(message));
                    } else {
                        emitter.send(SseEmitter.event()
                            .name("ping")
                            .data(Map.of("timestamp", System.currentTimeMillis())));
                    }
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                sessions.remove(sessionId);
            }
        });

        return emitter;
    }

    @PostMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> sendSessionMessage(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> request) {
        
        BlockingQueue<Map<String, Object>> queue = sessions.get(sessionId);
        if (queue == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid session"));
        }

        String method = (String) request.get("method");
        Map<String, Object> params = (Map<String, Object>) request.get("params");

        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", request.get("id"));

        try {
            switch (method) {
                case "tools/list":
                    response.put("result", Map.of("tools", mcpServer.listTools()));
                    break;
                case "tools/call":
                    String toolName = (String) params.get("name");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
                    Object result = mcpServer.callTool(toolName, arguments != null ? arguments : new HashMap<>());
                    response.put("result", Map.of("content", List.of(Map.of("type", "text", "text", result.toString()))));
                    break;
                case "resources/list":
                    response.put("result", Map.of("resources", List.of()));
                    break;
                default:
                    response.put("error", Map.of("code", -32601, "message", "Method not found"));
            }
        } catch (Exception e) {
            response.put("error", Map.of("code", -32603, "message", e.getMessage()));
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/tools/list")
    public ResponseEntity<Map<String, Object>> listTools() {
        return ResponseEntity.ok(Map.of(
            "tools", mcpServer.listTools()
        ));
    }

    @PostMapping("/tools/call")
    public ResponseEntity<Map<String, Object>> callTool(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) request.get("arguments");
        
        Object result = mcpServer.callTool(name, arguments != null ? arguments : new HashMap<>());
        
        return ResponseEntity.ok(Map.of(
            "content", List.of(Map.of(
                "type", "text",
                "text", result.toString()
            ))
        ));
    }

    @GetMapping("/stdio")
    public ResponseEntity<String> stdioManifest() {
        String manifest = String.format("""
            {
                "manifestVersion": "1.0",
                "name": "AIOps MCP Server",
                "version": "1.0.0",
                "description": "AIOps智能运维平台MCP服务",
                "tools": %s
            }
            """, mcpServer.getToolsJson());
        
        return ResponseEntity.ok()
            .header("Content-Type", "application/json")
            .body(manifest);
    }

    @PostMapping("/stdio/call")
    public ResponseEntity<Map<String, Object>> stdioCall(@RequestBody Map<String, Object> request) {
        String method = (String) request.get("method");
        Map<String, Object> params = (Map<String, Object>) request.get("params");
        
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", request.get("id"));

        try {
            if ("tools/call".equals(method)) {
                String name = (String) params.get("name");
                @SuppressWarnings("unchecked")
                Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
                Object result = mcpServer.callTool(name, arguments != null ? arguments : new HashMap<>());
                response.put("result", Map.of("content", List.of(Map.of("type", "text", "text", result.toString()))));
            } else if ("tools/list".equals(method)) {
                response.put("result", Map.of("tools", mcpServer.listTools()));
            }
        } catch (Exception e) {
            response.put("error", Map.of("code", -32603, "message", e.getMessage()));
        }

        return ResponseEntity.ok(response);
    }

    public static class MCPRequest {
        public String jsonrpc = "2.0";
        public String id;
        public String method;
        public Map<String, Object> params;
    }

    public static class MCPResponse {
        public String jsonrpc = "2.0";
        public String id;
        public Object result;
        public Map<String, Object> error;
    }
}
