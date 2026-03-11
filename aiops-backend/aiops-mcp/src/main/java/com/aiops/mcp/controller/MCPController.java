package com.aiops.mcp.controller;

import com.aiops.mcp.model.MCPServer;
import com.aiops.mcp.model.MCPTool;
import com.aiops.mcp.service.MCPService;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mcp")
public class MCPController {
    private final MCPService mcpService;

    public MCPController(MCPService mcpService) {
        this.mcpService = mcpService;
    }

    @PostConstruct
    public void init() {
        mcpService.initializeDefaultServers();
    }

    @GetMapping("/servers")
    public List<MCPServer> listServers() { return mcpService.findAllServers(); }

    @PostMapping("/servers")
    public MCPServer createServer(@RequestBody MCPServer server) { return mcpService.saveServer(server); }

    @GetMapping("/servers/{id}")
    public MCPServer getServer(@PathVariable String id) { return mcpService.findServerById(id); }

    @PutMapping("/servers/{id}")
    public MCPServer updateServer(@PathVariable String id, @RequestBody MCPServer server) {
        server.setId(id);
        return mcpService.saveServer(server);
    }

    @DeleteMapping("/servers/{id}")
    public void deleteServer(@PathVariable String id) { mcpService.deleteServer(id); }

    @PostMapping("/servers/{id}/test")
    public Map<String, Object> testConnection(@PathVariable String id) {
        boolean success = mcpService.testConnection(id);
        return Map.of("success", success, "message", success ? "Connection successful" : "Connection failed");
    }

    @GetMapping("/tools")
    public List<MCPTool> listTools() { return mcpService.findAllTools(); }

    @GetMapping("/tools/definitions")
    public String toolDefinitions() { return mcpService.getToolDefinitions(); }

    @GetMapping("/servers/{id}/tools")
    public List<MCPTool> listServerTools(@PathVariable String id) { return mcpService.findToolsByServer(id); }

    @PostMapping("/tools")
    public MCPTool createTool(@RequestBody MCPTool tool) { return mcpService.saveTool(tool); }

    @DeleteMapping("/tools/{id}")
    public void deleteTool(@PathVariable String id) { mcpService.deleteTool(id); }

    @PostMapping("/call")
    public Map<String, Object> callTool(@RequestBody Map<String, Object> request) {
        String toolId = (String) request.get("tool_id");
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) request.get("params");
        return mcpService.callTool(toolId, params);
    }
}
