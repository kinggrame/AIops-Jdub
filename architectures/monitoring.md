# 监控指标模块

> 模块：aiops-monitoring
> 版本：V1.0

---

## 一、架构概述

### 1.1 设计目标

本模块负责**运维对象的指标采集、存储和管理**，是自动化运维的基础数据来源。

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                      监控指标架构                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    数据采集层                                │   │
│   │                                                             │   │
│   │   Agent ──指标──▶ 后端 ──▶ 指标存储                         │   │
│   │   (采集)      (上报)      (时序库/内存)                      │   │
│   │                                                             │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    数据服务层                                │   │
│   │                                                             │   │
│   │   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│   │   │ 指标配置服务  │  │ 指标查询服务  │  │ 指标聚合服务  │    │   │
│   │   │ MetricConfig │  │ MetricQuery  │  │ MetricAgg   │    │   │
│   │   └──────────────┘  └──────────────┘  └──────────────┘    │   │
│   │                                                             │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、数据模型

### 2.1 TargetServer (运维对象)

```java
@Entity
@Table(name = "target_servers")
public class TargetServer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;              // "Web服务器1"
    private String description;      // "Nginx + Tomcat"
    private String endpoint;         // "http://192.168.1.100:8089"
    
    private String groupName;        // 分组: "Web服务组"
    private String tags;             // 标签: "生产,核心"
    
    @Enumerated(EnumType.STRING)
    private ServerStatus status;     // ONLINE / OFFLINE / MAINTENANCE
    
    // 关联Agent
    private String agentId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum ServerStatus {
    ONLINE,        // 在线
    OFFLINE,       // 离线
    MAINTENANCE    // 维护中
}
```

### 2.2 MetricConfig (指标配置)

```java
@Entity
@Table(name = "metric_configs")
public class MetricConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long serverId;           // 关联服务器
    
    private String metricName;       // "cpu.usage"
    private String metricType;       // "gauge", "counter"
    private String unit;             // "%"
    private Boolean enabled;         // 是否启用
    
    private String description;      // "CPU使用率"
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 2.3 MetricData (指标数据)

```java
@Entity
@Table(name = "metric_data")
public class MetricData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long serverId;           // 服务器ID
    private String metricName;      // "cpu.usage"
    private Double value;            // 95.5
    private String unit;             // "%"
    
    private LocalDateTime timestamp; // 时间戳
    
    // 标签
    private String tags;            // JSON: {"host": "web01"}
}
```

### 2.4 内置指标

| 指标名 | 类型 | 说明 | 单位 |
|--------|------|------|------|
| cpu.usage | gauge | CPU使用率 | % |
| cpu.load.1m | gauge | 1分钟负载 | - |
| cpu.load.5m | gauge | 5分钟负载 | - |
| cpu.load.15m | gauge | 15分钟负载 | - |
| memory.usage | gauge | 内存使用率 | % |
| memory.used | gauge | 已用内存 | MB |
| memory.total | gauge | 总内存 | MB |
| disk.usage | gauge | 磁盘使用率 | % |
| disk.free | gauge | 可用磁盘 | GB |
| network.rx | counter | 网络接收 | bytes |
| network.tx | counter | 网络发送 | bytes |
| process.count | gauge | 进程数 | 个 |
| disk.io.read | counter | 磁盘读 | bytes |
| disk.io.write | counter | 磁盘写 | bytes |

---

## 三、API 设计

### 3.1 运维对象 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/servers | 服务器列表 |
| POST | /api/v1/servers | 添加服务器 |
| GET | /api/v1/servers/{id} | 服务器详情 |
| PUT | /api/v1/servers/{id} | 更新服务器 |
| DELETE | /api/v1/servers/{id} | 删除服务器 |
| GET | /api/v1/servers/{id}/metrics | 服务器指标 |
| GET | /api/v1/servers/{id}/metrics/history | 历史指标 |

### 3.2 指标配置 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/metrics/config | 指标配置列表 |
| PUT | /api/v1/metrics/config | 更新配置 |

### 3.3 指标上报 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/v1/metrics/report | Agent上报指标 |

---

## 四、指标上报流程

### 4.1 Agent 上报

```json
// POST /api/v1/metrics/report
{
  "agentId": "server-web-01",
  "metrics": {
    "cpu.usage": 95.5,
    "cpu.load.1m": 8.5,
    "memory.usage": 82.3,
    "disk.usage": 45.0
  },
  "timestamp": "2025-03-09T10:00:00Z"
}
```

### 4.2 后端处理

```java
@Service
public class MetricReportService {
    
    public void report(MetricReportRequest request) {
        for (Map.Entry<String, Double> entry : request.getMetrics().entrySet()) {
            MetricData data = new MetricData();
            data.setServerId(request.getServerId());
            data.setMetricName(entry.getKey());
            data.setValue(entry.getValue());
            data.setTimestamp(request.getTimestamp());
            
            // 存储
            repository.save(data);
            
            // 触发告警检查
            alertEngine.check(request.getServerId(), entry.getKey(), entry.getValue());
        }
    }
}
```

---

## 五、指标查询

### 5.1 实时指标

```java
// GET /api/v1/servers/{id}/metrics
{
  "cpu": {
    "usage": 95.5,
    "load_1m": 8.5
  },
  "memory": {
    "usage": 82.3,
    "used": 6552,
    "total": 8192
  },
  "disk": {
    "usage": 45.0,
    "free": 550
  }
}
```

### 5.2 历史指标

```java
// GET /api/v1/servers/{id}/metrics/history
// 参数: metric=cpu.usage&start=2025-03-09T10:00:00Z&end=2025-03-09T11:00:00Z

{
  "metric": "cpu.usage",
  "data": [
    {"timestamp": "2025-03-09T10:00:00Z", "value": 95.5},
    {"timestamp": "2025-03-09T10:01:00Z", "value": 94.2},
    {"timestamp": "2025-03-09T10:02:00Z", "value": 93.8}
  ]
}
```

---

## 六、实施计划

| 序号 | 工作内容 | 优先级 |
|------|----------|--------|
| 1 | TargetServer 实体 + CRUD | P0 |
| 2 | MetricConfig 实体 + 配置 | P0 |
| 3 | MetricData 实体 + 存储 | P0 |
| 4 | 指标上报接口 | P0 |
| 5 | 指标查询接口 | P0 |
| 6 | 前端服务器管理页面 | P1 |

---

> 模块版本：V1.0
> 最后更新：2025-03-09
