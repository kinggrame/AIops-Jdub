# Backend Integration

这是 `aiops-backend` 的联调文档。

## 关联项目

- 前端项目说明：`../aiops-frontend/README.md`
- 前端联调文档：`../aiops-frontend/INTEGRATION.md`
- Agent 项目说明：`../aiops-agent/README.md`
- Agent 联调文档：`../aiops-agent/INTEGRATION.md`

## 启动后端

```bash
cd aiops-backend
mvn spring-boot:run -pl aiops-web -am
```

## 提供能力

- HTTP API：`http://localhost:8080/api/v1`
- WebSocket：`ws://localhost:8080/ws/agent/{agentId}`

## 联调顺序

1. 启动后端
2. 启动前端
3. 设置 Agent token
4. 启动 Agent
5. Agent 注册并建立 WebSocket
6. Agent 周期上报指标
7. 后端生成告警、分析和命令
8. Agent 执行命令并回传结果

## 关键接口

- `POST /api/v1/agent/register`
- `POST /api/v1/agent/report`
- `POST /api/v1/agent/command/result`
- `POST /api/v1/agent/chat`
- `GET /api/v1/agent/clients`
- `GET /api/v1/agent/command/results`
- `GET /api/v1/agent/command/pending`

## 验证建议

1. 打开前端客户端管理页，确认 Agent 出现
2. 打开告警页，确认高负载告警出现
3. 打开 Agent 本地面板，确认最近事件更新
4. 查看命令结果页或仪表盘，确认命令执行结果回传
