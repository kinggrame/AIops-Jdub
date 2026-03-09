# AIOps Platform MVP

一个面向开源演示和后续扩展的智能运维平台 MVP，包含后端、前端控制台和轻量 Agent 客户端三部分。

## 项目结构

- `aiops-backend`：Spring Boot 后端，负责 Agent 注册、指标上报、告警分析、知识检索、命令下发
- `aiops-frontend`：React + TypeScript 前端控制台，负责运维可视化、Agent 管理、告警查看和智能对话
- `aiops-agent`：Go 编写的轻量客户端，负责指标采集、规则触发、命令执行和本地 Web 面板

## 文档入口

### 项目说明

- 后端：`aiops-backend/README.md`
- 前端：`aiops-frontend/README.md`
- Agent：`aiops-agent/README.md`

### 联调文档

- 后端联调：`aiops-backend/INTEGRATION.md`
- 前端联调：`aiops-frontend/INTEGRATION.md`
- Agent 联调：`aiops-agent/INTEGRATION.md`

## 快速启动

### 1. 启动后端

```bash
cd aiops-backend
mvn spring-boot:run -pl aiops-web -am
```

Windows 下一键启动前后端：

```powershell
./start-demo.ps1
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

如果 backend 只运行在本机，而 agent 跑在公网服务器上，则还需要给 backend 提供一个公网可达地址。当前 MVP 已支持在 backend 生命周期内临时托管内网穿透，但默认关闭，且仅建议演示时开启。

## 默认访问地址

- 前端控制台：`http://localhost:5173`
- 后端接口：`http://localhost:8080/api/v1`
- Agent 本地面板：`http://localhost:8089`
- Agent WebSocket：`ws://localhost:8080/ws/agent/{agentId}`

## 当前能力

- Agent 注册、心跳、指标上报
- 告警评估与知识检索
- Agent 对话分析
- WebSocket 命令下发
- Agent 命令执行结果回传
- 前端可视化展示与联调演示

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

后续可以继续演进为生产化版本，例如接入 Redis、Elasticsearch、Milvus、真实 LLM、消息队列和更完整的安装引导流程。
