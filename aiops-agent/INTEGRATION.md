# Agent Integration

这是 `aiops-agent` 的联调文档。

## 关联项目

- 后端项目说明：`../aiops-backend/README.md`
- 后端联调文档：`../aiops-backend/INTEGRATION.md`
- 前端项目说明：`../aiops-frontend/README.md`
- 前端联调文档：`../aiops-frontend/INTEGRATION.md`

## 启动前准备

先设置 bootstrap 环境变量：

```bash
set AIOPS_BOOTSTRAP_TOKEN=aiops-mvp-seed-demo-token
```

首次注册成功后会自动生成本地授权文件：

- Windows: `%USERPROFILE%\\.aiops\\authorization.json`
- Linux: `~/.aiops/authorization.json`

## 启动 Agent

```bash
cd aiops-agent
go mod tidy
go run ./cmd -c config.yaml
```

本地面板：`http://localhost:8089`

## 联调过程

1. Agent 启动后调用 `/api/v1/agent/register`
2. 收到 `agentId` 和服务端签发的 agent token
3. 后续请求统一携带 `Authorization: Bearer <agent-token>`
4. 建立 `ws://localhost:8080/ws/agent/{agentId}`
5. 周期上报 `/api/v1/agent/report`
6. 收到后端下发命令
7. 执行命令并回传 `/api/v1/agent/command/result`

## 本地验证点

1. 查看本地面板中的连接状态
2. 查看最近事件是否持续增长
3. 查看待重试上报是否为 0
4. 配合前端确认命令执行结果是否展示
