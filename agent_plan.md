# AIOps Agent 客户端程序规划

## 一、设计目标

| 目标 | 说明 |
|------|------|
| **低内存占用** | < 50MB 内存，适合低配置服务器 |
| **低资源消耗** | CPU 占用 < 1%，适合长期运行 |
| **简单部署** | 单一二进制文件，无依赖 |
| **高可靠性** | 异常自动重连，断点续传 |

## 二、技术选型

| 语言 | 内存占用 | 打包大小 | 学习成本 | 推荐指数 |
|------|----------|----------|----------|----------|
| **Go** | ~30MB | ~15MB | 中 | ⭐⭐⭐⭐⭐ |
| Rust | ~20MB | ~5MB | 高 | ⭐⭐⭐⭐ |
| Node.js | ~100MB | ~100MB | 低 | ⭐⭐⭐ |

**选择：Go** - 平衡了性能和开发效率

## 三、架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                        AIOps Agent                           │
│                         (Go)                                 │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐      │
│  │  Collector  │───▶│   Trigger   │───▶│   Sender   │      │
│  │  (采集器)    │    │  (触发器)   │    │  (发送器)   │      │
│  └─────────────┘    └─────────────┘    └─────────────┘      │
│         │                  │                                   │
│         ▼                  ▼                                   │
│  ┌─────────────────────────────────────────────────────┐     │
│  │              Config Manager (配置管理)               │     │
│  └─────────────────────────────────────────────────────┘     │
│         │                  │                                   │
│         ▼                  ▼                                   │
│  ┌─────────────┐    ┌─────────────┐                         │
│  │  Metrics   │    │   Logger    │                         │
│  │  (本地缓存) │    │  (日志)     │                         │
│  └─────────────┘    └─────────────┘                         │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## 四、核心模块

### 1. Collector（指标采集）

| 指标 | 采集方式 | 频率 |
|------|----------|------|
| CPU | /proc/stat | 10s |
| 内存 | /proc/meminfo | 10s |
| 磁盘 | df -h | 30s |
| 负载 | /proc/loadavg | 10s |
| 进程数 | /proc | 30s |
| 网络 | /proc/net/dev | 30s |

### 2. Trigger（触发器）

```go
// 触发规则
type Trigger struct {
    Name     string  `yaml:"name"`
    Metric   string  `yaml:"metric"`
    Operator string  `yaml:"operator"` // >, <, ==, !=
    Value    float64 `yaml:"value"`
    Target   string  `yaml:"target"`   // "ai" | "server"
}
```

### 3. Sender（发送器）

| 发送方式 | 说明 |
|----------|------|
| HTTP POST | 基础上报，定时发送 |
| WebSocket | 实时命令接收 |
| 断点续传 | 网络中断时本地缓存 |

### 4. Config Manager（配置管理）

```yaml
# config.yaml
server:
  url: "https://aiops.example.com"
  token: "${AIOPS_TOKEN}"  # 从环境变量读取

webui:
  enable: true             # 是否启用 Web 配置页面
  port: 8089              # Web UI 端口

collection:
  interval: 30  # 秒

triggers:
  - name: cpu_high
    metric: cpu.usage
    operator: ">"
    value: 90
    target: ai
  
  - name: memory_high
    metric: memory.usage
    operator: ">"
    value: 85
    target: ai
    
commands:
  allowed:
    - restart_service
    - get_logs
    - clear_cache
```

### 5. Web UI 页面设计

```
┌─────────────────────────────────────────────────────────┐
│              AIOps Agent - 配置页面                      │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌─────────────────────────────────────────────────┐   │
│  │  状态: ✅ 已连接                                  │   │
│  │  服务器: https://aiops.example.com                │   │
│  │  运行时间: 2小时30分钟                            │   │
│  └─────────────────────────────────────────────────┘   │
│                                                          │
│  ┌─────────────────────────────────────────────────┐   │
│  │  实时指标                                        │   │
│  │  ────────────────────────────────────────────    │   │
│  │  CPU:    45.5%                                  │   │
│  │  内存:   60.2%                                  │   │
│  │  磁盘:   72.8%                                  │   │
│  └─────────────────────────────────────────────────┘   │
│                                                          │
│  ┌─────────────────────────────────────────────────┐   │
│  │  触发规则                                        │   │
│  │  ────────────────────────────────────────────    │   │
│  │  [CPU > 90%] → 发送给AI        [编辑] [删除]   │   │
│  │  [内存 > 85%] → 发送给AI        [编辑] [删除]   │   │
│  │  [+ 添加规则]                                   │   │
│  └─────────────────────────────────────────────────┘   │
│                                                          │
│  ┌─────────────────────────────────────────────────┐   │
│  │  命令白名单                                      │   │
│  │  ────────────────────────────────────────────    │   │
│  │  ✓ restart_service                              │   │
│  │  ✓ get_logs                                    │   │
│  │  ✓ clear_cache                                 │   │
│  │  [+ 添加命令]                                   │   │
│  └─────────────────────────────────────────────────┘   │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

访问方式：`http://<目标服务器IP>:8089/`

访问 `http://localhost:8089/` 查看配置页面。

### 6. 命令执行器

| 命令 | 说明 |
|------|------|
| restart_service | 重启系统服务 |
| stop_service | 停止系统服务 |
| clear_cache | 清理缓存 |
| get_logs | 获取日志 |
| get_process | 查看进程 |

```yaml
# config.yaml
server:
  url: "https://aiops.example.com"
  token: "${AIOPS_TOKEN}"  # 从环境变量读取

collection:
  interval: 30  # 秒

triggers:
  - name: cpu_high
    metric: cpu.usage
    operator: ">"
    value: 90
    target: ai
  
  - name: memory_high
    metric: memory.usage
    operator: ">"
    value: 85
    target: ai
    
commands:
  allowed:
    - restart_service
    - get_logs
    - clear_cache
```

## 五、目录结构

```
aiops-agent/
├── cmd/
│   └── main.go              # 入口
├── internal/
│   ├── collector/           # 指标采集
│   │   ├── cpu.go
│   │   ├── memory.go
│   │   ├── disk.go
│   │   └── network.go
│   ├── trigger/             # 触发引擎
│   │   ├── engine.go
│   │   └── rule.go
│   ├── sender/              # 数据发送
│   │   ├── http.go
│   │   └── websocket.go
│   ├── executor/           # 命令执行
│   │   └── executor.go
│   ├── config/             # 配置管理
│   │   └── loader.go
│   ├── webui/              # 嵌入式 Web 配置页面
│   │   ├── handler.go
│   │   └── static/         # HTML/CSS/JS
│   ├── cache/              # 本地缓存
│   │   └── buffer.go
│   └── security/           # 安全
│       └── token.go
├── config.yaml             # 配置文件
├── go.mod
└── Makefile               # 构建脚本
```

## 六、API 接口

### 客户端 → 服务端

```go
// 上报数据
POST /api/v1/agent/report
Header: Authorization: Bearer {token}
Body: {
    "hostname": "server-001",
    "metrics": {
        "cpu": {"usage": 45.5},
        "memory": {"usage": 60.2}
    },
    "events": [
        {
            "type": "threshold",
            "metric": "cpu.usage",
            "value": 95.0,
            "target": "ai"
        }
    ]
}
```

### 服务端 → 客户端（WebSocket）

```go
// 命令下发
{
    "cmd": "execute",
    "action": "restart_service",
    "params": {"service": "nginx"},
    "id": "cmd-001"
}

// 客户端响应
{
    "cmd_id": "cmd-001",
    "status": "success",
    "output": "service nginx restarted"
}
```

## 七、内存优化

| 优化点 | 说明 |
|--------|------|
| **对象池** | 复用 byte buffer，减少 GC |
| **增量更新** | 只发送变化的数据 |
| **压缩** | gzip 压缩传输数据 |
| **缓存策略** | 本地缓存 + 定时上报 |
| **无运行时** | 静态编译，无 GC 压力 |

## 八、部署方式

### 方式一：直接运行
```bash
./aiops-agent -c config.yaml
```

### 方式二：Systemd 服务
```ini
[Unit]
Description=AIOps Agent
After=network.target

[Service]
Type=simple
ExecStart=/usr/local/bin/aiops-agent -c /etc/aiops-agent/config.yaml
Restart=always

[Install]
WantedBy=multi-user.target
```

### 方式三：Docker
```dockerfile
FROM alpine:3.19
COPY aiops-agent /usr/local/bin/
RUN chmod +x /usr/local/bin/aiops-agent
ENTRYPOINT ["aiops-agent", "-c", "/config/config.yaml"]
```

## 九、安全设计

### Token 认证流程
```
1. 首次启动：从环境变量读取 token
2. 每次请求：Header 携带 token
3. 服务端验证：JWT 验证 + IP 白名单
4. 加密传输：HTTPS (TLS 1.3)
```

### 命令白名单
- 客户端本地存储白名单命令
- 不在白名单的命令直接拒绝
- 危险命令（rm -rf）永久禁止

## 十、监控自身

| 指标 | 说明 |
|------|------|
| agent_uptime | 运行时间 |
| agent_memory | 内存占用 |
| agent_cpu | CPU 占用 |
| report_success | 上报成功率 |
| ws_connected | WebSocket 连接状态 |

---

## 十一、与宝塔对比

| 特性 | 宝塔 | AIOps Agent |
|------|------|-------------|
| 内存占用 | ~500MB | ~30MB |
| CPU 占用 | ~5-10% | <1% |
| 功能 | 面板 | 轻量采集 |
| 依赖 | PHP/Python | 无依赖 |
| 适合场景 | 运维面板 | 低配服务器 |

---

## 十二、开发计划

| 阶段 | 功能 |
|------|------|
| v0.1 | 基础指标采集 + HTTP 上报 |
| v0.2 | 触发规则引擎 |
| v0.3 | WebSocket 命令接收 |
| v0.4 | 本地缓存 + 断点续传 |
| v0.5 | 安全加固 |
| v1.0 | 稳定版发布 |
