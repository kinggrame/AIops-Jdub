# 架构文档目录

> 更新时间：2025-03-09

---

## 文档列表

| 序号 | 文档 | 说明 |
|------|------|------|
| 1 | [connection.md](connection.md) | Agent连接/配对层 |
| 2 | [monitoring.md](monitoring.md) | 监控指标模块 |
| 3 | [alert.md](alert.md) | 告警规则模块 |
| 4 | [action.md](action.md) | 执行动作模块 |
| 5 | [tool.md](tool.md) | Tool系统 (PTC) |
| 6 | [mcp.md](mcp.md) | MCP扩展模块 |
| 7 | [agent.md](agent.md) | Multi-Agent系统 |
| 8 | [elk.md](elk.md) | ELK日志系统 |
| 9 | [milvus.md](milvus.md) | Milvus向量数据库/知识库 |
| 10 | [llm.md](llm.md) | LLM配置模块 |

---

## 模块依赖关系

```
连接层 (connection)
    │
    ▼
监控指标 (monitoring) ──▶ 告警规则 (alert)
    │                         │
    │                         ▼
    │                  执行动作 (action)
    │                         │
    │                         ▼
    │                    Tool系统 (tool)
    │                         │
    ├─────────────────────────┼─────────────────────────┐
    │                         │                         │
    ▼                         ▼                         ▼
ELK日志 (eld)          Milvus (milvus)           MCP (mcp)
    │                         │                         │
    └─────────────────────────┼─────────────────────────┘
                              │
                              ▼
                        LLM (llm)
                              │
                              ▼
                         Agent (agent)
```

---

## 实施优先级

### 阶段一：基础架构 (2周)

| 模块 | 优先级 | 工作内容 |
|------|--------|----------|
| connection | P0 | Agent配对+认证+WebSocket |
| monitoring | P0 | 指标采集+存储 |
| alert | P0 | 告警规则+规则引擎 |
| action | P0 | 执行引擎+执行器 |

### 阶段二：Tool系统 (2周)

| 模块 | 优先级 | 工作内容 |
|------|--------|----------|
| tool | P0 | Tool注册+Script/ELK/Milvus/Agent Tool |
| eld | P0 | 日志采集+查询 |
| milvus | P0 | 向量存储+搜索 |
| mcp | P1 | MCP Server+扩展能力 |
| llm | P0 | Provider管理+Chat/Embedding分离 |

### 阶段三：Multi-Agent (1周)

| 模块 | 优先级 | 工作内容 |
|------|--------|----------|
| agent | P1 | Planner/Analyzer/Executor/Report Agent |

---

## 核心流程

### 完整执行流程

```
用户请求 / 告警触发
       │
       ▼
   Multi-Agent
       │
       ├─▶ Planner    ──▶ 制定计划
       │
       ├─▶ Analyzer   ──▶ 分析问题 (调用ELK/Milvus Tool)
       │
       ├─▶ Executor   ──▶ 执行操作 (调用Script/Agent/MCP Tool)
       │
       └─▶ Report     ──▶ 生成报告

       │
       ▼
   执行引擎
       │
       ├─▶ Script执行器   ──▶ Agent执行脚本
       ├─▶ Skill执行器    ──▶ 执行Skill
       ├─▶ MCP执行器      ──▶ 调用外部服务
       └─▶ 通知执行器     ──▶ 发送通知
```

---

> 文档版本：V1.0
