# 智能运维平台 MVP - 前端搭建计划

## 一、项目概述

搭建智能运维（AIOps）平台的前端应用，采用 React + TypeScript + Vite + Ant Design。

### 技术选型

| 层级 | 技术选型 |
|------|----------|
| 框架 | React 18 |
| 构建工具 | Vite |
| 语言 | TypeScript |
| UI 组件库 | Ant Design 5 |
| 路由 | React Router v6 |
| 状态管理 | React Query / Zustand |
| HTTP 客户端 | Axios |
| 图表 | @ant-design/charts |

---

## 二、项目结构

```
aiops-web/
├── public/                          # 静态资源
│   └── favicon.ico
│
├── src/
│   ├── api/                        # API 请求
│   │   ├── index.ts                # Axios 实例配置
│   │   ├── types.ts                # 类型定义
│   │   └── aiops.ts                # 业务 API
│   │
│   ├── assets/                     # 静态资源
│   │   └── styles/
│   │       └── global.css          # 全局样式
│   │
│   ├── components/                 # 公共组件
│   │   ├── Layout/                 # 布局组件
│   │   │   ├── index.tsx
│   │   │   ├── Header.tsx
│   │   │   ├── Sider.tsx
│   │   │   └── Content.tsx
│   │   └── Common/                 # 通用组件
│   │       ├── Loading.tsx
│   │       └── ErrorBoundary.tsx
│   │
│   ├── pages/                      # 页面组件
│   │   ├── Login/                  # 登录页
│   │   │   └── index.tsx
│   │   ├── Dashboard/              # 仪表盘
│   │   │   └── index.tsx
│   │   ├── AgentChat/              # Agent 对话
│   │   │   └── index.tsx
│   │   ├── Knowledge/              # 知识库管理
│   │   │   └── index.tsx
│   │   └── Settings/               # 系统设置
│   │       └── index.tsx
│   │
│   ├── hooks/                      # 自定义 Hooks
│   │   ├── useAuth.ts
│   │   └── useChat.ts
│   │
│   ├── store/                      # 状态管理
│   │   └── index.ts
│   │
│   ├── utils/                      # 工具函数
│   │   ├── storage.ts              # 本地存储
│   │   └── format.ts               # 格式化工具
│   │
│   ├── App.tsx                     # 根组件
│   ├── main.tsx                    # 入口文件
│   └── vite-env.d.ts               # Vite 类型声明
│
├── .env                            # 环境变量
├── .env.development                # 开发环境配置
├── .env.production                 # 生产环境配置
│
├── index.html                      # HTML 模板
├── package.json
├── tsconfig.json
├── tsconfig.node.json
└── vite.config.ts                  # Vite 配置
```

---

## 三、页面路由

| 路径 | 页面 | 权限 | 说明 |
|------|------|------|------|
| `/login` | 登录页 | 公开 | 用户登录 |
| `/` | 首页/仪表盘 | 需登录 | 系统概览 |
| `/agents` | 客户端管理 | 需登录 | 管理已连接的 Agent |
| `/agent` | Agent 对话 | 需登录 | AI 运维对话 |
| `/alerts` | 告警管理 | 需登录 | 查看告警、配置规则 |
| `/knowledge` | 知识库 | 需登录 | RAG 知识管理 |
| `/settings` | 系统设置 | 需登录 | 配置管理 |

---

## 四、MVP 页面功能

### 1. 登录页 (/login)

- [x] 用户名/密码输入
- [x] 登录按钮
- [x] 登录失败提示

### 2. 仪表盘 (/dashboard)

- [x] 系统指标卡片（CPU、内存、磁盘）
- [x] 告警统计图表
- [x] 最近任务列表
- [x] 在线 Agent 数量

### 3. 客户端管理 (/agents)

- [x] 已连接 Agent 列表
- [x] Agent 状态（在线/离线）
- [x] Agent 基本信息（IP、主机名）
- [x] 下发命令（重启服务等）
- [x] 查看 Agent 采集的实时指标
- [ ] 查看 Agent 日志监听配置
- [ ] 查看 Agent 信任的服务端地址

### 4. Agent 对话 (/agent)

- [x] 对话输入框
- [x] Agent 类型选择（Data/Analysis/Report）
- [x] 消息列表展示
- [x] 发送/接收消息
- [x] 关联目标服务器选择
- [ ] 展示 Summarize / Plan / Execute / Report 多 Agent 阶段结果
- [ ] ExecuteAgent 命令审批卡片
- [ ] 审批通过后查看命令执行结果

### 5. 告警管理 (/alerts)

- [x] 告警列表展示
- [x] 告警详情查看
- [x] 告警状态处理（确认/解决）
- [x] 告警规则配置
- [x] 告警通知渠道配置
- [ ] 关联日志摘要查看
- [ ] 关联执行建议和审批记录查看

### 6. 知识库管理 (/knowledge)

- [x] 知识列表展示
- [x] 搜索功能
- [x] 新增知识（基础）
- [x] 知识分类管理

### 7. 系统设置 (/settings)

- [x] 用户管理
- [x] LLM 配置（API Key 选择）
- [x] 告警阈值配置
- [x] 命令白名单配置

---

## 五、API 接口定义

```typescript
// 请求/响应类型
interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

// Agent 对话
interface ChatRequest {
  message: string;
  agentType: 'SUMMARIZE' | 'PLAN' | 'EXECUTE' | 'REPORT';
}

interface ChatResponse {
  message: string;
  timestamp: number;
  stages?: Array<{
    agent: 'SUMMARIZE' | 'PLAN' | 'EXECUTE' | 'REPORT';
    output: string;
  }>;
}

interface ApprovalRequest {
  commandId: string;
  command: string;
  targetAgentId: string;
  approved: boolean;
  reviewer: string;
}

// 知识检索
interface SearchRequest {
  query: string;
  topK?: number;
}

interface SearchResponse {
  results: Array<{
    content: string;
    score: number;
  }>;
}
```

---

## 六、环境变量

```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8080/api

# .env.production
VITE_API_BASE_URL=/api
```

---

## 七、组件规范

### 1. 页面组件模板

```tsx
/**
 * 页面名称
 *
 * <p>页面功能描述</p>
 */
import { useState } from 'react';
import { Card } from 'antd';

export default function PageName() {
  const [loading, setLoading] = useState(false);

  return (
    <Card>
      {/* 页面内容 */}
    </Card>
  );
}
```

### 2. API 调用规范

```typescript
import axios from './index';

/**
 * 获取 Agent 对话
 */
export const getAgentChat = async (params: ChatRequest) => {
  const response = await axios.post<ApiResponse<ChatResponse>>(
    '/agent/chat',
    params
  );
  return response.data;
};
```

---

## 八、启动命令

```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview
```
