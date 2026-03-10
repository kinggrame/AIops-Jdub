# 执行动作模块

> 模块：aiops-action
> 版本：V1.0

---

## 一、架构概述

### 1.1 设计目标

本模块是**自动化运维的核心**，负责执行运维动作，包括脚本执行、Skill执行、通知发送等。

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                      执行动作架构                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   告警触发 ──▶ 动作队列 ──▶ 执行引擎 ──▶ 执行器 ──▶ 结果记录       │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐    │
│   │                      执行引擎                               │    │
│   │                                                             │    │
│   │   ┌─────────────┐                                          │    │
│   │   │ 执行队列    │  ← 并发控制                               │    │
│   │   └──────┬──────┘                                          │    │
│   │          │                                                  │    │
│   │          ▼                                                  │    │
│   │   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │    │
│   │   │ 脚本执行器  │  │ Skill执行器 │  │ 通知发送器  │       │    │
│   │   │ ScriptExec  │  │ SkillExec   │  │ NotifyExec  │       │    │
│   │   └─────────────┘  └─────────────┘  └─────────────┘       │    │
│   │                                                             │    │
│   └─────────────────────────────────────────────────────────────┘    │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、数据模型

### 2.1 Action (动作)

```java
@Entity
@Table(name = "actions")
public class Action {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;              // "重启Nginx"
    private String description;       // "当CPU过高时重启Nginx"
    
    @Enumerated(EnumType.String)
    private ActionType type;          // 类型
    
    private Long ruleId;             // 关联告警规则
    
    // 触发条件
    @Enumerated(EnumType.String)
    private ActionTriggerType triggerType;  // "on_trigger", "on_resolve"
    
    // 执行配置 (JSON)
    private String config;            // {"script_id": 1, "params": {...}}
    
    @Enumerated(EnumType.STRING)
    private ActionStatus status;     // ENABLED / DISABLED
    
    private Integer order;           // 执行顺序
    private Integer timeout;        // 超时时间(秒)
    private Integer retryCount;     // 重试次数
    
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum ActionType {
    EXECUTE_SCRIPT,       // 执行脚本
    EXECUTE_SKILL,        // 执行Skill
    SEND_NOTIFICATION,    // 发送通知
    APPROVAL,            // 人工审批
    WEBHOOK              // 调用Webhook
}

public enum ActionTriggerType {
    ON_TRIGGER,          // 告警触发时
    ON_RESOLVE           // 告警解决时
}

public enum ActionStatus {
    ENABLED,
    DISABLED
}
```

### 2.2 ActionExecution (执行记录)

```java
@Entity
@Table(name = "action_executions")
public class ActionExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long actionId;
    private String actionName;
    
    private Long ruleId;             // 关联告警
    private Long alertId;            // 关联告警实例
    private Long serverId;           // 目标服务器
    
    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;   // PENDING / RUNNING / SUCCESS / FAILED
    
    // 输入
    private String input;            // 输入参数
    
    // 输出
    private String output;           // 执行输出
    private String errorMessage;     // 错误信息
    
    private Integer retryCount;     // 重试次数
    
    private Long executedBy;         // 执行人(system=自动)
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;           // 耗时(毫秒)
    
    // 关联
    private Long nextExecutionId;   // 下一个执行(链式)
}

public enum ExecutionStatus {
    PENDING,      // 待执行
    RUNNING,      // 执行中
    SUCCESS,      // 成功
    FAILED,       // 失败
    CANCELLED,    // 取消
    TIMEOUT       // 超时
}
```

---

## 三、执行引擎

### 3.1 执行器接口

```java
public interface ActionExecutor {
    
    /**
     * 执行动作
     * @param action 动作配置
     * @param context 执行上下文(告警信息、服务器信息等)
     * @return 执行结果
     */
    ExecuteResult execute(Action action, Map<String, Object> context);
    
    /**
     * 验证动作配置
     */
    boolean validate(Action action);
    
    /**
     * 获取执行器类型
     */
    ActionType getType();
}
```

### 3.2 执行服务

```java
@Service
public class ExecutorService {
    
    @Autowired
    private List<ActionExecutor> executors;
    
    public ExecuteResult execute(Action action, Map<String, Object> context) {
        // 1. 获取执行器
        ActionExecutor executor = executors.stream()
            .filter(e -> e.getType() == action.getType())
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No executor for " + action.getType()));
        
        // 2. 验证配置
        if (!executor.validate(action)) {
            return ExecuteResult.fail("Invalid action config");
        }
        
        // 3. 创建执行记录
        ActionExecution execution = createExecution(action, context);
        
        // 4. 执行
        try {
            execution.setStatus(ExecutionStatus.RUNNING);
            execution.setStartTime(LocalDateTime.now());
            repository.save(execution);
            
            // 5. 调用执行器
            ExecuteResult result = executor.execute(action, context);
            
            // 6. 更新结果
            execution.setStatus(result.isSuccess() ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILED);
            execution.setOutput(result.getOutput());
            execution.setErrorMessage(result.getError());
            execution.setEndTime(LocalDateTime.now());
            execution.setDuration(Duration.between(execution.getStartTime(), execution.getEndTime()).toMillis());
            
        } catch (Exception e) {
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setErrorMessage(e.getMessage());
            execution.setEndTime(LocalDateTime.now());
        }
        
        return ExecuteResult.builder()
            .success(execution.getStatus() == ExecutionStatus.SUCCESS)
            .output(execution.getOutput())
            .error(execution.getErrorMessage())
            .executionId(execution.getId())
            .build();
    }
}
```

---

## 四、执行器实现

### 4.1 脚本执行器

```java
@Component
public class ScriptExecutor implements ActionExecutor {
    
    @Autowired
    private AgentClientService agentClient;
    
    @Override
    public ExecuteResult execute(Action action, Map<String, Object> context) {
        // 解析配置
        ActionConfig config = parseConfig(action.getConfig());
        
        Long serverId = context.get("serverId", Long.class);
        String script = replaceParams(config.getScript(), context);
        
        // 调用Agent执行
        Response response = agentClient.executeScript(serverId, script, action.getTimeout());
        
        return ExecuteResult.builder()
            .success(response.getExitCode() == 0)
            .output(response.getOutput())
            .error(response.getError())
            .build();
    }
    
    @Override
    public boolean validate(Action action) {
        try {
            ActionConfig config = parseConfig(action.getConfig());
            return config.getScriptId() != null || config.getScript() != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public ActionType getType() {
        return ActionType.EXECUTE_SCRIPT;
    }
}
```

### 4.2 Skill执行器

```java
@Component
public class SkillExecutor implements ActionExecutor {
    
    @Autowired
    private SkillService skillService;
    
    @Override
    public ExecuteResult execute(Action action, Map<String, Object> context) {
        ActionConfig config = parseConfig(action.getConfig());
        
        Long serverId = context.get("serverId", Long.class);
        String skillName = config.getSkillName();
        Map<String, Object> params = config.getParams();
        
        // 执行Skill
        SkillResult result = skillService.execute(skillName, serverId, params);
        
        return ExecuteResult.builder()
            .success(result.isSuccess())
            .output(result.getOutput())
            .error(result.getError())
            .build();
    }
    
    @Override
    public ActionType getType() {
        return ActionType.EXECUTE_SKILL;
    }
}
```

### 4.3 通知执行器

```java
@Component
public class NotificationExecutor implements ActionExecutor {
    
    @Autowired
    private NotificationService notificationService;
    
    @Override
    public ExecuteResult execute(Action action, Map<String, Object> context) {
        ActionConfig config = parseConfig(action.getConfig());
        
        String template = config.getTemplate();
        String channel = config.getChannel();  // "dingtalk", "email", "wecom"
        
        // 渲染消息
        String message = renderTemplate(template, context);
        
        // 发送通知
        boolean success = notificationService.send(channel, message);
        
        return ExecuteResult.builder()
            .success(success)
            .output("Notification sent via " + channel)
            .build();
    }
    
    @Override
    public ActionType getType() {
        return ActionType.SEND_NOTIFICATION;
    }
}
```

---

## 五、API 设计

### 5.1 动作管理 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/actions | 动作列表 |
| POST | /api/v1/actions | 创建动作 |
| GET | /api/v1/actions/{id} | 动作详情 |
| PUT | /api/v1/actions/{id} | 更新动作 |
| DELETE | /api/v1/actions/{id} | 删除动作 |
| POST | /api/v1/actions/{id}/execute | 手动执行 |

### 5.2 执行记录 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/actions/executions | 执行记录列表 |
| GET | /api/v1/actions/executions/{id} | 执行详情 |
| POST | /api/v1/actions/executions/{id}/cancel | 取消执行 |

---

## 六、实施计划

| 序号 | 工作内容 | 优先级 |
|------|----------|--------|
| 1 | Action 实体 + CRUD | P0 |
| 2 | ActionExecution 实体 | P0 |
| 3 | 执行引擎 | P0 |
| 4 | ScriptExecutor | P0 |
| 5 | SkillExecutor | P1 |
| 6 | NotificationExecutor | P1 |
| 7 | 前端动作管理页面 | P1 |

---

> 模块版本：V1.0
> 最后更新：2025-03-09
