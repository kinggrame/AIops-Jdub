# Multi-Agent 系统

> 模块：aiops-agent
> 版本：V2.0

---

## 一、架构概述

### 1.1 设计目标

本模块负责**AI Agent的编排和协调**，通过Multi-Agent协作实现智能运维。基于LangGraph4j实现工作流编排。

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                      Multi-Agent 架构                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    LangGraph4j 工作流                        │   │
│   │                                                             │   │
│   │   ┌───────────┐    ┌───────────┐    ┌───────────┐        │   │
│   │   │  Planner  │───▶│ Analyzer  │───▶│ Executor  │        │   │
│   │   │   Agent   │◀───│   Agent   │◀───│   Agent   │        │   │
│   │   └───────────┘    └───────────┘    └───────────┘        │   │
│   │        │                  │                  │              │   │
│   │        └──────────────────┼──────────────────┘              │   │
│   │                           │                                   │   │
│   │                    ┌──────▼──────┐                         │   │
│   │                    │Report Agent │                         │   │
│   │                    └─────────────┘                         │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                               │                                       │
│   ┌───────────────────────────▼───────────────────────────────────┐   │
│   │                    Tool Execution Layer                         │   │
│   │   ┌─────────────────────────────────────────────────────────┐  │   │
│   │   │              ToolRegistryInitializer                   │  │   │
│   │   │  (自动注册5个Tool)                                      │  │   │
│   │   └─────────────────────────────────────────────────────────┘  │   │
│   │   Script | ELK | Milvus | Agent | Notify                      │   │
│   └───────────────────────────────────────────────────────────────┘   │
│                                                                     │
│   ┌───────────────────────────▼───────────────────────────────────┐   │
│   │                    LLM Service Layer                          │   │
│   │              (支持Ollama/OpenAI)                            │   │
│   └───────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、核心组件

### 2.1 LangGraph4jWorkflowService

```java
@Service
public class LangGraph4jWorkflowService {

    // Agent接口定义（使用@AiServices自动实现）
    public interface PlannerAgent {
        @UserMessage("分析用户请求并制定执行计划: {{request}}")
        String plan(String request);
    }

    public interface AnalyzerAgent {
        @UserMessage("分析计划并识别潜在问题: {{plan}}")
        String analyze(String plan);
    }

    public interface ExecutorAgent {
        @UserMessage("执行分析并返回结果: {{analysis}}")
        String execute(String analysis);
    }

    public interface ReportAgent {
        @UserMessage("生成最终报告: {{execution}}")
        String report(String execution);
    }

    // 执行工作流
    public String execute(String sessionId, String userRequest);
}
```

### 2.2 AgentState

```java
public static class AgentState {
    private String sessionId;
    private String userRequest;
    private String plan;
    private String analysis;
    private String execution;
    private String finalReport;
    private boolean completed = false;
    private int iteration = 0;
}
```

---

## 三、Agent职责

### 3.1 Planner Agent

```
职责：
• 接收用户请求 / 告警触发
• 分析需求，制定执行计划
• 决定调用哪些Tool
• 协调其他Agent

系统提示词：
你是一个运维计划Agent，负责分析用户请求并制定执行计划。
你需要：
1. 理解用户需求
2. 分析需要执行的步骤
3. 选择合适的Tool
4. 生成执行计划
```

### 3.2 Analyzer Agent

```
职责：
• 分析监控数据、日志
• 理解问题根因
• 生成解决方案脚本
• 调用Tool检索信息

系统提示词：
你是一个运维分析Agent，负责分析问题并生成解决方案。
你需要：
1. 收集相关日志和指标
2. 分析问题根因
3. 生成修复脚本
4. 评估解决方案
```

### 3.3 Executor Agent

```
职责：
• 执行运维动作
• 调用Script Tool执行脚本
• 调用Agent Tool与目标服务器通信
• 处理执行结果

系统提示词：
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

系统提示词：
你是一个报告Agent，负责生成执行报告。
你需要：
1. 收集执行结果
2. 汇总执行情况
3. 生成可读的报告
4. 提供后续建议
```

---

## 四、Tool系统

### 4.1 已注册Tools

系统启动时自动注册以下Tools：

| Tool名称 | 类型 | 描述 |
|----------|------|------|
| execute_script | SCRIPT | 在目标服务器执行Shell脚本 |
| elk_query | ELK | 查询Elasticsearch日志 |
| milvus_search | MILVUS | 搜索知识库 |
| agent_execute | AGENT | 在目标Agent上执行命令 |
| notify | NOTIFY | 发送通知到指定渠道 |

### 4.2 ToolRegistryInitializer

```java
@Component
public class ToolRegistryInitializer {

    @PostConstruct
    public void registerTools() {
        // 自动注册所有ToolExecutor实现
        toolRegistry.register(executor.getToolName(), executor);
    }
}
```

### 4.3 ToolExecutor接口

```java
public interface ToolExecutor {
    Object execute(Map<String, Object> params);
    String getDefinition();
    Tool.ToolType getType();
    String getToolName();
}
```

---

## 五、协作流程

### 5.1 完整流程

```
用户请求: "网站访问慢，请排查"
      │
      ▼
┌─────────────────────────────────────┐
│         Planner Agent               │
│  思考: 需要分析CPU、内存、网络、日志   │
│  生成执行计划                        │
└─────────────────────────────────────┘
      │
      ▼
┌─────────────────────────────────────┐
│         Analyzer Agent               │
│  调用Tool: elk_query               │
│  调用Tool: agent_metrics           │
│  分析结论: PHP-FPM进程阻塞          │
└─────────────────────────────────────┘
      │
      ▼
┌─────────────────────────────────────┐
│         Executor Agent              │
│  调用Tool: execute_script          │
│  执行: systemctl restart php-fpm   │
│  验证结果: CPU 95% → 30%           │
└─────────────────────────────────────┘
      │
      ▼
┌─────────────────────────────────────┐
│         Report Agent                │
│  生成执行报告                        │
└─────────────────────────────────────┘
```

---

## 六、API设计

### 6.1 Agent管理 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/agents | Agent列表 |
| GET | /api/agents/enabled | 启用的Agent |
| GET | /api/agents/{id} | Agent详情 |
| PUT | /api/agents/{id} | 更新Agent |

### 6.2 Agent对话 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/agents/chat | 发起对话 |
| GET | /api/agents/chat/stream | SSE流式对话 |
| GET | /api/agents/tools | 可用工具列表 |
| GET | /api/agents/sessions/{id} | 会话历史 |

---

## 七、使用示例

### 7.1 普通对话

```bash
curl -X POST http://localhost:8080/api/agents/chat \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user001",
    "message": "服务器CPU使用率很高怎么办？"
  }'
```

### 7.2 SSE流式对话

```javascript
const response = await fetch(
  'http://localhost:8080/api/agents/chat/stream?message=服务器出问题了&userId=user001'
);
const reader = response.body.getReader();
while (true) {
  const { done, value } = await reader.read();
  if (done) break;
  console.log(new TextDecoder().decode(value));
}
```

### 7.3 获取可用工具

```bash
curl http://localhost:8080/api/agents/tools
```

---

## 八、集成说明

### 8.1 LLM集成

Agent系统通过LlmService调用LLM：

```java
@Service
public class AgentCoordinator {
    private final LlmService llmService;
    
    public String coordinate(String sessionId, String userRequest) {
        return llmService.chat(userRequest, null);
    }
}
```

### 8.2 Tool集成

通过ToolExecutionService执行Tool：

```java
@Service
public class ToolExecutionService {
    private final ToolRegistry toolRegistry;
    
    public Object execute(String toolName, Map<String, Object> params) {
        return toolRegistry.execute(toolName, params);
    }
}
```

---

## 九、实施状态

| 功能 | 状态 |
|------|------|
| AIAgent实体+管理 | ✅ 完成 |
| LangGraph4j工作流 | ✅ 完成 |
| LLM集成(Ollama) | ✅ 完成 |
| Tool注册机制 | ✅ 完成 |
| 5个Tool实现 | ✅ 完成 |
| SSE流式输出 | ✅ 完成 |
| 前端对话页面 | 🔄 待完善 |

---

## 十、注意事项

1. **Ollama需提前启动**：确保Ollama服务运行在 `http://localhost:11434`
2. **模型需下载**：使用 `ollama pull llama3` 下载模型
3. **Tool执行**：当前Tool为模拟实现，生产环境需接入真实服务

---

> 模块版本：V2.0
> 最后更新：2026-03-10
