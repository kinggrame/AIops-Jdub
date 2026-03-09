# AIOps Backend MVP

一个基于 Spring Boot 3 / Java 17 的智能运维后端 MVP，多模块结构与 `backend_plan.md` 对齐，当前重点是先打通 Agent 上报、告警分析、知识检索、命令下发这一条闭环链路。

联调说明请查看 `INTEGRATION.md`。

## 目录结构

```text
aiops-backend/
├── aiops-common       # 通用响应与异常处理
├── aiops-cache        # 双层缓存抽象（MVP 为内存实现）
├── aiops-connection   # Agent 注册、心跳、命令结果
├── aiops-command      # 命令白名单与安全校验
├── aiops-search       # 日志索引与检索（MVP 为内存实现）
├── aiops-rag          # 知识检索（MVP 为内存模拟）
├── aiops-detection    # 告警评估
├── aiops-core         # Agent 分析、Provider fallback、闭环流程编排
├── aiops-api          # REST API
└── aiops-web          # Spring Boot 启动模块
```

## 当前 MVP 能力

- Agent 注册：`POST /api/v1/agent/register`
- Agent 上报：`POST /api/v1/agent/report`
- 命令结果回传：`POST /api/v1/agent/command/result`
- Agent 对话：`POST /api/v1/agent/chat`
- 知识检索：`POST /api/v1/knowledge/search`
- 告警管理：`GET /api/v1/alerts`、`POST /api/v1/alerts`
- 指标与日志：`GET /api/v1/metrics/agents`、`POST /api/v1/metrics/logs/search`

## 已打通的闭环流程

`agent/report -> alert evaluation -> analysis -> command dispatch`

处理流程由 `aiops-core/src/main/java/com/aiops/core/service/DefaultAgentReportFlowService.java` 统一编排：

1. 接收 Agent 指标与事件
2. 写入搜索索引
3. 评估阈值并生成告警
4. 调用分析 Agent，结合日志与知识库生成分析结果
5. 若存在 critical 告警，则自动生成命令下发元数据

## 启动方式

在 `aiops-backend/` 目录执行：

```bash
mvn spring-boot:run -pl aiops-web -am
```

默认端口：`8080`

## 配置说明

主配置文件：`aiops-web/src/main/resources/application.yml`

关键配置：

- `aiops.security.seed`：Agent bootstrap token 种子，首次注册后服务端会签发长期 agent token
- `aiops.tunnel.*`：可选的内网穿透生命周期管理，默认关闭
- `aiops.cache.*`：缓存参数
- `spring.data.redis.*`：Redis 二级缓存连接配置
- `aiops.llm.*`：Provider 开关，默认使用 `Ollama` 模拟 provider
- `aiops.command.*`：允许/禁止命令列表

## 双层缓存

后端当前已经接入 `Caffeine + Redis` 双层缓存：

- 一级缓存：Caffeine，本地热点数据快速命中
- 二级缓存：Redis，多实例共享缓存数据
- Redis 不可用时自动降级为 Caffeine-only，不阻塞主流程

### 启动 Redis（可选但推荐）

如果本地已经安装 Redis，可直接启动；也可以使用 Docker：

```bash
docker run -d --name aiops-redis -p 6379:6379 redis:7
```

### 环境变量示例

```bash
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
AIOPS_CACHE_REDIS_ENABLED=true
```

当 Redis 未启动时，日志中会看到缓存降级 warning，这是预期行为。

## 安全优先的内网穿透

后端现在支持可选的 tunnel 进程托管，但默认关闭，且默认只允许把本地 `127.0.0.1:8080` 暴露出去。

默认配置在 `aiops-backend/aiops-web/src/main/resources/application.yml`：

```yaml
aiops:
  tunnel:
    enabled: false
    command: cloudflared
    arguments: tunnel --url http://127.0.0.1:8080
    allow-non-local-url: false
```

安全约束：

- 默认不启动 tunnel
- 仅允许 `cloudflared`、`frpc`、`ngrok` 三种命令
- 默认只允许转发 `localhost`，避免误把局域网其他服务暴露到公网
- backend 停止时会主动关闭 tunnel 子进程

建议：

- 演示环境优先使用临时 tunnel
- 只暴露 backend，绝不暴露 MySQL、Redis 或本地文件服务
- 只在需要外部 agent 接入时开启，演示结束立即关闭
- tunnel 启动后可访问 `/api/v1/metrics/tunnel` 查看当前 targetUrl 和识别到的 publicUrl
- 可访问 `/api/v1/metrics/tunnel/install-preview` 获取可直接复制的 agent 启动命令预览

## API 示例

### 1. 注册 Agent

```bash
curl -X POST http://localhost:8080/api/v1/agent/register \
  -H "Content-Type: application/json" \
  -d '{
    "hostname": "server-001",
    "ip": "10.0.0.1",
    "token": "aiops-mvp-seed-demo-token",
    "capabilities": ["cpu", "memory", "disk"]
  }'
```

### 2. 上报指标并触发闭环

将上一步返回的 `agentId` 和 `token` 带入：

```bash
curl -X POST http://localhost:8080/api/v1/agent/report \
  -H "Authorization: Bearer <issuedToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "<agentId>",
    "hostname": "server-001",
    "metrics": {
      "cpu": {"usage": 95},
      "memory": {"usage": 88}
    },
    "events": [
      {"type": "threshold", "metric": "cpu.usage", "value": 95, "target": "ai"}
    ]
  }'
```

典型响应会包含：

- `alerts`
- `analysis`
- `status=command_dispatched`
- `command`

### 3. Agent 对话

```bash
curl -X POST http://localhost:8080/api/v1/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "agentType": "analysis",
    "message": "cpu usage is high",
    "metrics": {
      "cpu": {"usage": 92},
      "memory": {"usage": 70}
    },
    "events": []
  }'
```

### 4. 知识检索

```bash
curl -X POST http://localhost:8080/api/v1/knowledge/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "cpu nginx restart",
    "topK": 2
  }'
```

## 测试

执行：

```bash
mvn test
```

当前测试覆盖：

- Spring Boot 上下文加载
- Agent 注册 + 上报 + 告警 + 分析 + 命令下发主流程
- Agent 对话接口
- 知识检索接口

测试文件：

- `aiops-web/src/test/java/com/aiops/web/AiopsApplicationTests.java`
- `aiops-web/src/test/java/com/aiops/web/AgentFlowIntegrationTests.java`

## Javadoc 说明

已按 `backend_plan.md` 中 519 行之后的要求，补充关键接口的 Javadoc，包括：

- `aiops-core/src/main/java/com/aiops/core/service/AgentService.java`
- `aiops-connection/src/main/java/com/aiops/connection/service/CommandDispatchService.java`
- `aiops-cache/src/main/java/com/aiops/cache/service/CacheService.java`
- `aiops-rag/src/main/java/com/aiops/rag/service/KnowledgeService.java`
- `aiops-detection/src/main/java/com/aiops/detection/service/AlertService.java`
- `aiops-command/src/main/java/com/aiops/command/service/CommandService.java`
- `aiops-core/src/main/java/com/aiops/core/service/AgentReportFlowService.java`

后续如果要严格覆盖到更多实现类和 DTO，我可以继续补全整套模块级 Javadoc。

## 与前端和 Agent 联调链路

完整联调顺序如下：

### 1. 启动后端

```bash
cd aiops-backend
mvn spring-boot:run -pl aiops-web -am
```

服务端提供：

- HTTP API: `http://localhost:8080/api/v1`
- WebSocket: `ws://localhost:8080/ws/agent/{agentId}`

### 2. 启动前端

```bash
cd aiops-frontend
npm install
npm run dev
```

访问：

```text
http://localhost:5173
```

### 3. 启动 Agent

先设置 bootstrap token：

```bash
set AIOPS_BOOTSTRAP_TOKEN=aiops-mvp-seed-demo-token
```

再启动：

```bash
cd aiops-agent
go mod tidy
go run ./cmd -c config.yaml
```

Agent 本地面板：

```text
http://localhost:8089
```

### 4. 实际联调链路

1. Agent 启动后调用 `/api/v1/agent/register`
2. 后端返回 `agentId` 和服务端签发的 agent token
3. Agent 使用签发 token 建立 WebSocket 连接到 `/ws/agent/{agentId}`
4. Agent 周期采集真实或回退指标，并带 token 调用 `/api/v1/agent/report`
5. 后端生成告警、分析结果，必要时下发命令
6. 如果 Agent WebSocket 在线，后端立即发送命令 envelope
7. Agent 执行命令后调用 `/api/v1/agent/command/result`
8. 前端在仪表盘、客户端页、告警页看到实时数据变化

### 5. 演示建议路径

建议你按这个顺序现场演示：

1. 打开前端仪表盘，初始为空
2. 启动 Agent，观察客户端页出现注册实例
3. 等待自动上报，仪表盘出现 CPU、内存、告警与日志数据
4. 在客户端页触发一次高负载模拟上报
5. 在告警页查看新增 critical 告警
6. 在 Agent 本地面板确认最近事件
7. 查看命令执行结果和后端命令记录

## 后续替换点

当前实现为 MVP 内存版，后续可逐步替换为：

- Redis / Caffeine 真正双层缓存
- Elasticsearch 日志检索
- Milvus 向量检索
- RocketMQ 异步分析与通知
- WebSocket 真正命令下发
- LangChain4j 接入真实 LLM Provider
