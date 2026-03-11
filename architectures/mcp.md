# MCP (Model Context Protocol) 模块

> 模块：aiops-mcp
> 版本：V2.0

---

## 一、架构概述

### 1.1 设计目标

本模块实现**MCP (Model Context Protocol)** 协议，支持外部应用（如Cherry Studio）通过标准化接口调用AIOps平台的工具能力。

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                        MCP 架构                                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌─────────────────┐              ┌─────────────────┐           │
│   │  外部MCP客户端  │              │   AIOps MCP服务  │           │
│   │                 │              │                  │           │
│   │ • Cherry Studio │◀─────────────▶│ • SSE 端点      │           │
│   │ • 其他MCP客户端  │   HTTP/SSE    │ • stdio 端点    │           │
│   │                 │              │ • Tool 注册     │           │
│   └─────────────────┘              └────────┬────────┘           │
│                                              │                      │
│   ┌─────────────────────────────────────────▼────────────────────┐  │
│   │                    Tool Execution Layer                      │  │
│   │                                                             │  │
│   │   Script | ELK | Milvus | Agent | Notify | MCP(Tool)      │  │
│   │                                                             │  │
│   └─────────────────────────────────────────────────────────────┘  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、核心组件

### 2.1 MCPServerRunner

```java
@Component
public class MCPServerRunner {
    
    private final Map<String, MCPToolHandler> tools = new ConcurrentHashMap<>();
    
    // 注册Tool
    public void registerTool(String name, String description, MCPToolHandler handler);
    
    // 列出Tools
    public List<Map<String, Object>> listTools();
    
    // 调用Tool
    public Object callTool(String name, Map<String, Object> params);
    
    // Tool处理器接口
    public interface MCPToolHandler {
        Object execute(Map<String, Object> params);
        String getDescription();
        Map<String, Object> getInputSchema();
    }
}
```

### 2.2 MCPProtocolController

```java
@RestController
@RequestMapping("/api/mcp")
public class MCPProtocolController {
    
    // SSE端点 - 实时推送
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sse();
    
    // 会话消息
    @PostMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> sendSessionMessage(...);
    
    // 工具列表
    @PostMapping("/tools/list")
    public ResponseEntity<Map<String, Object>> listTools();
    
    // 工具调用
    @PostMapping("/tools/call")
    public ResponseEntity<Map<String, Object>> callTool(...);
    
    // stdio清单
    @GetMapping("/stdio")
    public ResponseEntity<String> stdioManifest();
}
```

---

## 三、传输协议

### 3.1 SSE (Server-Sent Events)

适用于Web前端实时交互：

```javascript
// 前端连接
const emitter = new EventSource('http://localhost:8080/api/mcp/sse');

emitter.onmessage = (event) => {
    const data = JSON.parse(event.data);
    console.log('Received:', data);
};
```

### 3.2 stdio

适用于命令行工具和外部应用集成：

```
GET /api/mcp/stdio

Response:
{
    "manifestVersion": "1.0",
    "name": "AIOps MCP Server",
    "version": "1.0.0",
    "tools": [...]
}
```

---

## 四、API 设计

### 4.1 MCP协议端点

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/mcp/sse | SSE实时推送 |
| POST | /api/mcp/session/{id} | 会话消息 |
| POST | /api/mcp/tools/list | 工具列表 |
| POST | /api/mcp/tools/call | 工具调用 |
| GET | /api/mcp/stdio | stdio清单 |

### 4.2 MCP协议格式

```json
// 请求格式
{
    "jsonrpc": "2.0",
    "id": "unique-id",
    "method": "tools/call",
    "params": {
        "name": "execute_script",
        "arguments": {
            "script": "ls -la",
            "server_id": 1
        }
    }
}

// 响应格式
{
    "jsonrpc": "2.0",
    "id": "unique-id",
    "result": {
        "content": [
            {
                "type": "text",
                "text": "..."
            }
        ]
    }
}
```

---

## 五、已集成工具

| Tool名称 | MCP名称 | 描述 |
|----------|---------|------|
| execute_script | script_execute | 执行Shell脚本 |
| elk_query | elk_query | 查询日志 |
| milvus_search | milvus_search | 知识库搜索 |
| agent_execute | agent_execute | Agent命令 |
| notify | notify | 发送通知 |

---

## 六、使用示例

### 6.1 列出可用工具

```bash
curl -X POST http://localhost:8080/api/mcp/tools/list
```

响应：
```json
{
    "tools": [
        {
            "name": "execute_script",
            "description": "在目标服务器执行Shell脚本",
            "inputSchema": {...}
        },
        ...
    ]
}
```

### 6.2 调用工具

```bash
curl -X POST http://localhost:8080/api/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "name": "execute_script",
    "arguments": {
        "script": "ls -la",
        "server_id": 1
    }
  }'
```

### 6.3 SSE连接

```javascript
const es = new EventSource('http://localhost:8080/api/mcp/sse');
es.addEventListener('initialized', (e) => {
    console.log('MCP Protocol initialized:', JSON.parse(e.data));
});
es.addEventListener('tools', (e) => {
    console.log('Available tools:', JSON.parse(e.data));
});
```

---

## 七、工具定义格式

### 7.1 OpenAI Function格式

```json
{
    "type": "function",
    "function": {
        "name": "execute_script",
        "description": "在目标服务器执行Shell脚本",
        "parameters": {
            "type": "object",
            "properties": {
                "script": {
                    "type": "string",
                    "description": "要执行的Shell命令"
                },
                "server_id": {
                    "type": "integer",
                    "description": "目标服务器ID"
                }
            },
            "required": ["script", "server_id"]
        }
    }
}
```

---

## 八、实施状态

| 功能 | 状态 |
|------|------|
| MCPServerRunner | ✅ 完成 |
| MCP协议控制器 | ✅ 完成 |
| SSE端点 | ✅ 完成 |
| stdio端点 | ✅ 完成 |
| 工具注册 | ✅ 完成 |
| 工具调用 | ✅ 完成 |
| LLM增强调用 | ✅ 完成 |

---

## 九、注意事项

1. **SSE连接**：长时间连接需要配置超时
2. **Tool注册**：Tool从ToolRegistry自动同步
3. **外部访问**：生产环境需配置CORS和认证
4. **工具权限**：注意控制工具的访问权限

---

## 十、外部集成

### 10.1 Cherry Studio集成

在Cherry Studio中添加MCP服务器：
- URL: `http://your-server:8080/api/mcp/stdio`
- 勾选需要的工具

### 10.2 自定义MCP客户端

```python
import json
import requests

class AIOpsMCPClient:
    def __init__(self, base_url):
        self.base_url = base_url
    
    def call_tool(self, name, arguments):
        response = requests.post(
            f"{self.base_url}/api/mcp/tools/call",
            json={"name": name, "arguments": arguments}
        )
        return response.json()

client = AIOpsMCPClient("http://localhost:8080")
result = client.call_tool("execute_script", {
    "script": "ls -la",
    "server_id": 1
})
```

---

> 模块版本：V2.0
> 最后更新：2026-03-10
