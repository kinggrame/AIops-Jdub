# 告警规则模块

> 模块：aiops-alert
> 版本：V1.0

---

## 一、架构概述

### 1.1 设计目标

本模块负责**告警规则管理、告警生成、告警处理**，是自动化运维的触发器。

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                      告警规则架构                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    规则引擎                                   │   │
│   │                                                             │   │
│   │   指标数据 ──▶ 规则匹配 ──▶ 告警生成 ──▶ 动作执行           │   │
│   │                                                             │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    告警生命周期                              │   │
│   │                                                             │   │
│   │   Firing ──▶ 确认 ──▶ 处理中 ──▶ 已解决                     │   │
│   │                                                             │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、数据模型

### 2.1 AlertRule (告警规则)

```java
@Entity
@Table(name = "alert_rules")
public class AlertRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;              // "CPU过高"
    private String description;      // "CPU使用率超过90%"
    
    private String metricName;       // "cpu.usage"
    private String operator;         // ">", ">=", "<", "==", "!="
    private Double threshold;        // 90
    
    private Integer duration;        // 持续时间(秒), 0=立即触发
    private Integer evalInterval;    // 评估间隔(秒), 默认30
    
    @Enumerated(EnumType.STRING)
    private AlertSeverity severity;  // CRITICAL / WARNING / INFO
    
    @Enumerated(EnumType.STRING)
    private AlertRuleStatus status;  // ENABLED / DISABLED
    
    // 关联范围
    private Long serverId;           // 关联服务器(可空=全局)
    private String serverGroup;      // 或关联分组
    
    // 动作
    @OneToMany(mappedBy = "rule")
    private List<Action> actions;    // 触发的动作
    
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum AlertSeverity {
    CRITICAL,   // 严重 - 立即触发动作
    WARNING,    // 警告 - 可选触发
    INFO        // 信息 - 记录
}

public enum AlertRuleStatus {
    ENABLED,
    DISABLED
}
```

### 2.2 Alert (告警实例)

```java
@Entity
@Table(name = "alerts")
public class Alert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long ruleId;             // 关联规则ID
    private String ruleName;         // 规则名称
    
    private Long serverId;           // 关联服务器
    private String serverName;       // 服务器名称
    
    private String metricName;       // "cpu.usage"
    private Double value;            // 95.5
    private Double threshold;        // 90
    
    @Enumerated(EnumType.STRING)
    private AlertSeverity severity;  // CRITICAL / WARNING / INFO
    
    private String message;         // "CPU使用率95.5%超过阈值90%"
    
    @Enumerated(EnumType.STRING)
    private AlertStatus status;     // FIRING / ACKNOWLEDGED / RESOLVED
    
    private String acknowledgedBy;   // 确认人
    private LocalDateTime acknowledgedAt;
    
    private String resolvedBy;       // 解决人
    private LocalDateTime resolvedAt;
    private String resolution;       // 解决方法
    
    private LocalDateTime firedAt;  // 触发时间
    private LocalDateTime createdAt;
}

public enum AlertStatus {
    FIRING,             // 触发中
    ACKNOWLEDGED,      // 已确认
    RESOLVED           // 已解决
}
```

### 2.3 AlertHistory (告警历史)

```java
@Entity
@Table(name = "alert_history")
public class AlertHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long alertId;           // 原始告警ID
    private Long serverId;
    private String metricName;
    private Double value;
    private Double threshold;
    private String action;         // "triggered", "resolved"
    private String result;         // 执行结果
    
    private LocalDateTime timestamp;
}
```

---

## 三、规则引擎

### 3.1 引擎流程

```
指标数据输入
      │
      ▼
┌─────────────┐
│ 遍历规则    │── No ──▶ 结束
└──────┬──────┘
       │ Yes
       ▼
┌─────────────┐
│ 条件匹配?   │── No ──▶ 下一规则
└──────┬──────┘
       │ Yes
       ▼
┌─────────────┐
│ 持续时间    │── No ──▶ 等待持续
│ 满足?      │
└──────┬──────┘
       │ Yes
       ▼
┌─────────────┐
│ 生成告警    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ 触发动作    │── 脚本执行/通知/审批
└─────────────┘
```

### 3.2 引擎实现

```java
@Service
public class AlertRuleEngine {
    
    public void check(Long serverId, String metricName, Double value) {
        // 获取适用的规则
        List<AlertRule> rules = ruleRepository.findActiveRules(metricName, serverId);
        
        for (AlertRule rule : rules) {
            // 条件匹配
            if (!evaluate(rule, value)) {
                continue;
            }
            
            // 持续时间检查
            if (!checkDuration(rule, serverId, metricName)) {
                continue;
            }
            
            // 触发告警
            triggerAlert(rule, serverId, metricName, value);
        }
    }
    
    private boolean evaluate(AlertRule rule, Double value) {
        return switch (rule.getOperator()) {
            case ">" -> value > rule.getThreshold();
            case ">=" -> value >= rule.getThreshold();
            case "<" -> value < rule.getThreshold();
            case "<=" -> value <= rule.getThreshold();
            case "==" -> Math.abs(value - rule.getThreshold()) < 0.01;
            case "!=" -> Math.abs(value - rule.getThreshold()) >= 0.01;
            default -> false;
        };
    }
}
```

---

## 四、API 设计

### 4.1 告警规则 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/alert/rules | 规则列表 |
| POST | /api/v1/alert/rules | 创建规则 |
| GET | /api/v1/alert/rules/{id} | 规则详情 |
| PUT | /api/v1/alert/rules/{id} | 更新规则 |
| DELETE | /api/v1/alert/rules/{id} | 删除规则 |
| POST | /api/v1/alert/rules/{id}/enable | 启用规则 |
| POST | /api/v1/alert/rules/{id}/disable | 禁用规则 |

### 4.2 告警 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/alerts | 告警列表 |
| GET | /api/v1/alerts/{id} | 告警详情 |
| POST | /api/v1/alerts/{id}/ack | 确认告警 |
| POST | /api/v1/alerts/{id}/resolve | 解决告警 |

### 4.3 告警历史 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/alert/history | 告警历史 |

---

## 五、告警生命周期

```
┌─────────────────────────────────────────────────────────────────────┐
│                       告警生命周期                                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌──────────┐      确认       ┌───────────────┐    解决      ┌──────────┐ │
│   │  FIRING  │ ──────────────▶ │ ACKNOWLEDGED  │ ──────────▶ │ RESOLVED │ │
│   │ (触发中)  │                │ (已确认)       │             │ (已解决)  │ │
│   └──────────┘                └───────────────┘             └──────────┘ │
│                                                                     │
│   触发条件满足 ──▶ 自动生成 ──▶ 用户确认 ──▶ 处理完成 ──▶ 标记解决   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 六、实施计划

| 序号 | 工作内容 | 优先级 |
|------|----------|--------|
| 1 | AlertRule 实体 + CRUD | P0 |
| 2 | Alert 实体 + 生命周期 | P0 |
| 3 | 规则引擎 | P0 |
| 4 | 告警历史 | P1 |
| 5 | 前端告警规则页面 | P1 |
| 6 | 前端告警列表页面 | P1 |

---

> 模块版本：V1.0
> 最后更新：2025-03-09
