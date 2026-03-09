# AIOps Frontend MVP

这是智能运维平台的前端控制台，基于 React + TypeScript + Vite + Ant Design。

联调说明请查看 `INTEGRATION.md`。

## 当前能力

- 仪表盘总览
- 客户端管理
- Agent 对话
- 告警管理
- 知识检索
- 系统设置

## 启动方式

```bash
cd aiops-frontend
npm install
npm run dev
```

默认访问：`http://localhost:5173`

客户端管理页现在会直接展示：

- tunnel 状态
- tunnel 识别到的 publicUrl
- Windows/Linux agent 启动命令（可直接复制）

## 关联项目

- 后端项目说明：`../aiops-backend/README.md`
- 后端联调文档：`../aiops-backend/INTEGRATION.md`
- Agent 项目说明：`../aiops-agent/README.md`
- Agent 联调文档：`../aiops-agent/INTEGRATION.md`

## 构建

```bash
npm run build
```
