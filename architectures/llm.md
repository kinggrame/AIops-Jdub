# LLM 配置模块

> 模块：aiops-llm
> 版本：V1.0

---

## 一、架构概述

### 1.1 设计目标

本模块负责**LLM Provider管理、对话模型与嵌入模型分离配置**，支持多Provider切换。

### 1.2 核心概念

```
┌─────────────────────────────────────────────────────────────────────┐
│                      LLM 配置核心                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    对话模型 vs 嵌入模型                      │   │
│   │                                                             │   │
│   │   ┌───────────────────┐      ┌───────────────────┐         │   │
│   │   │    对话模型        │      │    嵌入模型        │         │   │
│   │   │  (Chat Model)     │      │ (Embedding Model) │         │   │
│   │   │                   │      │                   │         │   │
│   │   │ • Agent对话       │      │ • 知识库向量化     │         │   │
│   │   │ • 告警分析        │      │ • 语义搜索        │         │   │
│   │   │ • 报告生成        │      │                   │         │   │
│   │   │                   │      │                   │         │   │
│   │   │ provider:        │      │ provider:        │         │   │
│   │   │   gpt-4         │      │   text-ada-002   │         │   │
│   │   │   claude-3      │      │   text-embedding-3│        │   │
│   │   └───────────────────┘      └───────────────────┘         │   │
│   │                                                             │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    多Provider支持                           │   │
│   │                                                             │   │
│   │   ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐     │   │
│   │   │ OpenAI  │  │Claude   │  │ Ollama  │  │ 自定义   │     │   │
│   │   │  gpt-4  │  │ claude-3│  │ llama2  │  │         │     │   │
│   │   └─────────┘  └─────────┘  └─────────┘  └─────────┘     │   │
│   │                                                             │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、数据模型

### 2.1 LlmProvider (Provider配置)

```java
@Entity
@Table(name = "llm_providers")
public class LlmProvider {
    
    @Id
    private String id;               // "openai", "anthropic", "ollama"
    
    private String name;             // "OpenAI"
    
    @Enumerated(EnumType.STRING)
    private ProviderType type;       // OPENAI / ANTHROPIC / OLLAMA / CUSTOM
    
    private String apiKey;           // API Key (加密存储)
    private String endpoint;         // 自定义端点
    
    @Enumerated(EnumType.STRING)
    private ModelType modelType;     // CHAT / EMBEDDING
    
    private String defaultModel;     // 默认模型
    
    private Double temperature;      // 默认温度
    private Integer maxTokens;      // 最大token
    
    private Integer dimensions;      // 嵌入维度 (嵌入模型用)
    
    private Integer priority;        // 优先级 (故障转移)
    
    private Boolean enabled;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum ProviderType {
    OPENAI,      // OpenAI
    ANTHROPIC,   // Anthropic Claude
    OLLAMA,      // 本地Ollama
    CUSTOM       // 自定义
}

public enum ModelType {
    CHAT,        // 对话模型
    EMBEDDING    // 嵌入模型
}
```

### 2.2 ChatModelConfig (对话模型)

```java
// 对话模型配置 - 对应 AI Agent、告警分析等
public class ChatModelConfig {
    private String id;
    private String providerId;      // Provider ID
    
    private String model;           // "gpt-4", "claude-3", "llama2"
    
    private Double temperature;     // 0.0-1.0
    private Integer maxTokens;      // 最大token
    
    private Integer contextWindow;  // 上下文窗口
    
    private Double pricePer1kInput; // 输入价格
    private Double pricePer1kOutput; // 输出价格
    
    private Boolean enabled;
    private Integer priority;       // 优先级
}
```

### 2.3 EmbeddingModelConfig (嵌入模型)

```java
// 嵌入模型配置 - 对应知识库向量化
public class EmbeddingModelConfig {
    private String id;
    private String providerId;      // Provider ID
    
    private String model;           // "text-embedding-ada-002"
    
    private Integer dimensions;     // 1536 / 1024 / 768
    
    private Double pricePer1kUnit; // 价格
    
    private Boolean enabled;
}
```

---

## 三、Provider管理

### 3.1 LLM Provider 服务

```java
@Service
public class LlmProviderService {
    
    public LlmProvider createProvider(CreateProviderRequest request) {
        // 1. 验证
        validateRequest(request);
        
        // 2. 加密存储apiKey
        String encryptedKey = encryptionService.encrypt(request.getApiKey());
        
        // 3. 创建
        LlmProvider provider = new LlmProvider();
        provider.setId(generateId());
        provider.setName(request.getName());
        provider.setType(request.getType());
        provider.setApiKey(encryptedKey);
        provider.setEndpoint(request.getEndpoint());
        provider.setModelType(request.getModelType());
        provider.setDefaultModel(request.getDefaultModel());
        provider.setEnabled(true);
        
        return repository.save(provider);
    }
    
    public boolean testConnection(String providerId) {
        LlmProvider provider = repository.findById(providerId);
        
        try {
            switch (provider.getType()) {
                case OPENAI:
                    return testOpenAI(provider);
                case ANTHROPIC:
                    return testAnthropic(provider);
                case OLLAMA:
                    return testOllama(provider);
                default:
                    return false;
            }
        } catch (Exception e) {
            log.error("Connection test failed", e);
            return false;
        }
    }
}
```

---

## 四、对话服务

### 4.1 Chat 服务

```java
@Service
public class ChatService {
    
    public ChatResponse chat(ChatRequest request) {
        // 1. 获取可用的Chat模型
        ChatModelConfig model = getAvailableChatModel();
        
        // 2. 构建请求
        ChatCompletionRequest req = ChatCompletionRequest.builder()
            .model(model.getModel())
            .messages(request.getMessages())
            .temperature(request.getTemperature() != null ? 
                request.getTemperature() : model.getTemperature())
            .maxTokens(request.getMaxTokens() != null ? 
                request.getMaxTokens() : model.getMaxTokens())
            .build();
        
        // 3. 调用LLM
        ChatCompletionResponse resp = llmProvider.chat(req);
        
        // 4. 返回结果
        return ChatResponse.builder()
            .message(resp.getChoices().get(0).getMessage())
            .model(model.getModel())
            .usage(resp.getUsage())
            .build();
    }
}
```

---

## 五、嵌入服务

### 5.1 Embedding 服务

```java
@Service
public class EmbeddingService {
    
    public EmbeddingResponse embed(EmbeddingRequest request) {
        // 1. 获取可用的嵌入模型
        EmbeddingModelConfig model = getAvailableEmbeddingModel();
        
        // 2. 调用LLM
        EmbeddingResponse resp = llmProvider.embed(
            EmbeddingRequest.builder()
                .model(model.getModel())
                .input(request.getText())
                .build()
        );
        
        return resp;
    }
    
    public List<Float> embed(String text) {
        EmbeddingResponse resp = embed(EmbeddingRequest.builder()
            .text(text)
            .build());
        
        return resp.getData().get(0).getEmbedding();
    }
}
```

---

## 六、API 设计

### 6.1 Provider管理 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/llm/providers | Provider列表 |
| POST | /api/v1/llm/providers | 添加Provider |
| GET | /api/v1/llm/providers/{id} | Provider详情 |
| PUT | /api/v1/llm/providers/{id} | 更新Provider |
| DELETE | /api/v1/llm/providers/{id} | 删除Provider |
| POST | /api/v1/llm/providers/{id}/test | 测试连接 |

### 6.2 模型 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/llm/models/chat | 对话模型列表 |
| GET | /api/v1/llm/models/embedding | 嵌入模型列表 |

### 6.3 对话/嵌入 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/v1/llm/chat | 对话 |
| POST | /api/v1/llm/embed | 向量化 |

---

## 七、前端配置页面

```
┌─────────────────────────────────────────────────────────────────────┐
│                      LLM 配置页面                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                    对话模型配置                              │   │
│  │  ┌─────────────────────────────────────────────────────────┐│   │
│  │  │ Provider: [OpenAI ▼]                                 ││   │
│  │  │ Model:     [gpt-4   ▼]                                ││   │
│  │  │ API Key:   [••••••••••••••••]  [测试连接]             ││   │
│  │  │ Temperature: [0.7    ]                                ││   │
│  │  │ Max Tokens: [2000   ]                                 ││   │
│  │  │ Enabled:    [✓]                                       ││   │
│  │  │                           [保存]                      ││   │
│  │  └─────────────────────────────────────────────────────────┘│   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                    嵌入模型配置                              │   │
│  │  ┌─────────────────────────────────────────────────────────┐│   │
│  │  │ Provider: [OpenAI ▼]                                 ││   │
│  │  │ Model:     [text-embedding-ada-002 ▼]                ││   │
│  │  │ API Key:   [••••••••••••••••]  [测试连接]             ││   │
│  │  │ Dimensions:[1536  ]                                   ││   │
│  │  │ Enabled:    [✓]                                       ││   │
│  │  │                           [保存]                      ││   │
│  │  └─────────────────────────────────────────────────────────┘│   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 八、实施计划

| 序号 | 工作内容 | 优先级 |
|------|----------|--------|
| 1 | LlmProvider 实体 + CRUD | P0 |
| 2 | Chat/Embedding模型管理 | P0 |
| 3 | 多Provider支持 (OpenAI/Anthropic/Ollama) | P0 |
| 4 | 对话服务 | P0 |
| 5 | 嵌入服务 | P0 |
| 6 | 前端LLM配置页面 | P0 |

---

> 模块版本：V1.0
> 最后更新：2025-03-09
