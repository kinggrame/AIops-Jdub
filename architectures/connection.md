# Agent 连接/配对层

> 模块：aiops-connection
> 版本：V1.0

---

## 一、架构概述

### 1.1 设计目标

本模块负责 **AIOps后端与目标服务器Agent之间的连接管理**，采用本机主动连接Agent的模式，类似于SSH的反向连接。

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                      连接/配对层架构                                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    本机 (AIOps后端 :8080)                   │   │
│   │                                                             │   │
│   │   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │   │
│   │   │ 连接管理器    │  │ 配对服务      │  │ 认证过滤器    │      │   │
│   │   │ Connection   │  │ Pairing      │  │ AuthFilter  │      │   │
│   │   │ Service     │  │ Service      │  │             │      │   │
│   │   └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │   │
│   │          │                  │                  │              │   │
│   │   ┌──────▼──────────────────▼──────────────────▼───────┐   │   │
│   │   │              HTTP/WebSocket 客户端                      │   │   │
│   │   │                   AgentClient                          │   │   │
│   │   └─────────────────────────┬─────────────────────────────┘   │   │
│   └─────────────────────────────┼─────────────────────────────────┘   │
│                                 │                                     │
│                                 │ WebSocket / HTTP                    │
│                                 │                                     │
│   ┌─────────────────────────────▼─────────────────────────────────┐   │
│   │                  目标服务器 (Agent :8089)                      │   │
│   │                                                             │   │
│   │   ~/.aiops-auth/                                            │   │
│   │   ├── pair_authorization.json    # 首次配对用                │   │
│   │   └── authorization.json         # 后续通信用                │   │
│   │                                                             │   │
│   │   ┌──────────────┐  ┌──────────────┐                       │   │
│   │   │ 配对处理器    │  │ 命令执行器    │                       │   │
│   │   │ PairingHandler│  │ Executor     │                       │   │
│   │   └──────────────┘  └──────────────┘                       │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、配对流程（核心）

### 2.1 首次配对完整流程

```
┌─────────────────────────────────────────────────────────────────────┐
│                      首次配对完整流程                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  【Agent端】                        【后端】                          │
│       │                                  │                          │
│  1. 检查配置                             │                          │
│     ~/.aiops-auth/                       │                          │
│     ├── pair_authorization.json ───存在? │                          │
│     │      │                             │                          │
│     │  Yes │                             │                          │
│     │      └──→ 直接用旧token            │                          │
│     │                                  │                          │
│     │ No                              │                          │
│     │  2. 生成新pairing_token          │                          │
│     │     "ps-abc123xyz"               │                          │
│     │                                  │                          │
│     │  3. 监听连接                      │                          │
│     │     0.0.0.0:8089                 │                          │
│     │                                  │                          │
│     │  4. 显示配对信息                   │                          │
│     │     ════════════════════════      │                          │
│     │     AIOps Agent 配对信息          │                          │
│     │     Token: ps-abc123xyz          │                          │
│     │     地址: http://x.x.x.x:8089      │                          │
│     │     ════════════════════════      │                          │
│     │                                  │                          │
│     │◄────── 用户告知管理员 ──────────│                          │
│     │                                  │                          │
│     │                           5. 管理员添加Agent                  │
│     │                              后端保存:                        │
│     │                              - name: "Web服务器1"             │
│     │                              - endpoint: "http://x.x.x.x:8089"
│     │                              - pairing_token: "ps-abc123xyz" │
│     │                              - status: PENDING               │
│     │                                  │                          │
│     │◄──── 6. 发起配对请求 ──────────│                          │
│     │      (带pairing_token)          │                          │
│     │                                  │                          │
│     │  7. 验证pairing_token           │                          │
│     │     匹配? ── Yes ──→           │                          │
│     │     │                           │                          │
│     │     │                      8. 生成authorization_token       │
│     │     │                           │                          │
│     │     │                      9. 保存到数据库                   │
│     │     │                           │                          │
│     │ 10. 保存到本地                   │                          │
│     │     ~/.aiops-auth/              │                          │
│     │     ├── pair_authorization.json │                          │
│     │     └── authorization.json    │                          │
│     │                                  │                          │
│     │──── 11. 返回配对成功 ──────────►│                          │
│     │      (带authorization_token)   │                          │
│     │                                  │                          │
│  12. 连接建立                           │                          │
│     │                                  │                          │
│     │◄──── 13. 双向通信 ────────────►│                          │
│     │      (指标上报/命令下发/结果回传)  │                          │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 后续通信流程

```
Agent启动                              后端
   │                                    │
   │ 1. 读取authorization.json          │
   │    - authorization_token          │
   │    - server_url                   │
   │                                    │
   │──── 2. 发起连接请求 ─────────►│
   │    (带authorization_token)       │
   │                                    │
   │◄─── 3. 验证token ─────────────────│
   │     有效?                          │
   │                                    │
   │──── 4. 建立WebSocket ───────────►│
   │                                    │
   │◄──── 5. 双向通信 ───────────────►│
   │     (指标上报/命令下发/结果回传)    │
```

---

## 三、数据模型

### 3.1 AgentConnection (连接记录)

```java
@Entity
@Table(name = "agent_connections")
public class AgentConnection {
    
    @Id
    private String id;
    
    private String name;                    // "Web服务器1"
    private String description;             // "Nginx + Tomcat"
    private String endpoint;                // "http://192.168.1.100:8089"
    
    // 分组
    private String groupName;               // "Web服务组"
    private String tags;                   // "生产,核心"
    
    @Enumerated(EnumType.STRING)
    private ConnectionStatus status;        // PENDING / CONNECTED / DISCONNECTED
    
    // 首次配对用
    private String pairingToken;            // "ps-abc123xyz"
    private LocalDateTime pairingAt;
    
    // 后续通信用
    private String authorizationToken;       // "auth-xyz789"
    private LocalDateTime tokenIssuedAt;
    private LocalDateTime tokenExpiresAt;
    
    // 元数据
    private Long approvedBy;               // 审批人ID
    private LocalDateTime approvedAt;
    private LocalDateTime lastConnectedAt;
    private LocalDateTime lastHeartbeatAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum ConnectionStatus {
    PENDING,        // 待配对
    CONNECTED,       // 已连接
    DISCONNECTED,   // 断开
    REJECTED,       // 拒绝
    EXPIRED         // 过期
}
```

---

## 四、API 设计

### 4.1 连接管理 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/connections | 连接列表 |
| POST | /api/v1/connections | 添加Agent |
| GET | /api/v1/connections/{id} | Agent详情 |
| PUT | /api/v1/connections/{id} | 更新Agent |
| DELETE | /api/v1/connections/{id} | 删除Agent |
| POST | /api/v1/connections/{id}/connect | 发起配对 |
| POST | /api/v1/connections/{id}/disconnect | 断开连接 |
| POST | /api/v1/connections/{id}/refresh | 刷新Token |

### 4.2 配对接口 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/v1/pairing/request | Agent发起配对请求 |
| POST | /api/v1/pairing/verify | 验证配对 |

---

## 五、认证过滤器

### 5.1 AgentAuthFilter

```java
@Component
public class AgentAuthFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, 
                         FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpRes = (HttpServletResponse) res;
        
        String path = httpReq.getRequestURI();
        
        // 配对接口不需要认证
        if (path.startsWith("/api/v1/pairing") || 
            path.startsWith("/api/v1/connections") && httpReq.getMethod().equals("POST")) {
            chain.doFilter(req, res);
            return;
        }
        
        // 获取Token
        String token = extractToken(httpReq);
        
        if (token == null) {
            httpRes.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJSON(httpRes, new ErrorResponse("missing token"));
            return;
        }
        
        // 验证Token
        AgentConnection conn = connectionService.findByToken(token);
        if (conn == null) {
            httpRes.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJSON(httpRes, new ErrorResponse("invalid token"));
            return;
        }
        
        // 验证状态
        if (conn.getStatus() != ConnectionStatus.CONNECTED) {
            httpRes.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJSON(httpRes, new ErrorResponse("connection not active"));
            return;
        }
        
        // 验证通过，放行
        req.setAttribute("agentConnection", conn);
        chain.doFilter(req, res);
    }
}
```

---

## 六、Agent 端配置

### 6.1 pair_authorization.json (首次配对)

```json
{
  "version": "1.0",
  "pairing": {
    "token": "ps-abc123xyz789",
    "created_at": "2025-03-09T10:00:00Z",
    "agent_id": "server-web-01",
    "server_url": "http://localhost:8080"
  }
}
```

### 6.2 authorization.json (后续通信)

```json
{
  "version": "1.0",
  "agent": {
    "agent_id": "server-web-01",
    "name": "Web服务器1",
    "description": "Nginx + Tomcat"
  },
  "server": {
    "url": "http://localhost:8080",
    "authorization_token": "auth-xyz789-def456",
    "expires_at": "2026-03-09T10:00:00Z"
  },
  "connection": {
    "status": "connected",
    "last_connected": "2025-03-09T10:05:00Z"
  }
}
```

---

## 七、WebSocket 通信

### 7.1 连接建立

```java
// 后端WebSocket处理器
@Component
public class AgentWebSocketHandler extends TextWebSocketHandler {
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String agentId = extractAgentId(session);
        
        // 验证token
        String token = extractToken(session);
        AgentConnection conn = connectionService.findByToken(token);
        
        if (conn == null) {
            session.close(CloseReason.PolicyViolation("invalid token"));
            return;
        }
        
        // 保存连接
        sessionService.addSession(agentId, session);
        
        log.info("Agent {} connected via WebSocket", agentId);
    }
}
```

### 7.2 消息格式

```java
// 消息类型
public enum MessageType {
    HEARTBEAT,       // 心跳
    METRICS,         // 指标上报
    COMMAND,         // 命令下发
    COMMAND_RESULT,  // 命令结果
    LOG,             // 日志
    ERROR            // 错误
}

// 消息结构
public class AgentMessage {
    private String type;
    private String agentId;
    private Long timestamp;
    private Object payload;
}
```

---

## 八、实施计划

| 序号 | 工作内容 | 优先级 |
|------|----------|--------|
| 1 | AgentConnection实体 + Repository | P0 |
| 2 | 配对Service + 接口 | P0 |
| 3 | HTTP/WebSocket客户端 | P0 |
| 4 | Token认证过滤器 | P0 |
| 5 | 连接管理前端页面 | P0 |

---

> 模块版本：V1.0
> 最后更新：2025-03-09
