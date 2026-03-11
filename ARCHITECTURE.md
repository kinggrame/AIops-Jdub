# AIOps 智能自动化运维平台 - 架构设计文档 V3.0

> 版本：V3.0
> 更新日期：2025-03-09

---

## 一、架构总览

### 1.1 核心理念

```
┌─────────────────────────────────────────────────────────────────────┐
│                     Multi-Agent + PTC + Tool Calling               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌───────────────────────────────────────────────────────────────┐ │
│   │                      User Interface                           │ │
│   │              (用户发起运维请求 / 查看结果)                      │ │
│   └────────────────────────────┬──────────────────────────────────┘ │
│                                │                                    │
│   ┌────────────────────────────▼──────────────────────────────────┐ │
│   │                    Multi-Agent System                         │ │
│   │                                                               │ │
│   │   ┌───────────┐    ┌───────────┐    ┌───────────┐           │ │
│   │   │  Planner  │───▶│ Analyzer  │───▶│ Executor  │           │ │
│   │   │   Agent   │◀───│   Agent   │◀───│   Agent   │           │ │
│   │   └───────────┘    └───────────┘    └───────────┘           │ │
│   │        │                  │                  │              │ │
│   │        └──────────────────┼──────────────────┘              │ │
│   │                           │                                   │ │
│   └───────────────────────────┼───────────────────────────────────┘ │
│                               │                                       │
│   ┌───────────────────────────▼───────────────────────────────────┐ │
│   │                    Tool Registry (PTC)                       │ │
│   │   ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐        │ │
│   │   │ Script  │  │   ELK   │  │ Milvus  │  │  Agent  │        │ │
│   │   │  Tool   │  │  Tool   │  │  Tool   │  │  Tool   │        │ │
│   │   └─────────┘  └─────────┘  └─────────┘  └─────────┘        │ │
│   │        │            │            │            │              │ │
│   │        └────────────┴────────────┴────────────┘              │ │
│   │                         │                                      │ │
│   └─────────────────────────┼──────────────────────────────────────┘ │
│                             │                                          │
│   ┌─────────────────────────▼──────────────────────────────────────┐ │
│   │                    执行层 (Automation Core)                    │ │
│   │   监控采集 ─▶ 告警规则 ─▶ 执行引擎 ─▶ 运维动作                  │ │
│   └───────────────────────────────────────────────────────────────┘ │
│                                                                     │
│   ┌───────────────────────────────────────────────────────────────┐ │
│   │                    连接/配对层                                 │ │
│   │        Agent注册、配对、连接、WebSocket                        │ │
│   └───────────────────────────────────────────────────────────────┘ │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、Multi-Agent 系统架构

### 2.1 Agent 职责

```
┌─────────────────────────────────────────────────────────────────────┐
│                       Multi-Agent 协作                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                      Planner Agent                            │  │
│  │  • 接收用户请求 / 告警触发                                     │  │
│  │  • 分析需求，制定执行计划                                      │  │
│  │  • 调用哪些Tool                                                │  │
│  │  • 协调其他Agent                                               │  │
│  └────────────────────────────┬──────────────────────────────────┘  │
│                               │                                     │
│                               ▼                                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                      Analyzer Agent                           │  │
│  │  • 分析监控数据、日志                                          │  │
│  │  • 理解问题根因                                                │  │
│  │  • 生成解决方案脚本                                            │  │
│  │  • 调用ELK Tool读取日志                                        │  │
│  │  • 调用Milvus Tool检索知识                                     │  │
│  └────────────────────────────┬──────────────────────────────────┘  │
│                               │                                     │
│                               ▼                                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                      Executor Agent                           │  │
│  │  • 执行运维动作                                                │  │
│  │  • 调用Script Tool执行脚本                                    │  │
│  │  • 调用Agent Tool与目标服务器通信                              │  │
│  │  • 处理执行结果                                                │  │
│  └────────────────────────────┬──────────────────────────────────┘  │
│                               │                                     │
│                               ▼                                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                     Report Agent                              │  │
│  │  • 汇总执行结果                                               │  │
│  │  • 生成报告                                                    │  │
│  │  • 通知用户                                                   │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 Agent 通信协议

```java
// Agent间通信消息
public class AgentMessage {
    private String messageId;
    private String fromAgent;      // "planner"
    private String toAgent;        // "analyzer"
    private MessageType type;      // REQUEST / RESPONSE / TOOL_CALL
    
    private String content;        // 消息内容
    private Map<String, Object> metadata;
    
    private List<ToolCall> toolCalls;  // Tool调用列表
    
    private LocalDateTime timestamp;
}

public enum MessageType {
    REQUEST,       // 请求
    RESPONSE,      // 响应
    TOOL_CALL,     // Tool调用
    TOOL_RESULT,   // Tool结果
    ERROR          // 错误
}
```

### 2.3 Agent 定义

```java
@Entity
public class AIAgent {
    @Id
    private String id;             // "planner", "analyzer", "executor"
    
    private String name;           // "计划Agent"
    private String description;    // 描述
    
    @Enumerated(EnumType.STRING)
    private AgentType type;        // PLANNER / ANALYZER / EXECUTOR / REPORT
    
    private String systemPrompt;  // 系统提示词
    
    private List<String> availableTools;  // 可用Tool列表
    
    private Boolean enabled;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum AgentType {
    PLANNER,      // 计划Agent - 制定执行计划
    ANALYZER,     // 分析Agent - 分析问题、生成脚本
    EXECUTOR,     // 执行Agent - 执行运维动作
    REPORT        // 报告Agent - 生成执行报告
}
```

---

## 三、Tool Registry (PTC模式)

### 3.1 Tool 架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                      Tool Registry 系统                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐  │
│   │                    Tool Definition                           │  │
│   │  • name: 工具名称                                            │  │
│   │  • description: 描述 (让AI知道何时调用)                        │  │
│   │  • parameters: 参数定义                                      │  │
│   │  • returns: 返回值                                            │  │
│   │  • examples: 使用示例                                         │  │
│   └─────────────────────────────────────────────────────────────┘  │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐  │
│   │                    Tool Executor                             │  │
│   │                                                               │  │
│   │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │  │
│   │  │ Script Tool │  │  ELK Tool   │  │ Milvus Tool │          │  │
│   │  │             │  │             │  │             │          │  │
│   │  │ • execute() │  │ • query()   │  │ • search()  │          │  │
│   │  │ • validate()│  │ • getLogs() │  │ • insert()  │          │  │
│   │  └─────────────┘  └─────────────┘  └─────────────┘          │  │
│   │                                                               │  │
│   │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │  │
│   │  │ Agent Tool  │  │ Notify Tool │  │ Custom Tool │          │  │
│   │  │             │  │             │  │             │          │  │
│   │  │ • execute() │  │ • send()    │  │ • call()    │          │  │
│   │  │ • upload()  │  │             │  │             │          │  │
│   │  └─────────────┘  └─────────────┘  └─────────────┘          │  │
│   │                                                               │  │
│   └─────────────────────────────────────────────────────────────┘  │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐  │
│   │                    Tool Calling Flow                         │  │
│   │                                                               │  │
│   │  AI ──决定调用Tool──▶ ToolRegistry ──执行──▶ 结果返回──▶ AI   │  │
│   │                                                               │  │
│   │  PTC模式: AI清楚要生成什么脚本，执行获取输出                     │  │
│   │                                                               │  │
│   └─────────────────────────────────────────────────────────────┘  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.2 Tool 定义

```java
@Entity
public class Tool {
    @Id
    private String id;              // "script_executor", "elk_query"
    
    private String name;           // "执行脚本"
    private String description;   // "在目标服务器执行Shell脚本"
    
    @Enumerated(EnumType.STRING)
    private ToolType type;        // SCRIPT / ELK / MILVUS / AGENT / NOTIFY
    
    private String definition;    // OpenAI格式的Tool定义
    
    private String schema;         // JSON Schema参数定义
    
    private List<String> examples; // 使用示例
    
    private Boolean enabled;
    
    private LocalDateTime createdAt;
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

### 3.3 PTC 模式详解

**核心思想：AI生成脚本 → 执行脚本 → 获取输出 → 继续分析**

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
│       │    "time_range": "1h",                                     │
│       │    "limit": 100                                            │
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
│       │    "server_id": 1                                          │
│       │  }                                                         │
│       │                                                             │
│       ▼                                                             │
│  Tool执行 ──▶ Agent执行 ──▶ 返回                                   │
│       │                                                             │
│       │  {                                                         │
│       │    "output": "1024",                                       │
│       │    "exit_code": 0                                          │
│       │  }                                                         │
│       │                                                             │
│       ▼                                                             │
│  AI ──▶ "当前连接数1024，需要调大"                                   │
│       │                                                             │
│       ▼                                                             │
│  AI ──▶ 生成修复脚本 ──▶ 执行 ──▶ 验证                              │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.4 Tool 详细设计

#### 3.4.1 Script Tool

```java
// 脚本执行Tool
public class ScriptTool implements Tool {
    
    @Override
    public ToolResult execute(Map<String, Object> params) {
        String script = (String) params.get("script");
        Long serverId = (Long) params.get("server_id");
        Integer timeout = (Integer) params.get("timeout", 30);
        
        // 1. 验证脚本安全性
        validateScript(script);
        
        // 2. 发送到Agent执行
        Response resp = agentClient.execute(serverId, script, timeout);
        
        // 3. 返回结果
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
                "description": "在目标服务器执行Shell脚本，返回执行结果",
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
}
```

#### 3.4.2 ELK Tool

```java
// ELK日志查询Tool
public class ELKTool implements Tool {
    
    @Override
    public ToolResult execute(Map<String, Object> params) {
        String query = (String) params.get("query");
        String timeRange = (String) params.get("time_range", "1h");
        Integer limit = (Integer) params.get("limit", 100);
        
        // 查询ES
        SearchResponse response = esClient.search(s -> s
            .index("logs-*")
            .query(q -> q
                .bool(b -> b
                    .must(m -> m.match(mt -> mt.field("message").query(query)))
                    .filter(f -> f
                        .range(r -> r.field("@timestamp")
                            .gte(JsonData.of("now-" + timeRange)))
                    )
                )
            )
            .size(limit)
        );
        
        // 转换为日志列表
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
                "description": "查询Elasticsearch日志，用于分析问题根因",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "query": {
                            "type": "string",
                            "description": "日志查询关键字，如 'error', 'nginx', 'timeout'"
                        },
                        "time_range": {
                            "type": "string",
                            "description": "时间范围，如 '1h', '24h', '7d'"
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
}
```

#### 3.4.3 Milvus Tool

```java
// 向量知识库搜索Tool
public class MilvusTool implements Tool {
    
    @Override
    public ToolResult execute(Map<String, Object> params) {
        String query = (String) params.get("query");
        Long repoId = (Long) params.get("repo_id");  // 知识库ID
        Integer topK = (Integer) params.get("top_k", 5);
        
        // 1. 生成向量
        List<Float> vector = embeddingService.embed(query);
        
        // 2. 查询向量数据库
        SearchResponse response = milvusClient.search(SearchRequest.builder()
            .collectionName("knowledge_" + repoId)
            .vector(vector)
            .topK(topK)
            .build());
        
        // 3. 返回结果
        List<Map<String, Object>> results = response.getResults().stream()
            .map(r -> Map.of(
                "content", r.get("content"),
                "score", r.get("score"),
                "title", r.get("title")
            ))
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
                "description": "搜索知识库，获取运维知识、解决方案",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "query": {
                            "type": "string",
                            "description": "搜索内容，如 'Nginx 502错误', 'MySQL连接失败'"
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
                    "required": ["query"]
                }
            }
        }
        """;
    }
}
```

#### 3.4.4 Agent Tool

```java
// 与目标Agent通信Tool
public class AgentTool implements Tool {
    
    @Override
    public ToolResult execute(Map<String, Object> params) {
        Long serverId = (Long) params.get("server_id");
        String action = (String) params.get("action");
        Map<String, Object> payload = (Map<String, Object>) params.get("payload");
        
        // 获取Agent连接
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
                "description": "与目标Agent通信，执行命令、上传下载文件、获取指标",
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
}
```

---

## 四、Agent Tool 下载机制

### 4.1 问题背景

```
当Executor Agent需要某个Tool，但目标服务器上的Agent没有这个Tool时，
需要触发下载机制
```

### 4.2 流程

```
Executor Agent                        目标服务器Agent
      │                                     │
      │ 1. 需要执行操作                       │
      │    (如: 获取Docker容器列表)           │
      │                                     │
      │ ───查询可用Tool───────────────────▶│
      │                                     │
      │◀──未找到该Tool─────────────────────│
      │                                     │
      │ 2. 触发Tool下载                      │
      │    询问用户/自动下载                  │
      │                                     │
      │ ───下载请求───────────────────────▶│
      │                                     │
      │◀──返回Tool包(script+config)────────│
      │                                     │
      │ 3. 安装Tool                         │
      │    写入~/.aiops/tools/              │
      │                                     │
      │ 4. 再次执行                         │
      │                                     │
```

### 4.3 Tool 包格式

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
    params:
      - name: CONTAINER_NAME
        required: true

requirements:
  - docker
```

### 4.4 实现

```java
// Tool下载服务
public class ToolDownloadService {
    
    public ToolPackage downloadTool(String toolName, Long serverId) {
        // 1. 获取服务器Agent信息
        AgentConnection conn = connectionService.findByServerId(serverId);
        
        // 2. 请求下载Tool
        Response resp = httpClient.post(
            conn.getEndpoint() + "/api/tool/download",
            new DownloadRequest(toolName)
        );
        
        // 3. 返回Tool包
        return parseToolPackage(resp.getBody());
    }
    
    public void installTool(Long serverId, ToolPackage pkg) {
        // 1. 写入本地Tool目录
        Path toolPath = Paths.get(".aiops/tools/", pkg.getName());
        Files.createDirectories(toolPath);
        
        // 2. 写入配置文件
        writeYaml(toolPath.resolve("tool.yaml"), pkg);
        
        // 3. 写入脚本
        for (ScriptFile script : pkg.getScripts()) {
            writeFile(toolPath.resolve("scripts/" + script.getName()), 
                     script.getContent());
        }
    }
}
```

---

## 五、ELK 日志系统

### 5.1 架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                        ELK 日志系统                                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   Agent                           ES                       Kibana  │
│   ──────                         ──                       ──────  │
│     │                             │                          │       │
│     │ 1. 日志产生                  │                          │       │
│     │   (应用日志/系统日志)         │                          │       │
│     │                             │                          │       │
│     │ 2. 发送到后端                │                          │       │
│     │   ────────────────────────▶│                          │       │
│     │                             │                          │       │
│     │                        3. 写入ES                      │       │
│     │                             │                          │       │
│     │                        4. 用户搜索 ◀────────────────│       │
│     │                             │                          │       │
│     │                        5. AI查询 ◀─────────────────│       │
│     │                             │                          │       │
│     │                        6. 返回结果 ────────────────▶│       │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐  │
│   │                     双重用途                                │  │
│   │  ┌─────────────────┐    ┌─────────────────────────────┐    │  │
│   │  │ 用户搜索          │    │ AI Tool 调用                │    │  │
│   │  │ • 查问题          │    │ • ELK_Tool(query,time)   │    │  │
│   │  │ • 排查错误        │    │ • 自动分析日志             │    │  │
│   │  │ • 日志分析        │    │ • 根因定位                 │    │  │
│   │  └─────────────────┘    └─────────────────────────────┘    │  │
│   └─────────────────────────────────────────────────────────────┘  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 5.2 日志采集

```java
// Agent端日志采集
public class LogCollector {
    
    // 采集文件日志
    public void collectFileLogs(String path, String pattern) {
        try (WatchService watch = FileSystems.getDefault().newWatchService()) {
            Path logPath = Paths.get(path);
            logPath.register(watch, StandardWatchEventKinds.ENTRY_MODIFY);
            
            while (true) {
                WatchKey key = watch.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        String newLines = readNewLines(path);
                        sendToBackend(newLines);
                    }
                }
                key.reset();
            }
        }
    }
    
    // 发送到后端
    private void sendToBackend(String logs) {
        httpClient.post(serverUrl + "/api/v1/logs/ingest", 
            new LogIngestRequest(logs));
    }
}

// 后端写入ES
public class LogIngestService {
    
    public void ingest(LogIngestRequest request) {
        for (String line : request.getLogs().split("\n")) {
            // 解析日志
            LogEntry entry = parser.parse(line);
            
            // 添加元数据
            entry.setAgentId(request.getAgentId());
            entry.setTimestamp(LocalDateTime.now());
            
            // 写入ES
            esClient.index("logs-" + DateUtil.format(new Date(), "yyyy.MM.dd"), 
                entry);
        }
    }
}
```

### 5.3 日志查询

```java
// ELK Tool 实现
public class ELKTool {
    
    public ToolResult query(String query, String timeRange, Integer limit) {
        // 构建ES查询
        BoolQueryBuilder esQuery = QueryBuilders.boolQuery();
        
        // 关键词匹配
        esQuery.must(QueryBuilders.matchQuery("message", query));
        
        // 时间范围
        esQuery.filter(QueryBuilders.rangeQuery("@timestamp")
            .gte("now-" + timeRange));
        
        // 执行搜索
        SearchResponse response = esClient.search(s -> s
            .index("logs-*")
            .query(q -> q.bool(esQuery))
            .size(limit)
            .sort(ss -> ss.field(f -> f.field("@timestamp").order(SortOrder.DESC)))
        );
        
        // 转换结果
        List<Map<String, Object>> results = new ArrayList<>();
        for (Hit<Map> hit : response.getHits().getHits()) {
            results.add(hit.getSourceAsMap());
        }
        
        return ToolResult.builder()
            .success(true)
            .data(Map.of("logs", results, "total", response.getHits().getTotalHits()))
            .build();
    }
}
```

---

## 六、Milvus 向量数据库

### 6.1 架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                      Milvus 向量数据库                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    知识库多仓库                              │   │
│   │                                                               │   │
│   │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │   │
│   │  │ Nginx仓库     │  │ MySQL仓库    │  │ 自定义仓库   │       │   │
│   │  │              │  │              │  │              │       │   │
│   │  │ documents   │  │ documents   │  │ documents   │       │   │
│   │  │ vectors     │  │ vectors     │  │ vectors     │       │   │
│   │  │              │  │              │  │              │       │   │
│   │  │ embedding:   │  │ embedding:   │  │ embedding:   │       │   │
│   │  │ text-ada-002│  │ text-ada-002│  │ custom      │       │   │
│   │  └──────────────┘  └──────────────┘  └──────────────┘       │   │
│   │                                                               │   │
│   └───────────────────────────────────────────────────────────────┘   │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    双重用途                                  │   │
│   │  ┌─────────────────┐    ┌─────────────────────────────┐    │  │
│   │  │ 用户搜索          │    │ AI Tool 调用                │    │  │
│   │  │ • 语义搜索       │    │ • Milvus_Tool(query,repo)  │    │  │
│   │  │ • 问答           │    │ • 检索运维知识              │    │  │
│   │  │ • 推荐           │    │ • 生成解决方案参考         │    │  │
│   │  └─────────────────┘    └─────────────────────────────┘    │  │
│   │                                                               │   │
│   └───────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 6.2 知识库多仓库

```java
@Entity
public class KnowledgeRepo {
    @Id
    private Long id;
    
    private String name;                  // "Nginx知识库"
    private String description;           // "Nginx配置、故障处理"
    
    private Long ownerId;                 // 所有者
    
    // 每个仓库独立配置嵌入模型
    private String embeddingModelId;      // "openai-ada-002"
    private Integer embeddingDimensions;   // 1536
    
    @Enumerated(EnumType.STRING)
    private RepoVisibility visibility;    // PUBLIC / PRIVATE
    
    private Long documentCount;
    private Long vectorCount;
    
    private LocalDateTime createdAt;
}
```

### 6.3 向量化和搜索

```java
public class EmbeddingService {
    
    // 文本向量化
    public List<Float> embed(String text) {
        // 调用嵌入模型
        EmbeddingResponse resp = llmProvider.embed(
            EmbeddingRequest.builder()
                .model(embeddingModel.getModel())
                .input(text)
                .build()
        );
        
        return resp.getData().get(0).getEmbedding();
    }
    
    // 文档向量化存储
    public void embedAndStore(Long repoId, KnowledgeDoc doc) {
        // 1. 生成向量
        List<Float> vector = embed(doc.getContent());
        
        // 2. 存储到Milvus
        milvusClient.insert(InsertRequest.builder()
            .collectionName("knowledge_" + repoId)
            .fields(Map.of(
                "id", doc.getId(),
                "content", doc.getContent(),
                "title", doc.getTitle(),
                "vector", vector
            ))
            .build());
        
        // 3. 更新统计
        repoService.incrementVectorCount(repoId);
    }
}

// Milvus Tool 实现
public class MilvusTool {
    
    public ToolResult search(String query, Long repoId, Integer topK) {
        // 1. 向量化查询
        List<Float> vector = embeddingService.embed(query);
        
        // 2. 搜索向量数据库
        SearchResponse resp = milvusClient.search(SearchRequest.builder()
            .collectionName("knowledge_" + repoId)
            .vector(vector)
            .topK(topK)
            .build());
        
        // 3. 转换结果
        List<Map<String, Object>> results = resp.getResults().stream()
            .map(r -> Map.of(
                "title", r.get("title"),
                "content", r.get("content"),
                "score", r.get("score")
            ))
            .collect(Collectors.toList());
        
        return ToolResult.builder()
            .success(true)
            .data(Map.of("results", results))
            .build();
    }
}
```

---

## 七、完整执行流程示例

### 场景：用户报告"网站访问慢"

```
┌─────────────────────────────────────────────────────────────────────┐
│                     执行流程示例 (Multi-Agent + PTC)                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│ 1. 用户发起请求                                                      │
│    "网站访问慢，请排查"                                             │
│       │                                                             │
│       ▼                                                             │
│ 2. Planner Agent                                                    │
│    "需要分析:                                                       │
│     - 服务器指标(CPU/内存/网络)                                      │
│     - Nginx日志                                                     │
│     - 最近变更                                                      │
│     调用: agent_metrics, elk_query"                                 │
│       │                                                             │
│       ▼                                                             │
│ 3. Analyzer Agent                                                   │
│    ├── Tool: agent_metrics(server_id=1)                            │
│    │   返回: {cpu: 95%, memory: 80%, network: 500Mbps}              │
│    │                                                               │
│    ├── Tool: elk_query(query="nginx", time_range="1h")            │
│    │   返回: {logs: [...]}                                          │
│    │                                                               │
│    └── 分析结果: "CPU 95% + Nginx错误日志大量connect() failed"       │
│        根因: "PHP-FPM进程阻塞导致Nginx响应慢"                         │
│       │                                                             │
│       ▼                                                             │
│ 4. Planner Agent                                                    │
│    "问题定位: PHP-FPM进程阻塞                                        │
│     建议: 重启PHP-FPM服务                                           │
│     调用: script_execute"                                           │
│       │                                                             │
│       ▼                                                             │
│ 5. Executor Agent                                                   │
│    ├── Tool: script_execute(                                       │
│    |   script="systemctl restart php-fpm",                        │
│    |   server_id=1)                                                │
│    │                                                               │
│    └── 返回: {output: "Service restarted", exit_code: 0}           │
│       │                                                             │
│       ▼                                                             │
│ 6. 验证                                                            │
│    ├── Tool: agent_metrics(server_id=1)                            │
│    │   返回: {cpu: 30%, memory: 45%}                                │
│    │                                                               │
│    └── 问题已解决 ✓                                                 │
│                                                                     │
│ 7. Report Agent                                                     │
│    "问题已解决:                                                     │
│     - 根因: PHP-FPM进程阻塞                                         │
│     - 操作: 重启php-fpm服务                                         │
│     - 结果: CPU从95%降至30%"                                        │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 八、后端模块清单

```
aiops-backend/
├── aiops-common                    # 通用
├── aiops-cache                     # 缓存
│
├── aiops-connection      ★ 重构    # Agent连接/配对
│
├── aiops-monitoring               # 监控指标
│   ├── TargetServer              # 运维对象
│   ├── MetricConfig              # 指标配置
│   └── MetricData                # 指标数据
│
├── aiops-alert                    # 告警规则
│   ├── AlertRule
│   ├── Alert
│   └── RuleEngine
│
├── aiops-action                   # 执行动作 ★ 核心
│   ├── Action
│   ├── ActionExecution
│   ├── ActionService
│   └── ExecutorService
│
├── aiops-agent         ★ 新增    # Multi-Agent系统
│   ├── entity/
│   │   ├── AIAgent               # Agent定义
│   │   └── AgentMessage          # 消息
│   ├── service/
│   │   ├── PlannerService        # 计划Agent
│   │   ├── AnalyzerService       # 分析Agent
│   │   ├── ExecutorService       # 执行Agent
│   │   └── ReportService         # 报告Agent
│   └── coordinator/              # Agent协调器
│
├── aiops-tool         ★ 新增    # Tool系统 ★ 核心
│   ├── entity/
│   │   └── Tool                  # Tool定义
│   ├── registry/
│   │   └── ToolRegistry          # Tool注册表
│   ├── executor/
│   │   ├── ScriptToolExecutor    # 脚本执行
│   │   ├── ELKToolExecutor      # ELK查询
│   │   ├── MilvusToolExecutor   # 向量搜索
│   │   ├── AgentToolExecutor    # Agent通信
│   │   └── NotifyToolExecutor   # 通知
│   └── download/
│       └── ToolDownloadService   # Tool下载
│
├── aiops-automation              # 自动化运维(编排)
│
├── aiops-script                 # 脚本管理
│
├── aiops-knowledge   ★ 重构    # 知识库多仓库
│   ├── entity/
│   │   ├── KnowledgeRepo        # 仓库
│   │   └── KnowledgeDoc        # 文档
│   ├── service/
│   │   ├── RepoService
│   │   ├── EmbeddingService    # 向量化
│   │   └── SearchService       # 搜索
│
├── aiops-llm         ★ 新增    # LLM配置
│   ├── ChatModel               # 对话模型
│   └── EmbeddingModel          # 嵌入模型
│
├── aiops-notification ★ 新增   # 通知
│
├── aiops-log          ★ 新增   # ELK集成
│   ├── LogIngestService       # 日志采集
│   ├── LogQueryService        # 日志查询
│   └── LogAnalyzerService     # 日志分析
│
└── aiops-web                    # 启动入口
```

---

## 九、实施计划

### 阶段一：连接 + 基础执行 (2周)

| 序号 | 模块 | 工作内容 | 优先级 |
|------|------|----------|--------|
| 1 | 连接管理 | Agent配对 + 认证 + WebSocket | P0 |
| 2 | 监控指标 | 指标采集 + 存储 | P0 |
| 3 | 告警规则 | 告警规则 + 规则引擎 | P0 |
| 4 | 执行动作 | Action + 执行器 | P0 |

### 阶段二：Tool系统 (2周) ★

| 序号 | 模块 | 工作内容 | 优先级 |
|------|------|----------|--------|
| 1 | Tool注册 | Tool实体 + Registry | P0 |
| 2 | Script Tool | 脚本执行Tool | P0 |
| 3 | Agent Tool | Agent通信Tool | P0 |
| 4 | Tool下载 | Tool下载机制 | P1 |

### 阶段三：Multi-Agent (1周) ★

| 序号 | 模块 | 工作内容 | 优先级 |
|------|------|----------|--------|
| 1 | Agent定义 | Agent实体 + 管理 | P1 |
| 2 | Planner | 计划Agent | P1 |
| 3 | Analyzer | 分析Agent | P1 |
| 4 | Executor | 执行Agent | P1 |

### 阶段四：ELK + Milvus (1周)

| 序号 | 模块 | 工作内容 | 优先级 |
|------|------|----------|--------|
| 1 | ELK集成 | 日志采集 + 查询Tool | P2 |
| 2 | Milvus集成 | 向量存储 + 搜索Tool | P2 |
| 3 | 知识库 | 多仓库管理 | P2 |

### 阶段五：LLM + 通知 (1周)

| 序号 | 模块 | 工作内容 | 优先级 |
|------|------|----------|--------|
| 1 | LLM配置 | 可视化配置 | P2 |
| 2 | 通知 | 钉钉/企微/邮件 | P2 |

---

## 十、前端页面

```
src/pages/
├── Dashboard/              # 态势总览
│
├── Servers/               # 运维对象
│
├── Alerts/                # 告警管理
│   ├── Rules/
│   └── History/
│
├── Actions/               # 运维动作
│   ├── History/
│   └── Execution/
│
├── AgentChat/             # ★ Multi-Agent对话
│
├── Tools/                # ★ Tool管理
│   ├── List/
│   └── Definition/
│
├── Scripts/              # 脚本管理
│
├── Knowledge/            # 知识库
│   ├── Repos/
│   └── Search/
│
├── Connections/           # Agent连接
│
└── Settings/             # 系统设置
```

---

> 文档版本：V3.0
