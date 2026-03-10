# AIOps Agent MVP

Go 版轻量 Agent 客户端，用于对接 `aiops-backend`。

联调说明请查看 `INTEGRATION.md`。

## 当前能力

- 读取 `config.yaml`
- 优先读取用户目录下的 `.aiops/authorization.json`
- 启动时注册到后端 `/api/v1/agent/register`
- 周期采集模拟 CPU / 内存 / 磁盘 / 负载 / 网络 / 进程指标
- 根据触发规则生成事件
- 上报到 `/api/v1/agent/report`
- 本地缓存失败上报记录
- 提供本地 Web UI：`http://localhost:8089`
- 使用服务端签发 token 访问 HTTP 和 WebSocket

## 运行前准备

设置 bootstrap 环境变量：

```bash
set AIOPS_BOOTSTRAP_TOKEN=aiops-mvp-seed-demo-token
```

首次注册成功后，agent 会把服务端签发的身份信息保存到：

- Windows: `%USERPROFILE%\\.aiops\\authorization.json`
- Linux: `~/.aiops/authorization.json`

后续重启会优先读取这个文件。

确保后端已启动：

```bash
cd ../aiops-backend
cd aiops-web
mvn spring-boot:run
```

## 运行 Agent

```bash
go mod tidy
go run ./cmd -c config.yaml
```

或：

```bash
make tidy
make run
```

## 构建

```bash
go build -o bin/aiops-agent ./cmd
```

## Web UI

启动后访问：

```text
http://localhost:8089
```

可查看：

- 连接状态
- Agent ID
- 实时指标
- 触发规则
- 命令白名单
- 最近事件
- 待重试上报数量

## 目录结构

```text
aiops-agent/
├── cmd/main.go
├── internal/collector
├── internal/trigger
├── internal/sender
├── internal/executor
├── internal/config
├── internal/webui
├── internal/cache
├── internal/security
├── config.yaml
└── README.md
```

## 说明

当前是 MVP 联调版本：

- 指标采集为轻量模拟实现，便于跨平台运行
- Linux 下优先采集真实系统指标，非 Linux 自动回退到模拟采集
- 已支持连接后端真实 `/ws/agent/{agentId}` WebSocket 命令通道
- 收到命令后会执行白名单校验，并把结果回传到 `/api/v1/agent/command/result`

## 完整联调链路

1. 启动 `aiops-backend`
2. 启动 `aiops-frontend`
3. 设置 `AIOPS_BOOTSTRAP_TOKEN=aiops-mvp-seed-demo-token`
4. 启动本 Agent
5. Agent 首次注册成功后获取服务端签发的 agent token，并自动发起 WebSocket 连接
6. Agent 周期上报真实指标和 trigger 事件
7. 后端在 critical 场景下下发命令
8. Agent 执行后回传命令结果
9. 前端仪表盘与告警页展示完整结果
