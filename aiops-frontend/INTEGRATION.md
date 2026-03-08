# Frontend Integration

这是 `aiops-frontend` 的联调文档。

## 关联项目

- 后端项目说明：`../aiops-backend/README.md`
- 后端联调文档：`../aiops-backend/INTEGRATION.md`
- Agent 项目说明：`../aiops-agent/README.md`
- Agent 联调文档：`../aiops-agent/INTEGRATION.md`

## 启动前端

```bash
cd aiops-frontend
npm install
npm run dev
```

访问地址：`http://localhost:5173`

## 联调前置

1. 后端已启动：`http://localhost:8080`
2. Agent 已启动并完成注册

## 演示建议路径

1. 打开仪表盘，观察是否有在线 Agent
2. 打开客户端管理页，确认 Agent 已注册
3. 触发一次模拟高负载上报
4. 打开告警页查看 critical / warning 告警
5. 打开 Agent 对话页发起分析请求
6. 打开知识检索页验证 RAG 返回

## 关键页面

- `/`
- `/agents`
- `/agent`
- `/alerts`
- `/knowledge`
- `/settings`
