# AIOps Platform MVP

一个面向开源演示和后续扩展的智能运维平台 MVP，包含后端、前端控制台和轻量 Agent 客户端三部分。

## 项目结构

- `aiops-backend`：Spring Boot 后端，负责 Agent 注册、指标上报、告警分析、知识检索、命令下发、LLM集成
- `aiops-frontend`：React + TypeScript 前端控制台，负责运维可视化、Agent 管理、告警查看和智能对话
- `aiops-agent`：Go 编写的轻量客户端，负责指标采集、规则触发、命令执行和本地 Web 面板

## 文档入口

### 项目说明

- 后端：`aiops-backend/README.md`
- 前端：`aiops-frontend/README.md`
- Agent：`aiops-agent/README.md`

### 架构设计

- Agent多Agent系统：`architectures/agent.md`
- LLM配置：`architectures/llm.md`
- MCP协议：`architectures/mcp.md`
- 告警系统：`architectures/alert.md`
- 监控指标：`architectures/monitoring.md`
- 工具系统：`architectures/tool.md`

### 联调文档

- 后端联调：`aiops-backend/INTEGRATION.md`
- 前端联调：`aiops-frontend/INTEGRATION.md`
- Agent 联调：`aiops-agent/INTEGRATION.md`

## 快速启动

### 1. 启动后端

```bash
cd aiops-backend
mvn -pl aiops-web -am -DskipTests package
java -jar aiops-web/target/aiops-web-1.0.0-SNAPSHOT.jar
```

Windows 下一键启动前后端：

```powershell
./start-demo.ps1
```

Linux / macOS 下一键启动前后端：

```bash
chmod +x ./start-demo.sh
./start-demo.sh
```

### 2. 启动前端

```bash
cd aiops-frontend
npm install
npm run dev
```

### 3. 启动 Agent

Windows:

```bash
set AIOPS_BOOTSTRAP_TOKEN=aiops-mvp-seed-demo-token
cd aiops-agent
go mod tidy
go run ./cmd -c config.yaml
```

### 4. 配置LLM（可选）

系统默认使用Ollama本地模型。如需配置：

```bash
# 启动Ollama
ollama serve
ollama pull llama3
```

或配置OpenAI：

```yaml
# application.yml
llm:
  openai:
    api-key: your-api-key
    model: gpt-4
```

## 默认访问地址

- 前端控制台：`http://localhost:5173`
- 后端接口：`http://localhost:8080/api`
- Agent 本地面板：`http://localhost:8089`

## 核心功能

### 1. Multi-Agent系统

基于LangGraph4j的多Agent协作系统，包含：
- **Planner Agent**：制定执行计划
- **Analyzer Agent**：分析问题根因
- **Executor Agent**：执行运维动作
- **Report Agent**：生成执行报告

### 2. LLM集成

- 支持Ollama本地模型
- 支持OpenAI API
- Provider管理（CRUD）
- 连接测试
- 模型列表获取

### 3. MCP协议

支持MCP (Model Context Protocol)：
- SSE实时推送
- stdio传输
- 外部工具集成（如钉钉、GitHub）

### 4. Tool系统

PTC (Programmatic Tool Calling) 模式：
- Script Tool：脚本执行
- ELK Tool：日志查询
- Milvus Tool：知识库搜索
- Agent Tool：Agent通信
- Notify Tool：通知发送

### 5. 传统运维能力

- Agent注册、心跳、指标上报
- 告警评估与知识检索
- WebSocket命令下发
- Agent命令执行结果回传

## API接口

### LLM接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/llm/providers | Provider列表 |
| POST | /api/llm/providers | 添加Provider |
| POST | /api/llm/providers/{id}/test | 测试连接 |
| POST | /api/llm/chat | 对话 |
| POST | /api/llm/embed | 向量化 |

### Agent接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/agents | Agent列表 |
| POST | /api/agents/chat | 发起对话 |
| GET | /api/agents/chat/stream | SSE流式对话 |
| GET | /api/agents/tools | 可用工具列表 |

### MCP接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/mcp/sse | MCP SSE端点 |
| POST | /api/mcp/tools/call | 调用工具 |
| GET | /api/mcp/stdio | MCP stdio清单 |

## 技术栈

- 后端：Spring Boot 3.3, Java 17
- 多Agent框架：LangGraph4j 1.8.7
- LLM框架：LangChain4j 0.35.0
- 前端：React 18, TypeScript, Ant Design
- Agent：Go, WebSocket
- 数据库：JPA (可扩展至MySQL/PostgreSQL)

## 推荐阅读顺序

1. 先看 `aiops-backend/README.md`
2. 再看 `aiops-frontend/README.md`
3. 最后看 `aiops-agent/README.md`
4. 联调时优先参考三个目录下的 `INTEGRATION.md`

## 说明

当前版本是 MVP，以跑通完整链路和便于开源演示为目标：

- 后端部分能力仍为内存实现
- 前端已具备演示所需交互状态
- Agent 在 Linux 下优先采集真实系统指标，非 Linux 回退模拟采集
- LLM默认使用Ollama本地模型，需提前启动Ollama服务

后续可以继续演进为生产化版本，例如接入 Redis、Elasticsearch、Milvus、真实 LLM、消息队列和更完整的安装引导流程。
