# MCP (Model Context Protocol) 模块

> 模块：aiops-mcp
> 版本：V1.0

---

## 一、架构概述

### 1.1 设计目标

本模块负责**扩展外部能力**，通过MCP协议让AI可以调用各种外部服务和工具。

### 1.2 MCP 核心思想

```
┌─────────────────────────────────────────────────────────────────────┐
│                      MCP 架构                                        │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    MCP Server                                │   │
│   │                                                             │   │
│   │   ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐    │   │
│   │   │ 钉钉MCP  │  │ GitHub  │  │ 数据库   │  │ 自定义   │    │   │
│   │   │         │  │   MCP   │  │   MCP   │  │   MCP   │    │   │
│   │   └─────────┘  └─────────┘  └─────────┘  └─────────┘    │   │
│   │                                                             │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                 │                                    │
│                                 ▼                                    │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    MCP Registry                              │   │
│   │                                                             │   │
│   │   • 工具定义 (给AI提供元数据)                                │   │
│   │   • 能力描述                                                │   │
│   │   • 参数Schema                                             │   │
│   │                                                             │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                 │                                    │
│                                 ▼                                    │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    AI 调用                                   │   │
│   │                                                             │   │
│   │   AI ──▶ 工具调用 ──▶ MCP Server ──▶ 外部服务             │   │
│   │       ◀─── 返回结果 ────                                   │   │
│   │                                                             │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 1.3 与Skill的区别

| 特性 | Skill | MCP |
|------|-------|-----|
| 位置 | 目标服务器Agent | AIOps后端 |
| 调用方式 | Agent执行脚本 | AI直接调用外部服务 |
| 用途 | 运维操作 | 扩展AI能力 |
| 示例 | 重启服务、查看日志 | 发送通知、查询GitHub |

---

## 二、MCP Server 定义

### 2.1 MCPServer 实体

```java
@Entity
@Table(name = "mcp_servers")
public class MCPServer {
    
    @Id
    private String id;               // "dingtalk", "github", "database"
    
    private String name;            // "钉钉通知"
    private String description;     // "发送钉钉消息"
    
    @Enumerated(EnumType.STRING)
    private MCPType type;          // DINGTALK / GITHUB / DATABASE / HTTP / CUSTOM
    
    // 连接配置
    private String endpoint;        // HTTP端点
    private String apiKey;          // API Key
    private String credentials;     // 认证信息 (加密)
    
    // 状态
    @Enumerated(EnumType.STRING)
    private ServerStatus status;   // ACTIVE / INACTIVE / ERROR
    
    private Boolean enabled;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum MCPType {
    DINGTALK,     // 钉钉
    WECOM,        // 企业微信
    FEISHU,       // 飞书
    GITHUB,       // GitHub
    GITLAB,       // GitLab
    DATABASE,     // 数据库
    HTTP,         // HTTP API
    CUSTOM        // 自定义
}

public enum ServerStatus {
    ACTIVE,       // 活跃
    INACTIVE,     // 未激活
    ERROR         // 错误
}
```

### 2.2 MCPTool 定义

```java
@Entity
@Table(name = "mcp_tools")
public class MCPTool {
    
    @Id
    private String id;               // "dingtalk.send_message"
    
    private String serverId;         // 所属Server
    
    private String name;            // "send_message"
    private String description;     // "发送钉钉消息"
    
    // 工具定义 (MCP格式)
    @Column(length = 5000)
    private String definition;      // MCP格式的工具定义
    
    // 参数Schema
    @Column(length = 3000)
    private String inputSchema;     // JSON Schema
    
    // 示例
    @ElementCollection
    private List<String> examples;  // 使用示例
    
    private Boolean enabled;
    
    private LocalDateTime createdAt;
}
```

---

## 三、MCP 工具类型

### 3.1 通知类 MCP

```java
// 钉钉MCP
@Component
public class DingTalkMCPServer implements MCPServerInterface {
    
    @Override
    public ToolResult execute(String toolName, Map<String, Object> params) {
        switch (toolName) {
            case "send_message":
                return sendMessage(params);
            case "send_markdown":
                return sendMarkdown(params);
            case "create_webhook":
                return createWebhook(params);
            default:
                return ToolResult.fail("Unknown tool: " + toolName);
        }
    }
    
    @Override
    public List<MCPTool> getTools() {
        return List.of(
            MCPTool.builder()
                .id("dingtalk.send_message")
                .name("send_message")
                .description("发送钉钉文本消息")
                .definition(getSendMessageDefinition())
                .build(),
            MCPTool.builder()
                .id("dingtalk.send_markdown")
                .name("send_markdown")
                .description("发送钉钉Markdown消息")
                .definition(getSendMarkdownDefinition())
                .build()
        );
    }
    
    private String getSendMessageDefinition() {
        return """
        {
            "name": "dingtalk_send_message",
            "description": "发送钉钉文本消息给指定用户或群组",
            "inputSchema": {
                "type": "object",
                "properties": {
                    "webhook": {
                        "type": "string",
                        "description": "钉钉Webhook地址"
                    },
                    "content": {
                        "type": "string",
                        "description": "消息内容"
                    },
                    "at": {
                        "type": "array",
                        "description": "@用户手机号列表",
                        "items": {"type": "string"}
                    }
                },
                "required": ["webhook", "content"]
            }
        }
        """;
    }
}
```

### 3.2 GitHub MCP

```java
@Component
public class GitHubMCPServer implements MCPServerInterface {
    
    @Override
    public List<MCPTool> getTools() {
        return List.of(
            MCPTool.builder()
                .id("github.get_issues")
                .name("get_issues")
                .description("获取GitHub仓库的Issue列表")
                .definition(getIssuesDefinition())
                .build(),
            MCPTool.builder()
                .id("github.create_issue")
                .name("create_issue")
                .description("创建GitHub Issue")
                .definition(getCreateIssueDefinition())
                .build(),
            MCPTool.builder()
                .id("github.get_workflow_runs")
                .name("get_workflow_runs")
                .description("获取GitHub Actions工作流运行状态")
                .definition(getWorkflowDefinition())
                .build()
        );
    }
    
    private String getIssuesDefinition() {
        return """
        {
            "name": "github_get_issues",
            "description": "获取GitHub仓库的Issue列表",
            "inputSchema": {
                "type": "object",
                "properties": {
                    "owner": {"type": "string", "description": "仓库所有者"},
                    "repo": {"type": "string", "description": "仓库名称"},
                    "state": {"type": "string", "enum": ["open", "closed", "all"]},
                    "labels": {"type": "string", "description": "标签筛选"}
                },
                "required": ["owner", "repo"]
            }
        }
        """;
    }
}
```

### 3.3 数据库 MCP

```java
@Component
public class DatabaseMCPServer implements MCPServerInterface {
    
    @Override
    public List<MCPTool> getTools() {
        return List.of(
            MCPTool.builder()
                .id("db.query")
                .name("query")
                .description("执行SQL查询")
                .definition(getQueryDefinition())
                .build(),
            MCPTool.builder()
                .id("db.execute")
                .name("execute")
                .description("执行SQL语句")
                .definition(getExecuteDefinition())
                .build()
        );
    }
}
```

### 3.4 HTTP MCP

```java
@Component
public class HttpMCPServer implements MCPServerInterface {
    
    @Override
    public ToolResult execute(String toolName, Map<String, Object> params) {
        String method = (String) params.get("method");
        String url = (String) params.get("url");
        Map<String, Object> headers = (Map<String, Object>) params.get("headers");
        Object body = params.get("body");
        
        // 执行HTTP请求
        HttpResponse resp = httpClient.execute(method, url, headers, body);
        
        return ToolResult.builder()
            .success(resp.getStatusCode() < 400)
            .data(Map.of(
                "status", resp.getStatusCode(),
                "headers", resp.getHeaders(),
                "body", resp.getBody()
            ))
            .build();
    }
}
```

---

## 四、MCP Registry

### 4.1 注册表服务

```java
@Service
public class MCPRegistry {
    
    @Autowired
    private List<MCPServerInterface> servers;
    
    // 获取所有可用工具
    public List<MCPTool> getAllTools() {
        return servers.stream()
            .filter(s -> s.isEnabled())
            .flatMap(s -> s.getTools().stream())
            .filter(MCPTool::getEnabled)
            .collect(Collectors.toList());
    }
    
    // 获取工具定义 (给AI用)
    public String getToolDefinitions() {
        List<MCPTool> tools = getAllTools();
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < tools.size(); i++) {
            sb.append(tools.get(i).getDefinition());
            if (i < tools.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        
        return sb.toString();
    }
    
    // 执行工具
    public ToolResult executeTool(String toolId, Map<String, Object> params) {
        // 解析toolId: "dingtalk.send_message" -> server="dingtalk", tool="send_message"
        String[] parts = toolId.split("\\.", 2);
        String serverId = parts[0];
        String toolName = parts[1];
        
        MCPServerInterface server = servers.stream()
            .filter(s -> s.getId().equals(serverId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Server not found: " + serverId));
        
        return server.execute(toolName, params);
    }
}
```

---

## 五、与Tool系统集成

### 5.1 MCP作为Tool

```java
@Component
public class MCPToolExecutor implements ToolExecutor {
    
    @Autowired
    private MCPRegistry mcpRegistry;
    
    @Override
    public ToolResult execute(Map<String, Object> params) {
        String toolId = (String) params.get("tool_id");
        Map<String, Object> toolParams = (Map<String, Object>) params.get("params");
        
        return mcpRegistry.executeTool(toolId, toolParams);
    }
    
    @Override
    public String getDefinition() {
        return """
        {
            "type": "function",
            "function": {
                "name": "mcp_call",
                "description": "调用MCP扩展工具，用于发送通知、查询外部服务等",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "tool_id": {
                            "type": "string",
                            "description": "工具ID，如 'dingtalk.send_message'"
                        },
                        "params": {
                            "type": "object",
                            "description": "工具参数"
                        }
                    },
                    "required": ["tool_id", "params"]
                }
            }
        }
        """;
    }
    
    @Override
    public ToolType getType() {
        return ToolType.MCP;
    }
}
```

---

## 六、API 设计

### 6.1 MCP Server管理 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/mcp/servers | Server列表 |
| POST | /api/v1/mcp/servers | 添加Server |
| GET | /api/v1/mcp/servers/{id} | Server详情 |
| PUT | /api/v1/mcp/servers/{id} | 更新Server |
| DELETE | /api/v1/mcp/servers/{id} | 删除Server |
| POST | /api/v1/mcp/servers/{id}/test | 测试连接 |

### 6.2 MCP Tool API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/mcp/tools | 工具列表 |
| GET | /api/v1/mcp/tools/definitions | AI用工具定义 |

### 6.3 MCP 调用 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/v1/mcp/call | 调用MCP工具 |

---

## 七、实施计划

| 序号 | 工作内容 | 优先级 |
|------|----------|--------|
| 1 | MCPServer 实体 + Registry | P0 |
| 2 | MCPServer接口 + 基础实现 | P0 |
| 3 | 钉钉MCP | P1 |
| 4 | GitHub MCP | P1 |
| 5 | 数据库MCP | P1 |
| 6 | HTTP MCP | P1 |
| 7 | 前端MCP管理页面 | P2 |

---

> 模块版本：V1.0
> 最后更新：2025-03-09
