# ELK 日志系统

> 模块：aiops-log
> 版本：V1.0

---

## 一、架构概述

### 1.1 设计目标

本模块负责**日志采集、存储、查询**，支持用户搜索和AI Tool调用。

### 1.2 双重用途

```
┌─────────────────────────────────────────────────────────────────────┐
│                      ELK 双重用途                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌─────────────────────┐    ┌─────────────────────┐               │
│   │     用户搜索          │    │    AI Tool 调用     │               │
│   │                     │    │                     │               │
│   │  • 查问题            │    │  • ELK_Tool(query) │               │
│   │  • 排查错误          │    │  • 自动分析日志     │               │
│   │  • 日志分析          │    │  • 根因定位         │               │
│   │                     │    │                     │               │
│   └──────────┬──────────┘    └──────────┬──────────┘               │
│              │                            │                          │
│              └────────────┬─────────────┘                          │
│                           │                                         │
│                           ▼                                         │
│                  ┌─────────────────┐                                │
│                  │   Elasticsearch │                                │
│                  └─────────────────┘                                │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                        ELK 架构                                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   Agent                           ES                      Kibana    │
│   ──────                         ──                       ──────    │
│     │                             │                          │       │
│     │ 1. 日志产生                  │                          │       │
│     │   (应用日志/系统日志)         │                          │       │
│     │                             │                          │       │
│     │ 2. 发送到后端                │                          │       │
│     │   ────────────────────────▶│                          │       │
│     │                             │                          │       │
│     │                        3. 写入ES                      │       │
│     │                             │                          │       │
│     │                        4. 用户搜索 ◀────────────────│       │
│     │                             │                          │       │
│     │                        5. AI查询 ◀─────────────────│       │
│     │                             │                          │       │
│     │                        6. 返回结果 ────────────────▶│       │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 三、数据模型

### 3.1 LogEntry

```java
@Entity
@Table(name = "log_entries")
public class LogEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String agentId;          // Agent ID
    private Long serverId;          // 服务器ID
    
    private String source;           // 日志源: "nginx", "system", "application"
    private String level;            // 日志级别: "INFO", "WARN", "ERROR"
    private String message;          // 日志内容
    
    private LocalDateTime timestamp;// 时间戳
    
    // ES索引用
    @Field(type = FieldType.Keyword)
    private String indexName;       // "logs-2025.03.09"
    
    // 额外字段
    private Map<String, Object> extra;
}
```

---

## 四、日志采集

### 4.1 Agent端日志采集

```go
// Agent端日志采集
type LogCollector struct {
    watchPaths []string
    patterns   []string
}

func (c *LogCollector) Start() {
    for _, path := range c.watchPaths {
        go c.watchFile(path)
    }
}

func (c *LogCollector) watchFile(path string) {
    // 使用fsnotify监听文件变化
    watcher, _ := fsnotify.NewWatcher()
    defer watcher.Close()
    
    watcher.Add(path)
    
    for {
        select {
        case event := <-watcher.Events:
            if event.Op&fsnotify.Write == fsnotify.Write {
                newLines := c.readNewLines(path)
                c.sendToBackend(newLines)
            }
        case err := <-watcher.Errors:
            log.Error(err)
        }
    }
}

func (c *LogCollector) sendToBackend(logs string) {
    // 发送到后端
    http.Post(
        serverUrl+"/api/v1/logs/ingest",
        "application/json",
        strings.NewReader(logs),
    )
}
```

### 4.2 后端日志接收

```java
@Service
public class LogIngestService {
    
    @Autowired
    private ElasticsearchClient esClient;
    
    public void ingest(LogIngestRequest request) {
        String indexName = "logs-" + LocalDate.now().format(DateFormatter);
        
        for (String line : request.getLogs().split("\n")) {
            // 解析日志
            LogEntry entry = parser.parse(line);
            entry.setAgentId(request.getAgentId());
            entry.setServerId(request.getServerId());
            entry.setTimestamp(LocalDateTime.now());
            entry.setIndexName(indexName);
            
            // 写入ES
            try {
                esClient.index(i -> i
                    .index(indexName)
                    .document(entry)
                );
            } catch (IOException e) {
                log.error("Failed to ingest log", e);
            }
        }
    }
}
```

---

## 五、日志查询

### 5.1 用户搜索API

```java
// GET /api/v1/logs/search
// 参数: query=error&level=ERROR&time_range=1h&limit=100

public class LogSearchRequest {
    private String query;           // 搜索关键字
    private String level;          // 日志级别
    private String source;         // 日志源
    private String timeRange;      // 时间范围
    private Integer limit;         // 返回条数
    private Integer page;          // 页码
}

public class LogSearchResponse {
    private List<LogEntry> logs;
    private Long total;
    private Long took;             // 耗时(毫秒)
}
```

### 5.2 ES查询实现

```java
@Service
public class LogQueryService {
    
    public LogSearchResponse search(LogSearchRequest request) {
        // 构建ES查询
        BoolQueryBuilder esQuery = QueryBuilders.boolQuery();
        
        // 关键词匹配
        if (request.getQuery() != null) {
            esQuery.must(QueryBuilders.matchQuery("message", request.getQuery()));
        }
        
        // 级别过滤
        if (request.getLevel() != null) {
            esQuery.filter(QueryBuilders.termQuery("level", request.getLevel()));
        }
        
        // 源过滤
        if (request.getSource() != null) {
            esQuery.filter(QueryBuilders.termQuery("source", request.getSource()));
        }
        
        // 时间范围
        if (request.getTimeRange() != null) {
            esQuery.filter(QueryBuilders.rangeQuery("@timestamp")
                .gte("now-" + request.getTimeRange()));
        }
        
        // 执行搜索
        SearchResponse response = esClient.search(s -> s
            .index("logs-*")
            .query(q -> q.bool(esQuery))
            .size(request.getLimit())
            .from((request.getPage() - 1) * request.getLimit())
            .sort(ss -> ss.field(f -> f.field("@timestamp").order(SortOrder.DESC)))
        );
        
        // 转换结果
        List<LogEntry> logs = new ArrayList<>();
        for (Hit<Map> hit : response.getHits().getHits()) {
            logs.add(mapper.convertValue(hit.getSourceAsMap(), LogEntry.class));
        }
        
        return LogSearchResponse.builder()
            .logs(logs)
            .total(response.getHits().getTotalHits())
            .took(response.getTook())
            .build();
    }
}
```

---

## 六、AI Tool 集成

### 6.1 ELK Tool 实现

```java
@Component
public class ELKToolExecutor implements ToolExecutor {
    
    @Override
    public ToolResult execute(Map<String, Object> params) {
        String query = (String) params.get("query");
        String timeRange = (String) params.getOrDefault("time_range", "1h");
        Integer limit = (Integer) params.getOrDefault("limit", 100);
        
        // 查询ES
        SearchResponse response = esClient.search(s -> s
            .index("logs-*")
            .query(q -> q.bool(b -> b
                .must(m -> m.match(mt -> mt.field("message").query(query)))
                .filter(f -> f.range(r -> r.field("@timestamp")
                    .gte(JsonData.of("now-" + timeRange))))
            ))
            .size(limit)
        );
        
        List<Map<String, Object>> logs = response.getHits().getHits().stream()
            .map(hit -> hit.getSourceAsMap())
            .collect(Collectors.toList());
        
        return ToolResult.builder()
            .success(true)
            .data(Map.of("logs", logs, "count", logs.size()))
            .build();
    }
    
    @Override
    public String getDefinition() {
        return """
        {
            "type": "function",
            "function": {
                "name": "elk_query",
                "description": "查询Elasticsearch日志，用于分析问题根因。",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "query": {
                            "type": "string",
                            "description": "日志查询关键字"
                        },
                        "time_range": {
                            "type": "string",
                            "description": "时间范围"
                        },
                        "limit": {
                            "type": "integer",
                            "description": "返回条数"
                        }
                    },
                    "required": ["query"]
                }
            }
        }
        """;
    }
}
```

---

## 七、API 设计

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/v1/logs/ingest | 日志上报 |
| GET | /api/v1/logs/search | 日志搜索 |
| GET | /api/v1/logs/sources | 日志源列表 |
| GET | /api/v1/logs/levels | 日志级别列表 |

---

## 八、实施计划

| 序号 | 工作内容 | 优先级 |
|------|----------|--------|
| 1 | LogEntry 实体 | P0 |
| 2 | 日志接收接口 | P0 |
| 3 | 日志搜索API | P0 |
| 4 | ELK Tool | P0 |
| 5 | 前端日志页面 | P1 |
| 6 | Kibana集成 | P2 |

---

> 模块版本：V1.0
> 最后更新：2025-03-09
