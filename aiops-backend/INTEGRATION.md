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

如需临时开启内网穿透，可显式设置：

```bash
set AIOPS_TUNNEL_ENABLED=true
set AIOPS_TUNNEL_COMMAND=cloudflared
set AIOPS_TUNNEL_ARGUMENTS=tunnel --url http://127.0.0.1:8080
mvn spring-boot:run -pl aiops-web -am
```

注意：

- tunnel 默认关闭
- 只建议转发 `127.0.0.1:8080`
- backend 关闭后，托管的 tunnel 进程也会被停止
- 如果需要固定公网地址，优先使用带固定域名的 tunnel 服务

## Redis（推荐）

为了启用 `Caffeine + Redis` 双层缓存，建议先启动 Redis：

```bash
docker run -d --name aiops-redis -p 6379:6379 redis:7
```

如果 Redis 未启动，后端仍可运行，但会自动回退为本地缓存。

## 提供能力

- HTTP API：`http://localhost:8080/api/v1`
- WebSocket：`ws://localhost:8080/ws/agent/{agentId}`
- Tunnel 状态：`GET /api/v1/metrics/tunnel`

## 联调顺序

1. 启动后端
2. 启动前端
3. 设置 Agent bootstrap token
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
5. 如已启动 Redis，观察后端日志中不再出现 Redis fallback warning
6. 如已开启 tunnel，访问 `/api/v1/metrics/tunnel` 确认 `publicUrl` 已被识别
