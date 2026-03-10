# Tool 系统

> 模块：aiops-tool
> 版本：V1.0

---

## 一、架构概述

### 1.1 设计目标

本模块实现**PTC (Programmatic Tool Calling)** 模式，让AI清楚要生成什么脚本，然后执行获取输出。

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                      Tool 系统架构                                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    Tool Registry                            │   │
│   │                                                             │   │
│   │   工具定义        工具执行          工具下载                  │   │
│   │   (Tool)    ──▶ (Executor)  ──▶ (Download)                │   │
│   │                                                             │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    Tool 类型                                │   │
│   │                                                             │   │
│   │   ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐      │   │
│   │   │ Script  │  │   ELK   │  │ Milvus  │  │  Agent  │      │   │
│   │   │  Tool   │  │  Tool   │  │  Tool   │  │  Tool   │      │   │
│   │   └─────────┘  └─────────┘  └─────────┘  └─────────┘      │   │
│   │                                                             │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、Tool 定义

### 2.1 Tool 实体

```java
@Entity
@Table(name = "tools")
public class Tool {
    
    @Id
    private String id;               // "script_executor", "elk_query"
    
    private String name;            // "执行脚本"
    private String description;    // "在目标服务器执行Shell脚本"
    
    @Enumerated(EnumType.STRING)
    private ToolType type;          // SCRIPT / ELK / MILVUS / AGENT / NOTIFY
    
    @Column(length = 5000)
    private String definition;      // OpenAI格式的Tool定义
    
    @Column(length = 3000)
    private String schema;          // JSON Schema参数定义
    
    @ElementCollection
    private List<String> examples;  // 使用示例
    
    private Boolean enabled;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum ToolType {
    SCRIPT,       // 脚本执行
    ELK,          // ELK日志查询
    MILVUS,       // 向量搜索
    AGENT,        // Agent通信
    NOTIFY,       // 通知发送
    CUSTOM        // 自定义
}
```

### 2.2 PTC 模式详解

```
┌─────────────────────────────────────────────────────────────────────┐
│                      PTC (Programmatic Tool Calling)                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  AI思考: "我需要检查Nginx错误日志"                                    │
│       │                                                             │
│       ▼                                                             │
│  AI ──▶ 调用 ELK_Tool                                              │
│       │  {                                                         │
│       │    "query": "error AND nginx",                            │
│       │    "time_range": "1h",                                    │
│       │    "limit": 100                                           │
│       │  }                                                         │
│       │                                                             │
│       ▼                                                             │
│  Tool执行 ──▶ ES查询日志 ──▶ 返回结果                               │
│       │                                                             │
│       │  {                                                         │
│       │    "logs": [                                               │
│       │      {"timestamp": "...", "message": "connect() failed"}  │
│       │    ]                                                        │
│       │  }                                                         │
│       │                                                             │
│       ▼                                                             │
│  AI分析日志 ──▶ "发现是连接数过多"                                   │
│       │                                                             │
│       ▼                                                             │
│  AI ──▶ 调用 Script_Tool                                           │
│       │  {                                                         │
│       │    "script": "ulimit -n",                                 │
│       │    "server_id": 1                                         │
│       │  }                                                         │
│       │                                                             │
│       ▼                                                             │
│  Tool执行 ──▶ Agent执行 ──▶ 返回                                   │
│       │                                                             │
│       │  {                                                         │
│       │    "output": "1024",                                      │
│       │    "exit_code": 0                                         │
│       │  }                                                         │
│       │                                                             │
│       ▼                                                             │
│  AI ──▶ "当前连接数1024，需要调大"                                   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 三、Tool 实现

### 3.1 Script Tool

```java
@Component
public class ScriptToolExecutor implements ToolExecutor {
    
    @Override
    public ToolResult execute(Map<String, Object> params) {
        String script = (String) params.get("script");
        Long serverId = (Long) params.get("server_id");
        Integer timeout = (Integer) params.getOrDefault("timeout", 30);
        
        // 验证脚本安全性
        validateScript(script);
        
        // 调用Agent执行
        Response resp = agentClient.execute(serverId, script, timeout);
        
        return ToolResult.builder()
            .success(resp.getExitCode() == 0)
            .output(resp.getOutput())
            .error(resp.getError())
            .build();
    }
    
    @Override
    public String getDefinition() {
        return """
        {
            "type": "function",
            "function": {
                "name": "execute_script",
                "description": "在目标服务器执行Shell脚本，返回执行结果。用于检查系统状态、执行运维操作。",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "script": {
                            "type": "string",
                            "description": "要执行的Shell命令，如 'df -h', 'ps aux', 'systemctl status nginx'"
                        },
                        "server_id": {
                            "type": "integer",
                            "description": "目标服务器ID"
                        },
                        "timeout": {
                            "type": "integer",
                            "description": "超时时间(秒)，默认30"
                        }
                    },
                    "required": ["script", "server_id"]
                }
            }
        }
        """;
    }
    
    @Override
    public ToolType getType() {
        return ToolType.SCRIPT;
    }
}
```

### 3.2 ELK Tool

```java
@Component
public class ELKToolExecutor implements ToolExecutor {
    
    @Override
    public ToolResult execute(Map<String, Object> params) {
        String query = (String) params.get("query");
        String timeRange = (String) params.getOrDefault("time_range", "1h");
        Integer limit = (Integer) params.getOrDefault("limit", 100);
        
        // 查询ES
        SearchResponse response = esClient.search(s -> s
            .index("logs-*")
            .query(q -> q.bool(b -> b
                .must(m -> m.match(mt -> mt.field("message").query(query)))
                .filter(f -> f.range(r -> r.field("@timestamp")
                    .gte(JsonData.of("now-" + timeRange))))
            ))
            .size(limit)
        );
        
        List<Map<String, Object>> logs = response.getHits().getHits().stream()
            .map(hit -> hit.getSourceAsMap())
            .collect(Collectors.toList());
        
        return ToolResult.builder()
            .success(true)
            .data(Map.of("logs", logs, "count", logs.size()))
            .build();
    }
    
    @Override
    public String getDefinition() {
        return """
        {
            "type": "function",
            "function": {
                "name": "elk_query",
                "description": "查询Elasticsearch日志，用于分析问题根因。可以查询错误日志、应用日志等。",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "query": {
                            "type": "string",
                            "description": "日志查询关键字，如 'error', 'nginx', 'timeout', 'connection refused'"
                        },
                        "time_range": {
                            "type": "string",
                            "description": "时间范围，如 '1h', '24h', '7d'，默认'1h'"
                        },
                        "limit": {
                            "type": "integer",
                            "description": "返回条数，默认100"
                        }
                    },
                    "required": ["query"]
                }
            }
        }
        """;
    }
    
    @Override
    public ToolType getType() {
        return ToolType.ELK;
    }
}
```

### 3.3 Milvus Tool

```java
@Component
public class MilvusToolExecutor implements ToolExecutor {
    
    @Override
    public ToolResult execute(Map<String, Object> params) {
        String query = (String) params.get("query");
        Long repoId = (Long) params.get("repo_id");
        Integer topK = (Integer) params.getOrDefault("top_k", 5);
        
        // 生成向量
        List<Float> vector = embeddingService.embed(query);
        
        // 查询向量数据库
        SearchResponse response = milvusClient.search(SearchRequest.builder()
            .collectionName("knowledge_" + repoId)
            .vector(vector)
            .topK(topK)
            .build());
        
        List<Map<String, Object>> results = response.getResults().stream()
            .map(r -> Map.of("content", r.get("content"), "score", r.get("score")))
            .collect(Collectors.toList());
        
        return ToolResult.builder()
            .success(true)
            .data(Map.of("results", results))
            .build();
    }
    
    @Override
    public String getDefinition() {
        return """
        {
            "type": "function",
            "function": {
                "name": "milvus_search",
                "description": "搜索知识库，获取运维知识、解决方案、故障处理方法。",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "query": {
                            "type": "string",
                            "description": "搜索内容，如 'Nginx 502错误', 'MySQL连接失败', '磁盘满处理'"
                        },
                        "repo_id": {
                            "type": "integer",
                            "description": "知识库ID"
                        },
                        "top_k": {
                            "type": "integer",
                            "description": "返回条数，默认5"
                        }
                    },
                    "required": ["query", "repo_id"]
                }
            }
        }
        """;
    }
    
    @Override
    public ToolType getType() {
        return ToolType.MILVUS;
    }
}
```

### 3.4 Agent Tool

```java
@Component
public class AgentToolExecutor implements ToolExecutor {
    
    @Override
    public ToolResult execute(Map<String, Object> params) {
        Long serverId = (Long) params.get("server_id");
        String action = (String) params.get("action");
        Map<String, Object> payload = (Map<String, Object>) params.get("payload");
        
        AgentConnection conn = connectionService.findByServerId(serverId);
        
        switch (action) {
            case "execute":
                return executeCommand(conn, payload);
            case "upload":
                return uploadFile(conn, payload);
            case "download":
                return downloadFile(conn, payload);
            case "metrics":
                return getMetrics(conn);
            default:
                return ToolResult.builder().success(false).error("unknown action").build();
        }
    }
    
    @Override
    public String getDefinition() {
        return """
        {
            "type": "function",
            "function": {
                "name": "agent_action",
                "description": "与目标Agent通信，执行命令、上传下载文件、获取指标。",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "server_id": {
                            "type": "integer",
                            "description": "目标服务器ID"
                        },
                        "action": {
                            "type": "string",
                            "enum": ["execute", "upload", "download", "metrics"],
                            "description": "操作类型"
                        },
                        "payload": {
                            "type": "object",
                            "description": "操作参数"
                        }
                    },
                    "required": ["server_id", "action"]
                }
            }
        }
        """;
    }
    
    @Override
    public ToolType getType() {
        return ToolType.AGENT;
    }
}
```

---

## 四、Tool 下载机制

### 4.1 问题背景

```
当Executor Agent需要某个Tool，但目标服务器上的Agent没有这个Tool时，
需要触发下载机制
```

### 4.2 Tool 包格式

```yaml
# tool_package.yaml
name: docker_manager
version: 1.0.0
description: Docker容器管理工具

scripts:
  - name: list_containers
    script: |
      docker ps -a --format "{{.Names}}\t{{.Status}}"
    description: 列出所有容器
    
  - name: restart_container
    script: |
      docker restart $CONTAINER_NAME
    description: 重启容器

requirements:
  - docker
```

### 4.3 下载流程

```
Executor Agent                        目标服务器Agent
      │                                     │
      │ 1. 需要执行Docker操作                  │
      │                                     │
      │ ───查询可用Tool───────────────────▶│
      │                                     │
      │◀──未找到该Tool─────────────────────│
      │                                     │
      │ 2. 触发Tool下载                      │
      │                                     │
      │ ───下载请求───────────────────────▶│
      │                                     │
      │◀──返回Tool包───────────────────────│
      │                                     │
      │ 3. 安装Tool                         │
      │                                     │
      │ 4. 再次执行                         │
```

---

## 五、API 设计

### 5.1 Tool管理 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/tools | Tool列表 |
| GET | /api/v1/tools/{id} | Tool详情 |
| PUT | /api/v1/tools/{id} | 更新Tool |
| GET | /api/v1/tools/{id}/definition | 获取Tool定义 |

### 5.2 Tool执行 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/v1/tools/execute | 执行Tool |

---

## 六、实施计划

| 序号 | 工作内容 | 优先级 |
|------|----------|--------|
| 1 | Tool 实体 + Registry | P0 |
| 2 | Script Tool | P0 |
| 3 | ELK Tool | P0 |
| 4 | Milvus Tool | P0 |
| 5 | Agent Tool | P0 |
| 6 | Tool下载机制 | P1 |
| 7 | 前端Tool管理 | P2 |

---

> 模块版本：V1.0
> 最后更新：2025-03-09
