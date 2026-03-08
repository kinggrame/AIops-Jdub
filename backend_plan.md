# 智能运维平台 MVP - 后端搭建计划

## 一、项目概述

基于简历中的技术栈，搭建一个前后端分离的智能运维（AIOps）平台后端 MVP。

### 技术选型

| 层级 | 技术选型 |
|------|----------|
| 框架 | Spring Boot 3.x (Java 17) + Spring Cloud (预留) |
| LLM/Agent | LangChain4j + Spring Boot Starter |
| 知识图谱 | Neo4j (故障传播链、依赖关系) |
| 流计算 | Apache Flink (实时指标处理，预留接口) |
| 搜索引擎 | Elasticsearch (日志/数据搜索) |
| RAG | Milvus 向量数据库 |
| 缓存 | Caffeine (本地) + Redis (分布式) |
| MQ | RocketMQ (集成 Canal，实现 Binlog 同步) |
| 监控 | Prometheus + AlertManager |
| 客户端 | 多语言（Python/Go/Node.js） |
| CI/CD | Jenkins / GitLab CI / GitHub Actions (预留) |
| 数据库 | MySQL 8 |

### LLM 调用优先级
API → Claude → Ollama

### 架构模式
**Server-Agent 架构**：Agent 主动 Push 数据到服务端，服务端只需开放 443 端口

---

## 二、项目结构（多模块 Maven）

```
aiops-platform/
├── pom.xml                                    # 父 POM，版本管理
│
├── aiops-common/                              # 公共模块
│   └── src/main/java/com/aiops/common/
│       ├── config/                            # 通用配置
│       ├── constant/                          # 常量定义
│       ├── exception/                         # 自定义异常
│       │   ├── BusinessException.java
│       │   └── GlobalExceptionHandler.java
│       └── util/                              # 工具类
│
├── aiops-entity/                             # 共享实体模块
│   └── src/main/java/com/aiops/entity/
│       ├── user/                             # 用户领域
│       │   ├── User.java                     # 用户实体
│       │   └── Role.java                     # 角色实体
│       └── base/                             # 基础领域
│           └── BaseEntity.java               # 基础实体（公共字段）
│
├── aiops-api/                                 # API 模块
│   └── src/main/java/com/aiops/api/
│       ├── dto/                               # DTO (Request/Response)
│       │   ├── request/
│       │   └── response/
│       ├── controller/                       # REST Controllers
│       └── advice/                            # 全局异常处理
│
├── aiops-core/                                # 核心业务（LangChain4j）
│   └── src/main/java/com/aiops/core/
│       ├── entity/                           # Agent 领域实体
│       │   ├── Conversation.java             # 对话会话
│       │   └── Message.java                  # 消息记录
│       ├── config/                           # LangChain4j 配置
│       ├── llm/                               # LLM 提供者层
│       │   ├── LlmProvider.java               # LLM Provider 接口
│       │   ├── OpenAiProvider.java            # OpenAI 实现
│       │   ├── AnthropicProvider.java         # Claude 实现
│       │   └── OllamaProvider.java            # Ollama 实现
│       ├── agent/                             # Agent 框架
│       │   ├── DataAgent.java                 # 数据分析 Agent
│       │   ├── AnalysisAgent.java             # 分析 Agent
│       │   ├── ReportAgent.java               # 报告生成 Agent
│       │   └── AgentFactory.java              # Agent 工厂
│       └── service/                           # 核心服务
│           └── AgentService.java
│
├── aiops-rag/                                 # RAG 模块
│   └── src/main/java/com/aiops/rag/
│       ├── entity/                           # 知识库领域实体
│       │   ├── Knowledge.java                # 知识条目
│       │   └── Category.java                 # 分类
│       ├── config/                           # Milvus 配置
│       ├── vector/                            # 向量存储
│       │   └── MilvusVectorStore.java
│       ├── knowledge/                         # 知识库管理
│       └── retrieval/                         # 检索服务
│           └── RetrievalService.java
│
├── aiops-cache/                               # 缓存模块
│   └── src/main/java/com/aiops/cache/
│       ├── config/                            # Caffeine + Redis 配置
│       │   ├── CaffeineConfig.java
│       │   └── RedisConfig.java
│       └── service/                          # 缓存服务
│           └── CacheService.java
│
├── aiops-search/                              # 搜索模块（Elasticsearch）
│   └── src/main/java/com/aiops/search/
│       ├── entity/                           # 日志/指标领域实体
│       │   ├── LogEntry.java                 # 日志条目
│       │   ├── MetricEntry.java              # 指标数据
│       │   └── AlertRecord.java              # 告警记录
│       ├── config/                           # ES 配置
│       │   └── ElasticsearchConfig.java
│       ├── document/                         # 文档实体
│       │   └── LogDocument.java
│       ├── repository/                       # ES 仓库
│       │   └── LogRepository.java
│       └── service/                          # 搜索服务
│           └── SearchService.java
│
├── aiops-mq/                                  # MQ 模块（集成 RocketMQ）
│   └── src/main/java/com/aiops/mq/
│       ├── config/                            # RocketMQ 配置
│       ├── producer/                          # 生产者
│       │   └── RocketMQProducer.java
│       ├── consumer/                          # 消费者
│       │   └── RocketMQConsumer.java
│       ├── listener/                          # 消息监听器
│       │   └── BinlogEventListener.java       # Canal 监听
│       └── context/                           # 上下文传递
│           └── MessageContext.java
│
├── aiops-detection/                           # 异常检测模块
│   └── src/main/java/com/aiops/detection/
│       ├── entity/                           # 告警领域实体
│       │   ├── Alert.java                    # 告警
│       │   └── AlertRule.java                # 告警规则
│       ├── config/                           # 配置
│       ├── model/                            # 异常检测模型
│       │   └── AnomalyDetector.java
│       └── alert/                             # 告警管理
│           └── AlertService.java
│
├── aiops-task/                                # 任务执行模块
│   └── src/main/java/com/aiops/task/
│       ├── entity/                           # 任务领域实体
│       │   ├── Task.java                     # 任务
│       │   └── Script.java                   # 脚本
│       ├── config/                           # 配置
│       ├── tool/                             # Agent 工具调用框架
│       │   ├── registry/                     # 工具注册中心
│       │   │   └── ToolRegistry.java         # 工具注册表
│       │   ├── executor/                     # 工具执行器
│       │   │   └── ToolExecutor.java         # 工具执行接口
│       │   └── builtin/                      # 内置工具
│       │       ├── LogQueryTool.java         # 日志查询工具
│       │       ├── MetricQueryTool.java      # 指标查询工具
│       │       └── ScriptExecuteTool.java    # 脚本执行工具
│       ├── script/                           # 脚本生成
│       │   └── ScriptGenerator.java
│       └── executor/                          # 脚本执行器
│           └── ScriptExecutor.java
│
├── aiops-graph/                               # 知识图谱模块（Neo4j）
│   └── src/main/java/com/aiops/graph/
│       ├── entity/                           # 图谱领域实体
│       │   ├── Node.java                     # 节点（服务/组件）
│       │   └── Edge.java                     # 边（依赖关系）
│       ├── config/                           # Neo4j 配置
│       ├── repository/                       # 图数据库仓库
│       │   └── GraphRepository.java
│       ├── service/                          # 图谱服务
│       │   ├── GraphService.java             # 图谱管理
│       │   └── PropagationService.java       # 故障传播分析
│       └── query/                            # 图查询
│           └── GraphQueryService.java
│
├── aiops-stream/                              # 流计算模块（Flink 预留）
│   └── src/main/java/com/aiops/stream/
│       ├── config/                           # Flink 配置（预留）
│       ├── job/                              # Flink 作业
│       │   └── MetricAggregationJob.java     # 指标聚合作业
│       ├── source/                           # 数据源
│       │   └── MetricSource.java
│       ├── sink/                             # 数据 sink
│       │   └── AlertSink.java
│       └── process/                          # 流处理
│           └── MetricProcessFunction.java   # 指标处理函数
│
├── aiops-monitor/                             # 监控模块（Prometheus）
│   └── src/main/java/com/aiops/monitor/
│       ├── config/                           # Prometheus 配置
│       ├── collector/                        # 指标采集
│       │   └── MetricsCollector.java
│       ├── alert/                            # 告警管理
│       │   ├── AlertManager.java             # 告警管理器
│       │   └── AlertRuleEngine.java          # 告警规则引擎
│       └── notification/                     # 通知服务
│           ├── NotificationService.java      # 通知接口
│           ├── EmailNotifier.java            # 邮件通知
│           ├── DingTalkNotifier.java         # 钉钉通知
│           └── WebhookNotifier.java          # Webhook 通知
│
├── aiops-agent/                              # Agent 客户端（多语言）
│   └── src/main/                            # 独立项目，不在主 Maven 中
│       ├── python/                          # Python 客户端
│       │   ├── aiops_agent/
│       │   │   ├── collector/               # 指标采集
│       │   │   ├── trigger/                 # 触发规则引擎
│       │   │   ├── sender/                  # 数据发送
│       │   │   └── config.py               # 配置管理
│       │   └── requirements.txt
│       │
│       ├── go/                              # Go 客户端（可选）
│       │   └── aiops-agent/
│       │
│       └── nodejs/                          # Node.js 客户端（可选）
│           └── aiops-agent/
│
├── aiops-cicd/                              # CI/CD 集成模块（预留）
│   └── src/main/java/com/aiops/cicd/
│       ├── config/                          # CI/CD 配置
│       ├── webhook/                         # Webhook 接收
│       │   └── CIWebhookHandler.java
│       ├── pipeline/                        # 流水线管理
│       │   └── PipelineService.java
│       └── rollback/                        # 回滚执行
│           └── RollbackService.java
│
├── aiops-command/                           # 命令执行模块
│   └── src/main/java/com/aiops/command/
│       ├── config/                          # 命令配置
│       ├── whitelist/                       # 命令白名单
│       │   └── CommandWhitelist.java
│       ├── executor/                        # 命令执行器
│       │   └── CommandExecutor.java
│       ├── sandbox/                         # 沙箱执行器（Agent Tool 执行）
│       │   └── ToolSandbox.java
│       └── security/                        # 安全控制
│           └── SecurityValidator.java
│
├── aiops-connection/                        # 服务端-客户端通信模块
│   └── src/main/java/com/aiops/connection/
│       ├── config/                          # WebSocket 配置
│       ├── server/                          # 服务端
│       │   ├── AgentServer.java             # Agent 连接管理
│       │   └── CommandDispatcher.java       # 命令分发器
│       ├── client/                          # 客户端（用于反向连接）
│       │   └── AgentClientRegistry.java     # 客户端注册表
│       └── protocol/                        # 通信协议
│           └── MessageProtocol.java
│
└── aiops-web/                              # Web 启动类
    └── src/main/java/com/aiops/web/
        ├── AiopsApplication.java
        └── controller/
```

---

## 三、MVP 功能范围

| 模块 | MVP 实现 | 状态 |
|------|----------|------|
| **aiops-api** | REST API 定义、全局异常处理 | ✅ |
| **aiops-core** | LangChain4j 集成、多 Provider 切换、Agent 框架 | ✅ |
| **aiops-rag** | Milvus 向量检索基础功能 | ✅ |
| **aiops-cache** | Caffeine + Redis 双重缓存 | ✅ |
| **aiops-search** | Elasticsearch 日志搜索基础功能 | ✅ |
| **aiops-mq** | RocketMQ 集成 + Canal 同步 + 接口封装 | ✅ |
| **aiops-detection** | 基础接口 + 简单实现，ML 模型预留 | ⏳ |
| **aiops-task** | 基础接口 + 工具调用框架（预留 PTC） | ⏳ |
| **aiops-graph** | Neo4j 接口预留，图谱查询基础功能 | ⏳ |
| **aiops-stream** | Flink 接口预留，流处理作业框架 | ⏳ |
| **aiops-monitor** | Prometheus 接口预留，告警规则基础 | ⏳ |
| **aiops-agent** | 多语言客户端 + 触发规则引擎 | ⏳ |
| **aiops-cicd** | CI/CD 集成预留接口 | ⏳ |
| **aiops-command** | 命令执行 + 白名单 + 沙箱 | ⏳ |
| **aiops-connection** | 服务端-客户端通信（WebSocket） | ⏳ |

---

## 四、Server-Agent 架构设计

### 1. 架构概述

```
┌─────────────────────────────────────────────────────────────────────┐
│                          AIOps 服务端                                │
│                    (用户部署的主程序 / SaaS)                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │
│  │   Web UI    │  │  Agent Core  │  │  Data Store  │            │
│  │   (React)   │  │  (处理请求)   │  │  (ES/MySQL)  │            │
│  └──────────────┘  └──────────────┘  └──────────────┘            │
│         ▲                   ▲                                        │
│         │             [WebSocket / HTTP / MQTT]                    │
│         │                   │                                        │
└─────────┼───────────────────┼────────────────────────────────────────┘
          │                   │
          │    Agent 主动 Push（无需开放端口给目标服务器）
          │                   │
┌─────────┼───────────────────┼────────────────────────────────────────┐
│         ▼                   ▼                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │
│  │ 目标服务器 A  │  │ 目标服务器 B  │  │ 目标服务器 C  │            │
│  │ (Python/Go)  │  │ (Python/Go)  │  │ (Python/Go)  │            │
│  │              │  │              │  │              │            │
│  │ - 指标采集   │  │ - 指标采集   │  │ - 指标采集   │            │
│  │ - 触发判断   │  │ - 触发判断   │  │ - 触发判断   │            │
│  │ - 异常上报   │  │ - 异常上报   │  │ - 异常上报   │            │
│  └──────────────┘  └──────────────┘  └──────────────┘            │
│                                                                      │
│  只需能访问服务端 443 端口，无需开放任何入站端口                      │
└──────────────────────────────────────────────────────────────────────┘
```

### 2. 数据发送策略

**核心原则**：不是所有数据都发给 AI，节省 Token

| 数据类型 | 发送给谁 | 说明 |
|----------|----------|------|
| **基础指标** | 服务端 | CPU/内存/磁盘，仅存储和展示 |
| **异常事件** | AI Agent | 需要分析、决策 |
| **告警** | AI Agent | 需要诊断、建议/执行 |
| **日志（异常时）** | AI Agent | 辅助分析，附带上下文 |

```
客户端判断逻辑：
┌─────────────────────────────────────┐
│         采集数据                     │
└─────────────┬───────────────────────┘
              ▼
      ┌───────────────┐
      │  超过阈值？    │
      └───────┬───────┘
         Yes / \No
          ▼      ▼
    ┌─────────┐  ┌──────────┐
    │  AI处理  │  │  仅存储   │
    │+分析    │  │  不发AI   │
    │+决策    │  │(节省token)│
    └─────────┘  └──────────┘
```

### 3. 客户端触发规则配置

```yaml
# 客户端配置 (config.yaml)
server:
  url: https://aiops.example.com  # 服务端地址
  token: ${AIOPS_TOKEN}           # 认证 Token（服务端生成）

collection:
  interval: 60                    # 基础指标采集间隔(秒)

triggers:
  # 触发条件：超过阈值 → 决定发送给谁
  - name: cpu_critical
    metric: cpu.usage
    threshold: 95
    target: ai                    # 发送给 AI 分析+决策

  - name: disk_full
    metric: disk.usage
    threshold: 90
    target: ai                    # 发送给 AI 建议处理方案

  - name: memory_warning
    metric: memory.usage
    threshold: 80
    target: server               # 仅存服务端，不发 AI

  - name: service_down
    metric: service.status
    value: down
    target: ai                   # 服务宕机必须发 AI

commands:
  allowed:                       # 允许 AI 执行的命令
    - restart_service
    - clear_cache
    - scale_deployment
    - get_logs
  forbidden:                     # 禁止执行的命令
    - rm -rf
    - shutdown
    - init 6
```

### 4. 安全认证方案

**方案**：Token 基于 AES 加密的 seed 认证

```
首次连接（注册）：
┌──────────┐    seed      ┌──────────┐
│  服务端   │ ──────────▶ │  客户端   │
│ (生成)    │             │          │
└──────────┘             └──────────┘
                          保存到:
                          - 环境变量 AIOPS_TOKEN
                          - 配置文件

后续请求：
┌──────────┐  token(AES加密)  ┌──────────┐
│  客户端   │ ──────────────▶ │  服务端   │
│          │  (Header)       │ (验证)    │
└──────────┘                 └──────────┘

Token 组成：{seed}+{timestamp}+{random}
加密方式：AES-256
```

| 安全层级 | 方案 |
|----------|------|
| 传输层 | HTTPS (TLS) |
| 应用层 | Token 认证（AES seed） |
| 命令层 | 命令白名单 |

### 5. 多语言客户端

| 语言 | 适用场景 | 优势 |
|------|----------|------|
| **Python** | 通用场景 | 运维最常用，生态丰富 |
| **Go** | 高性能场景 | 二进制部署，资源占用低 |
| **Node.js** | 轻量场景 | 快速集成 Web 技术 |

---

## 五、AI 自动闭环流程

```
┌─────────────────────────────────────────────────────────────┐
│                    AI Ops 智能闭环                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   检测 ──▶ 分析 ──▶ 决策 ──▶ 执行 ──▶ 验证                  │
│     │                              │                        │
│     │                              ▼                        │
│     │                    ┌─────────────────┐              │
│     │                    │   CI/CD 流水线   │              │
│     │                    │  - 获取版本      │              │
│     │                    │  - 执行回滚      │              │
│     │                    │  - 验证部署     │              │
│     │                    └─────────────────┘              │
│     │                              │                        │
│     │                              ▼                        │
│     │                    ┌─────────────────┐              │
│     │                    │ Git 版本管理     │              │
│     │                    │ - 配置版本      │              │
│     │                    │ - 回滚记录      │              │
│     │                    └─────────────────┘              │
│     │                                                      │
│     ◀──────────────────────────────────────               │
│                    验证结果回传                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 六、高级能力规划（开源项目愿景）

本项目定位为 **AIOps 领域开源项目**，以下为高阶能力规划：

### 1. 多 Agent 协作框架

| 能力 | 说明 | 优先级 |
|------|------|--------|
| Agent 通信协议 | Agent 间消息传递、任务分发 | 高 |
| 任务编排 | DAG 任务调度、依赖管理 | 高 |
| Agent 生命周期 | 创建、执行、销毁、监控 | 中 |

### 2. Programmatic Tool Calling (PTC)

| 能力 | 说明 | 优先级 |
|------|------|--------|
| 工具注册中心 | 动态注册/发现运维工具 | 高 |
| 工具执行器 | Python/Shell 脚本安全执行 | 高 |
| LLM 脚本生成 | 自动生成运维脚本 | 中 |
| 工具编排 | 多个工具链式调用 | 中 |

### 3. 知识图谱 + RAG 融合

| 能力 | 说明 | 优先级 |
|------|------|--------|
| Neo4j 图谱 | 故障传播链、服务依赖关系 | 高 |
| 混合检索 | 向量检索 + 图查询融合 | 高 |
| 智能推荐 | 基于历史案例推荐解决方案 | 中 |

### 4. 实时流计算

| 能力 | 说明 | 优先级 |
|------|------|--------|
| Flink 作业 | 指标实时聚合、窗口计算 | 高 |
| 异常检测模型 | 时序异常检测算法集成 | 中 |
| 告警收敛 | 告警聚合、抑制、升级 | 中 |

### 5. 智能告警系统

| 能力 | 说明 | 优先级 |
|------|------|--------|
| Prometheus 集成 | 指标采集、存储、查询 | 高 |
| 告警规则引擎 | 自定义告警规则、表达式 | 高 |
| 多通道通知 | 邮件/钉钉/飞书/Webhook | 中 |

### 6. LLM 调用优化

| 能力 | 说明 | 优先级 |
|------|------|--------|
| 上下文管理 | Memory 缓存、压缩策略 | 高 |
| 请求缓存 | 相同请求复用结果 | 中 |
| 流量控制 | QPS 限流、熔断 | 中 |

### 7. 微服务架构（预留）

| 能力 | 说明 | 优先级 |
|------|------|--------|
| Spring Cloud | Gateway / Nacos / Sentinel | 低 |
| 服务拆分 | 按模块独立部署 | 低 |

---

## 五、Javadoc 规范

```java
/**
 * 类/接口描述
 *
 * <p>详细功能说明，包括业务场景、技术实现等。</p>
 *
 * <p><b>异步处理说明：</b></p>
 * <ul>
 *   <li>同步操作：直接执行，立即返回结果</li>
 *   <li>异步操作：通过 MQ 异步处理，如 RabbitTemplate / RocketMQTemplate</li>
 *   <li>异步回调：通过 CompletableFuture / Callback 机制</li>
 * </ul>
 *
 * <p><b>TODO (MVP 后实现)：</b></p>
 * <ul>
 *   <li>功能点1</li>
 *   <li>功能点2</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public class ExampleService {
    
    /**
     * 方法描述
     *
     * @param param 参数说明
     * @return 返回值说明
     * @throws Exception 可能抛出的异常
     */
    public void method(Type param) {}
}
```

---

## 五、异步场景说明

| 场景 | 同步/异步 | 流程 | 目标成果 |
|------|----------|------|----------|
| **Canal → MQ → ES** | 异步 | MySQL Binlog → Canal → RocketMQ → 消费者 → ES | 实时数据同步 |
| **Agent 对话** | 异步 | 请求 → 异步处理 → 流式响应 / 回调 | 非阻塞交互 |
| **告警检测** | 异步 | 指标采集 → 异步检测 → MQ 通知 → 推送 | 实时告警 |
| **日志采集** | 异步 | 日志 → 批量缓冲 → 异步批量写入 ES | 高吞吐写入 |
| **Caffeine ↔ Redis** | 异步 | 写入 → Caffeine → 异步同步 Redis | 多级缓存一致性 |
| **Redis → MySQL** | 异步 | 缓存数据 → 异步批量落库 | 持久化存储 |

> **说明**：MVP 阶段采用简化实现，但接口需预留异步能力，具体异步流程在 Javadoc 中标注。

---

## 六、关键接口示例

### 1. Agent 对话服务

```java
/**
 * Agent 对话服务接口
 *
 * <p>提供基于 LangChain4j 的多 Agent 协作对话能力，支持 DataAgent、AnalysisAgent、ReportAgent
 * 三种 Agent 的协调工作，实现日志解析、异常检测与分析报告生成的自动化流程。</p>
 *
 * <p>LLM 调用优先级：OpenAI API > Claude > Ollama</p>
 *
 * <p><b>异步处理说明：</b></p>
 * <ul>
 *   <li>同步模式：即时返回完整响应（MVP 阶段实现）</li>
 *   <li>异步模式：通过 CompletableFuture 异步处理，支持流式响应（TODO）</li>
 *   <li>消息队列：复杂分析任务通过 RocketMQ 异步执行，结果回调通知（TODO）</li>
 * </ul>
 *
 * <p><b>TODO (MVP 后实现)：</b></p>
 * <ul>
 *   <li>集成 RocketMQ 实现异步任务队列</li>
 *   <li>支持 SSE 流式响应</li>
 *   <li>添加对话上下文管理（Memory）</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public interface AgentService {

    /**
     * 处理 Agent 对话请求
     *
     * <p>根据请求中的 agentType 选择对应的 Agent 处理。</p>
     *
     * <p><b>异步说明：</b> MVP 阶段同步返回，TODO 改为 CompletableFuture 异步</p>
     *
     * @param request 对话请求
     * @return 对话响应
     */
    AgentChatResponse chat(AgentChatRequest request);
}
```

### 2. MQ 消息生产者

```java
/**
 * 消息生产者接口
 *
 * <p>MQ 生产者抽象接口，用于封装 RocketMQ 的消息发送能力。
 * 支持同步/异步发送模式，配合 Canal 实现 MySQL Binlog 实时同步。</p>
 *
 * <p><b>异步处理说明：</b></p>
 * <ul>
 *   <li>同步发送：send() 方法阻塞等待 Broker 确认</li>
 *   <li>异步发送：sendAsync() 方法通过 CompletableFuture 异步回调</li>
 *   <li>单向发送：sendOneway() 方法不等待响应，适用于日志类消息</li>
 * </ul>
 *
 * <p><b>应用场景：</b></p>
 * <ul>
 *   <li>Canal → RocketMQ：数据库变更实时同步</li>
 *   <li>告警通知：异步推送告警消息到通知服务</li>
 *   <li>任务分发：将 Agent 任务分发到执行队列</li>
 * </ul>
 *
 * <p><b>TODO (MVP 后实现)：</b></p>
 * <ul>
 *   <li>支持消息顺序性保证</li>
 *   <li>消息事务支持（本地事务 + 消息事务）</li>
 *   <li>消息轨迹追踪</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public interface MessageProducer {
    
    /**
     * 同步发送消息到指定 Topic
     *
     * <p>阻塞等待 Broker 确认，适用于需要可靠投递的场景。</p>
     *
     * @param topic   目标 Topic，不能为空
     * @param message 消息内容，不能为空
     * @return 发送结果，包含 MessageId
     */
    SendResult send(String topic, String message);
    
    /**
     * 异步发送消息
     *
     * <p>通过 CompletableFuture 异步回调，适用于高吞吐场景。</p>
     *
     * @param topic    目标 Topic
     * @param message  消息内容
     * @return CompletableFuture 异步结果
     */
    CompletableFuture<SendResult> sendAsync(String topic, String message);
}
```

### 3. 缓存服务（双层缓存）

```java
/**
 * 缓存服务接口
 *
 * <p>提供 Caffeine 本地缓存 + Redis 分布式缓存的双层缓存能力。
 * 支持缓存一致性、过期策略、分布式锁等功能。</p>
 *
 * <p><b>异步处理说明：</b></p>
 * <ul>
 *   <li>读操作：先查 Caffeine → 未命中查 Redis → 未命中查 DB</li>
 *   <li>写操作：先写 DB → 写 Caffeine → 异步写 Redis</li>
 *   <li>Caffeine → Redis 同步：异步任务定期同步，延迟可配置</li>
 *   <li>Redis → MySQL 落库：定时任务批量异步落库，支持削峰填谷</li>
 * </ul>
 *
 * <p><b>TODO (MVP 后实现)：</b></p>
 * <ul>
 *   <li>Caffeine 与 Redis 异步同步机制（Scheduled Task）</li>
 *   <li>Redis 批量异步落库（Write-Behind 模式）</li>
 *   <li>分布式锁（Redisson）解决热点数据竞争</li>
 *   <li>缓存预热机制</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public interface CacheService {
    
    /**
     * 获取缓存值
     *
     * <p>三层读取：Caffeine → Redis → Database</p>
     *
     * @param key 缓存键
     * @return 缓存值，不存在返回 null
     */
    <T> T get(String key);
    
    /**
     * 设置缓存值
     *
     * <p><b>异步说明：</b> 同步写 Caffeine，异步写 Redis</p>
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param ttl   过期时间
     */
    void set(String key, Object value, Duration ttl);
    
    /**
     * 删除缓存
     *
     * <p><b>异步说明：</b> 同步删除 Caffeine，异步删除 Redis</p>
     *
     * @param key 缓存键
     */
    void delete(String key);
}

### 4. Agent 工具调用框架

```java
/**
 * 工具注册中心接口
 *
 * <p>提供 Agent 工具的动态注册、发现和管理能力。
 * 支持内置工具和自定义工具的注册，是 PTC (Programmatic Tool Calling) 的核心组件。</p>
 *
 * <p><b>异步处理说明：</b></p>
 * <ul>
 *   <li>工具注册：同步注册到内存注册表</li>
 *   <li>工具执行：通过线程池异步执行，耗时操作不阻塞 Agent</li>
 * </ul>
 *
 * <p><b>TODO (MVP 后实现)：</b></p>
 * <ul>
 *   <li>支持工具热注册/热卸载</li>
 *   <li>工具调用限流和熔断</li>
 *   <li>工具调用日志和审计</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public interface ToolRegistry {
    
    /**
     * 注册工具
     *
     * @param tool 工具实例
     */
    void register(Tool tool);
    
    /**
     * 获取工具
     *
     * @param toolName 工具名称
     * @return 工具实例
     */
    Tool get(String toolName);
    
    /**
     * 执行工具
     *
     * <p><b>异步说明：</b> 通过 CompletableFuture 异步执行</p>
     *
     * @param toolName 工具名称
     * @param params   工具参数
     * @return 执行结果
     */
    CompletableFuture<ToolResult> execute(String toolName, Map<String, Object> params);
}

/**
 * 工具执行器接口
 *
 * <p>负责执行具体的运维工具，包括脚本执行、API 调用等。
 * 支持 Python、Shell 等多种脚本类型。</p>
 *
 * <p><b>TODO (MVP 后实现)：</b></p>
 * <ul>
 *   <li>脚本安全沙箱执行</li>
 *   <li>执行超时控制</li>
 *   <li>执行结果实时输出</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public interface ToolExecutor {
    
    /**
     * 执行脚本
     *
     * <p>执行 Python/Shell 运维脚本，返回执行结果。</p>
     *
     * <p><b>异步说明：</b> 脚本执行耗时较长，通过 CompletableFuture 异步返回</p>
     *
     * @param script 脚本内容
     * @param type   脚本类型 (PYTHON, SHELL)
     * @return 执行结果
     */
    CompletableFuture<ExecutionResult> execute(Script script, ScriptType type);
}
```

### 5. 知识图谱服务

```java
/**
 * 知识图谱服务接口
 *
 * <p>基于 Neo4j 提供运维知识图谱管理，支持故障传播分析、服务依赖关系查询。
 * 结合 Milvus 向量检索实现混合搜索能力。</p>
 *
 * <p><b>异步处理说明：</b></p>
 * <ul>
 *   <li>图谱查询：同步执行 Neo4j 查询</li>
 *   <li>向量检索：异步调用 Milvus</li>
 *   <li>混合搜索：并行查询后合并结果</li>
 * </ul>
 *
 * <p><b>TODO (MVP 后实现)：</b></p>
 * <ul>
 *   <li>故障传播链分析算法</li>
 *   <li>基于图谱的根因分析</li>
 *   <li>图谱可视化导出</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public interface GraphService {
    
    /**
     * 创建节点
     *
     * @param node 节点信息
     */
    void createNode(Node node);
    
    /**
     * 创建关系
     *
     * @param edge 边信息
     */
    void createEdge(Edge edge);
    
    /**
     * 查询故障传播路径
     *
     * <p>给定故障节点，查询可能的故障传播路径。</p>
     *
     * @param sourceNode 故障源节点
     * @return 传播路径列表
     */
    List<PropagationPath> queryPropagation(String sourceNode);
    
    /**
     * 混合检索
     *
     * <p>结合图查询和向量检索，返回综合结果。</p>
     *
     * <p><b>异步说明：</b> 向量检索部分异步执行</p>
     *
     * @param query     查询语句
     * @param topK      返回结果数
     * @return 混合检索结果
     */
    CompletableFuture<HybridSearchResult> hybridSearch(String query, int topK);
}
```

### 6. 流计算作业接口

```java
/**
 * Flink 流计算作业接口
 *
 * <p>提供 Flink 作业的抽象，用于实时指标处理、窗口计算、异常检测等场景。</p>
 *
 * <p><b>异步处理说明：</b></p>
 * <ul>
 *   <li>作业提交：异步提交到 Flink 集群</li>
 *   <li>指标处理：流式处理，低延迟</li>
 *   <li>结果输出：异步写入下游系统</li>
 * </ul>
 *
 * <p><b>TODO (MVP 后实现)：</b></p>
 * <ul>
 *   <li>Flink SQL 作业支持</li>
 *   <li>状态管理和 checkpoint</li>
 *   <li>作业监控和告警</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public interface FlinkJob {
    
    /**
     * 提交作业
     *
     * <p><b>异步说明：</b> 异步提交到 Flink 集群</p>
     *
     * @return 作业 ID
     */
    CompletableFuture<String> submit();
    
    /**
     * 取消作业
     *
     * @param jobId 作业 ID
     */
    void cancel(String jobId);
    
    /**
     * 获取作业状态
     *
     * @param jobId 作业 ID
     * @return 作业状态
     */
    JobStatus getStatus(String jobId);
}
```

### 7. 告警通知服务

```java
/**
 * 告警通知服务接口
 *
 * <p>提供多通道告警通知能力，支持邮件、钉钉、飞书、Webhook 等。
 * 是 AIOps 告警闭环的重要环节。</p>
 *
 * <p><b>异步处理说明：</b></p>
 * <ul>
 *   <li>通知发送：异步发送到各通道</li>
 *   <li>失败重试：异步重试机制</li>
 *   <li>发送频率控制：异步调度</li>
 * </ul>
 *
 * <p><b>TODO (MVP 后实现)：</b></p>
 * <ul>
 *   <li>通知模板管理</li>
 *   <li>告警升级策略</li>
 *   <li>通知效果追踪</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public interface NotificationService {
    
    /**
     * 发送告警通知
     *
     * <p><b>异步说明：</b> 通过线程池异步发送到各通知通道</p>
     *
     * @param alert   告警信息
     * @param channels 通知通道列表
     * @return 发送结果
     */
    CompletableFuture<NotificationResult> notify(Alert alert, List<Channel> channels);
}

/**
 * 告警规则引擎接口
 *
 * <p>基于 Prometheus 告警规则的引擎，支持自定义告警表达式。
 * 支持 PromQL、阈值告警、趋势告警等多种规则类型。</p>
 *
 * <p><b>TODO (MVP 后实现)：</b></p>
 * <ul>
 *   <li>告警规则管理（CRUD）</li>
 *   <li>规则动态更新</li>
 *   <li>告警聚合和收敛</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public interface AlertRuleEngine {
    
    /**
     * 评估告警规则
     *
     * <p><b>异步说明：</b> 定时异步评估所有规则</p>
     *
     * @param metrics 指标数据
     * @return 触发的告警列表
     */
    List<Alert> evaluate(Map<String, Double> metrics);
    
    /**
     * 创建告警规则
     *
     * @param rule 告警规则
     */
    void createRule(AlertRule rule);
}
```

---

## 七、配置规范（application.yml）

```yaml
spring:
  application:
    name: aiops-platform
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}
  datasource:
    url: ${MYSQL_URL:jdbc:mysql://localhost:3306/aiops?useUnicode=true&characterEncoding=utf8}
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:root}
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}

langchain4j:
  open-ai:
    chat-model:
      api-key: ${OPENAI_API_KEY:}
      model-name: ${OPENAI_MODEL:gpt-4o-mini}
  anthropic:
    chat-model:
      api-key: ${ANTHROPIC_API_KEY:}
  ollama:
    chat-model:
      base-url: ${OLLAMA_BASE_URL:http://localhost:11434}

milvus:
  host: ${MILVUS_HOST:localhost}
  port: ${MILVUS_PORT:19530}
  collection-name: ${MILVUS_COLLECTION:aiops_knowledge}

elasticsearch:
  host: ${ES_HOST:localhost}
  port: ${ES_PORT:9200}
  index-name: ${ES_INDEX:aiops-logs}

neo4j:
  uri: ${NEO4J_URI:bolt://localhost:7687}
  username: ${NEO4J_USERNAME:neo4j}
  password: ${NEO4J_PASSWORD:password}

flink:
  checkpoint-dir: ${FLINK_CHECKPOINT_DIR:/tmp/flink/checkpoints}
  state-backend: ${FLINK_STATE_BACKEND:rocksdb}

rocketmq:
  name-server: ${ROCKETMQ_NAMESERVER:localhost:9876}
  producer:
    group: ${ROCKETMQ_PRODUCER_GROUP:aiops-producer}

canal:
  server: ${CANAL_SERVER:localhost:11111}
  destination: ${CANAL_DESTINATION:example}

cache:
  caffeine:
    spec: maximumSize=1000,expireAfterWrite=600s

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      probes:
        enabled: true
```

---

## 八、依赖版本管理（父 POM）

```xml
<properties>
    <java.version>17</java.version>
    <spring-boot.version>3.3.0</spring-boot.version>
    <langchain4j.version>1.0.0-beta1</langchain4j.version>
    <elasticsearch.version>8.14.0</elasticsearch.version>
    <neo4j.version>5.18.0</neo4j.version>
    <flink.version>1.18.1</flink.version>
    <rocketmq.version>5.1.4</rocketmq.version>
    <milvus.version>2.3.4</milvus.version>
    <prometheus.version>0.19.0</prometheus.version>
</properties>
```
