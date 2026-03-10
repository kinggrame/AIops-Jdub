package com.aiops.mcp.service;

import com.aiops.mcp.model.MCPServer;
import com.aiops.mcp.model.MCPTool;
import com.aiops.mcp.repository.MCPServerRepository;
import com.aiops.mcp.repository.MCPToolRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class MCPService {
    private final MCPServerRepository serverRepository;
    private final MCPToolRepository toolRepository;

    public MCPService(MCPServerRepository serverRepository, MCPToolRepository toolRepository) {
        this.serverRepository = serverRepository;
        this.toolRepository = toolRepository;
    }

    public List<MCPServer> findAllServers() { return serverRepository.findAll(); }
    public MCPServer findServerById(String id) { return serverRepository.findById(id).orElse(null); }
    public MCPServer saveServer(MCPServer server) {
        server.setUpdatedAt(java.time.LocalDateTime.now());
        return serverRepository.save(server);
    }
    public void deleteServer(String id) {
        toolRepository.deleteById(id);
        serverRepository.deleteById(id);
    }

    public List<MCPTool> findAllTools() { return toolRepository.findAll(); }
    public List<MCPTool> findToolsByServer(String serverId) { return toolRepository.findByServerId(serverId); }
    public MCPTool saveTool(MCPTool tool) { return toolRepository.save(tool); }
    public void deleteTool(String id) { toolRepository.deleteById(id); }

    public String getToolDefinitions() {
        List<MCPTool> tools = toolRepository.findByEnabledTrue();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < tools.size(); i++) {
            if (tools.get(i).getDefinition() != null) {
                if (i > 0) sb.append(",");
                sb.append(tools.get(i).getDefinition());
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public Map<String, Object> callTool(String toolId, Map<String, Object> params) {
        MCPTool tool = toolRepository.findById(toolId).orElse(null);
        if (tool == null || !tool.getEnabled()) {
            throw new RuntimeException("Tool not found or disabled: " + toolId);
        }

        MCPServer server = serverRepository.findById(tool.getServerId()).orElse(null);
        if (server == null || !server.getEnabled()) {
            throw new RuntimeException("Server not found or disabled: " + tool.getServerId());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("toolId", toolId);
        result.put("toolName", tool.getName());
        result.put("server", server.getName());
        result.put("message", "Simulated MCP call executed");
        result.put("params", params);
        return result;
    }

    public boolean testConnection(String serverId) {
        MCPServer server = findServerById(serverId);
        if (server == null) {
            return false;
        }
        return server.getApiKey() != null && !server.getApiKey().isEmpty();
    }

    public void initializeDefaultServers() {
        if (serverRepository.count() == 0) {
            createDingTalkServer();
            createGitHubServer();
        }
    }

    private void createDingTalkServer() {
        MCPServer server = new MCPServer();
        server.setId("dingtalk");
        server.setName("钉钉通知");
        server.setDescription("发送钉钉消息通知");
        server.setType(MCPServer.MCPType.DINGTALK);
        server.setEnabled(true);
        server.setStatus(MCPServer.ServerStatus.INACTIVE);
        serverRepository.save(server);

        createTool("dingtalk.send_message", "dingtalk", "send_message", "发送钉钉文本消息",
            "{\"name\":\"dingtalk_send_message\",\"description\":\"发送钉钉文本消息\",\"inputSchema\":{\"type\":\"object\",\"properties\":{\"webhook\":{\"type\":\"string\"},\"content\":{\"type\":\"string\"}}}}");
    }

    private void createGitHubServer() {
        MCPServer server = new MCPServer();
        server.setId("github");
        server.setName("GitHub");
        server.setDescription("GitHub API集成");
        server.setType(MCPServer.MCPType.GITHUB);
        server.setEnabled(true);
        server.setStatus(MCPServer.ServerStatus.INACTIVE);
        serverRepository.save(server);

        createTool("github.get_issues", "github", "get_issues", "获取GitHub Issue列表",
            "{\"name\":\"github_get_issues\",\"description\":\"获取GitHub仓库Issue列表\",\"inputSchema\":{\"type\":\"object\",\"properties\":{\"owner\":{\"type\":\"string\"},\"repo\":{\"type\":\"string\"}}}}");
    }

    private void createTool(String id, String serverId, String name, String description, String definition) {
        MCPTool tool = new MCPTool();
        tool.setId(id);
        tool.setServerId(serverId);
        tool.setName(name);
        tool.setDescription(description);
        tool.setDefinition(definition);
        tool.setEnabled(true);
        toolRepository.save(tool);
    }
}
