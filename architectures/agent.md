# Multi-Agent 系统

> 模块：aiops-agent
> 版本：V1.0

---

## 一、架构概述

### 1.1 设计目标

本模块负责**AI Agent的编排和协调**，通过Multi-Agent协作实现智能运维。

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                      Multi-Agent 架构                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    Multi-Agent 协作                         │   │
│   │                                                             │   │
│   │   ┌───────────┐    ┌───────────┐    ┌───────────┐        │   │
│   │   │  Planner  │───▶│ Analyzer  │───▶│ Executor  │        │   │
│   │   │   Agent   │◀───│   Agent   │◀───│   Agent   │        │   │
│   │   └───────────┘    └───────────┘    └───────────┘        │   │
│   │        │                  │                  │              │   │
│   │        └──────────────────┼──────────────────┘              │   │
│   │                           │                                   │   │
│   └───────────────────────────┼───────────────────────────────────┘   │
│                               │                                       │
│   ┌───────────────────────────▼───────────────────────────────────┐   │
│   │                    Tool Registry                               │   │
│   │   Script Tool | ELK Tool | Milvus Tool | Agent Tool           │   │
│   └───────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、Agent 定义

### 2.1 Agent 实体

```java
@Entity
@Table(name = "ai_agents")
public class AIAgent {
    
    @Id
    private String id;               // "planner", "analyzer", "executor"
    
    private String name;             // "计划Agent"
    private String description;      // "负责制定执行计划"
    
    @Enumerated(EnumType.STRING)
    private AgentType type;           // PLANNER / ANALYZER / EXECUTOR / REPORT
    
    @Column(length = 5000)
    private String systemPrompt;     // 系统提示词
    
    @ElementCollection
    private List<String> availableTools;  // 可用Tool列表
    
    private Integer maxIterations;   // 最大迭代次数
    private Integer timeout;         // 超时时间(秒)
    
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

### 2.2 Agent 消息

```java
@Entity
@Table(name = "agent_messages")
public class AgentMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String sessionId;         // 会话ID
    
    private String fromAgent;        // "planner"
    private String toAgent;          // "analyzer"
    
    @Enumerated(EnumType.STRING)
    private MessageType type;        // REQUEST / RESPONSE / TOOL_CALL / TOOL_RESULT
    
    @Column(length = 10000)
    private String content;          // 消息内容
    
    @ElementCollection
    private List<ToolCall> toolCalls; // Tool调用列表
    
    private Map<String, Object> metadata;
    
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

---

## 三、Agent 职责

### 3.1 Planner Agent

```
职责：
• 接收用户请求 / 告警触发
• 分析需求，制定执行计划
• 决定调用哪些Tool
• 协调其他Agent

系统提示词示例：
你是一个运维计划Agent，负责分析用户请求并制定执行计划。
你需要：
1. 理解用户需求
2. 分析需要执行的步骤
3. 选择合适的Tool
4. 生成执行计划

可用的Tool：
- elk_query: 查询日志
- milvus_search: 搜索知识库
- agent_metrics: 获取服务器指标
- execute_script: 执行脚本
```

### 3.2 Analyzer Agent

```
职责：
• 分析监控数据、日志
• 理解问题根因
• 生成解决方案脚本
• 调用ELK Tool读取日志
• 调用Milvus Tool检索知识

系统提示词示例：
你是一个运维分析Agent，负责分析问题并生成解决方案。
你需要：
1. 收集相关日志和指标
2. 分析问题根因
3. 生成修复脚本
4. 评估解决方案

分析流程：
1. 使用elk_query查询相关日志
2. 使用milvus_search检索类似问题的解决方案
3. 使用agent_metrics获取当前指标
4. 综合分析生成结论
```

### 3.3 Executor Agent

```
职责：
• 执行运维动作
• 调用Script Tool执行脚本
• 调用Agent Tool与目标服务器通信
• 处理执行结果
• 重试失败操作

系统提示词示例：
你是一个执行Agent，负责执行运维动作。
你需要：
1. 执行生成的脚本
2. 验证执行结果
3. 处理错误和异常
4. 记录执行日志
```

### 3.4 Report Agent

```
职责：
• 汇总执行结果
• 生成执行报告
• 通知用户

系统提示词示例：
你是一个报告Agent，负责生成执行报告。
你需要：
1. 收集执行结果
2. 汇总执行情况
3. 生成可读的报告
4. 提供后续建议
```

---

## 四、Agent 协作流程

### 4.1 完整流程

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Multi-Agent 协作流程                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  用户请求: "网站访问慢，请排查"                                       │
│       │                                                             │
│       ▼                                                             │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                    Planner Agent                              │  │
│  │  思考: 需要分析CPU、内存、网络、Nginx日志                       │  │
│  │  决定调用: agent_metrics, elk_query                            │  │
│  │  生成计划:                                                     │  │
│  │    1. 获取服务器指标                                           │  │
│  │    2. 查询Nginx日志                                            │  │
│  │    3. 分析问题                                                  │  │
│  │    4. 执行修复                                                  │  │
│  └────────────────────────────┬──────────────────────────────────┘  │
│                               │                                      │
│                               ▼                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                    Analyzer Agent                             │  │
│  │  调用Tool: agent_metrics(server_id=1)                         │  │
│  │  返回: {cpu: 95%, memory: 80%}                                 │  │
│  │                                                               │  │
│  │  调用Tool: elk_query(query="nginx error")                    │  │
│  │  返回: {logs: [...]}                                          │  │
│  │                                                               │  │
│  │  分析结论:                                                     │  │
│  │    根因: PHP-FPM进程阻塞导致Nginx响应慢                        │  │
│  │    建议: 重启php-fpm服务                                       │  │
│  │    脚本: systemctl restart php-fpm                            │  │
│  └────────────────────────────┬──────────────────────────────────┘  │
│                               │                                      │
│                               ▼                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                    Executor Agent                             │  │
│  │  调用Tool: execute_script(script="systemctl restart php-fpm")│  │
│  │  返回: {output: "Service restarted", exit_code: 0}            │  │
│  │                                                               │  │
│  │  验证: 调用Tool: agent_metrics                                │  │
│  │  返回: {cpu: 30%, memory: 45%}                                 │  │
│  │                                                               │  │
│  │  结论: 问题已解决                                              │  │
│  └────────────────────────────┬──────────────────────────────────┘  │
│                               │                                      │
│                               ▼                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                    Report Agent                               │  │
│  │  生成报告:                                                     │  │
│  │    问题: 网站访问慢                                           │  │
│  │    根因: PHP-FPM进程阻塞                                     │  │
│  │    操作: 重启php-fpm服务                                      │  │
│  │    结果: CPU从95%降至30%，问题已解决                          │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 五、服务实现

### 5.1 Agent协调器

```java
@Service
public class AgentCoordinator {
    
    @Autowired
    private LlmProvider llmProvider;
    
    @Autowired
    private ToolRegistry toolRegistry;
    
    public AgentResponse coordinate(AgentRequest request) {
        String sessionId = UUID.randomUUID().toString();
        
        // 1. Planner - 制定计划
        AgentMessage planMsg = plannerAgent.execute(request.getMessage(), sessionId);
        
        // 2. Analyzer - 分析问题
        List<ToolCall> toolCalls = planMsg.getToolCalls();
        List<ToolResult> toolResults = executeTools(toolCalls);
        
        AgentMessage analysisMsg = analyzerAgent.executeWithResults(
            planMsg.getContent(), toolResults, sessionId);
        
        // 3. Executor - 执行操作
        if (analysisMsg.getToolCalls() != null) {
            toolResults = executeTools(analysisMsg.getToolCalls());
            executorAgent.executeWithResults(
                analysisMsg.getContent(), toolResults, sessionId);
        }
        
        // 4. Report - 生成报告
        AgentMessage reportMsg = reportAgent.execute(sessionId);
        
        return AgentResponse.builder()
            .sessionId(sessionId)
            .report(reportMsg.getContent())
            .build();
    }
    
    private List<ToolResult> executeTools(List<ToolCall> calls) {
        return calls.stream()
            .map(call -> toolRegistry.execute(call.getName(), call.getParams()))
            .collect(Collectors.toList());
    }
}
```

---

## 六、API 设计

### 6.1 Agent管理 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/agents | Agent列表 |
| GET | /api/v1/agents/{id} | Agent详情 |
| PUT | /api/v1/agents/{id} | 更新Agent |
| GET | /api/v1/agents/{id}/tools | Agent可用Tools |

### 6.2 Agent对话 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/v1/agents/chat | 发起对话 |
| GET | /api/v1/agents/sessions/{id} | 会话历史 |

---

## 七、实施计划

| 序号 | 工作内容 | 优先级 |
|------|----------|--------|
| 1 | AIAgent 实体 + 管理 | P1 |
| 2 | AgentMessage 实体 | P1 |
| 3 | Planner Agent 实现 | P1 |
| 4 | Analyzer Agent 实现 | P1 |
| 5 | Executor Agent 实现 | P1 |
| 6 | Report Agent 实现 | P1 |
| 7 | 前端Agent对话页面 | P2 |

---

> 模块版本：V1.0
> 最后更新：2025-03-09
