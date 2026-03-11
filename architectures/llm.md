# LLM 配置模块

> 模块：aiops-llm
> 版本：V2.0

---

## 一、架构概述

### 1.1 设计目标

本模块负责**LLM Provider管理、对话模型与嵌入模型分离配置**，支持多Provider切换。

### 1.2 核心特性

```
┌─────────────────────────────────────────────────────────────────────┐
│                      LLM 模块核心架构                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    Provider 类型支持                        │   │
│   │                                                             │   │
│   │   ┌─────────┐  ┌─────────┐  ┌─────────┐                   │   │
│   │   │ OpenAI  │  │ Ollama  │  │ Custom  │                   │   │
│   │   │  gpt-4  │  │ llama3  │  │         │                   │   │
│   │   └─────────┘  └─────────┘  └─────────┘                   │   │
│   │                                                             │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    服务层                                   │   │
│   │                                                             │   │
│   │   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │   │
│   │   │ LlmService  │  │ OllamaClient│  │ LlmConfig   │       │   │
│   │   │   (CRUD)   │  │  (HTTP)     │  │  (Bean)    │       │   │
│   │   └─────────────┘  └─────────────┘  └─────────────┘       │   │
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
    private String id;               // "openai", "ollama-local"
    
    private String name;             // "OpenAI", "Ollama本地"
    
    @Enumerated(EnumType.STRING)
    private ProviderType type;       // OPENAI / ANTHROPIC / OLLAMA / CUSTOM
    
    private String apiKey;           // API Key
    private String endpoint;         // 自定义端点
    
    @Enumerated(EnumType.STRING)
    private ModelType modelType;     // CHAT / EMBEDDING
    
    private String defaultModel;     // 默认模型
    
    private Double temperature;      // 温度
    private Integer maxTokens;       // 最大token
    
    private Integer dimensions;      // 嵌入维度
    private Integer priority;         // 优先级
    private Boolean enabled;         // 是否启用
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum ProviderType {
    OPENAI,      // OpenAI
    ANTHROPIC,  // Anthropic Claude
    OLLAMA,     // 本地Ollama
    CUSTOM      // 自定义
}

public enum ModelType {
    CHAT,       // 对话模型
    EMBEDDING   // 嵌入模型
}
```

---

## 三、核心服务

### 3.1 LlmService

```java
@Service
public class LlmService {
    
    // Provider管理
    public List<LlmProvider> findAllProviders();
    public LlmProvider findProviderById(String id);
    public LlmProvider saveProvider(LlmProvider provider);
    public void deleteProvider(String id);
    
    // 获取可用Provider
    public List<LlmProvider> findChatProviders();
    public List<LlmProvider> findEmbeddingProviders();
    public LlmProvider getDefaultChatProvider();
    public LlmProvider getDefaultEmbeddingProvider();
    
    // 对话与嵌入
    public String chat(String message, String providerId);
    public List<Double> embed(String text, String providerId);
    
    // 连接测试
    public boolean testConnection(String providerId);
    
    // 模型列表
    public List<String> listModels(String providerId);
}
```

### 3.2 OllamaClient

```java
@Component
public class OllamaClient {
    
    // 聊天
    public String chat(String model, String prompt);
    
    // 嵌入
    public List<Float> embed(String model, String text);
    
    // 模型列表
    public List<String> listModels();
    
    // 连接测试
    public boolean testConnection();
}
```

---

## 四、Spring配置

### 4.1 LlmConfig

```java
@Configuration
public class LlmConfig {
    
    @Bean
    @ConfigurationProperties(prefix = "llm.openai")
    public OpenAiProperties openAiProperties();
    
    @Bean
    public ChatLanguageModel chatLanguageModel(OpenAiProperties props);
    
    @Bean
    public EmbeddingModel embeddingModel(OpenAiProperties props);
}
```

### 4.2 配置属性

```yaml
llm:
  openai:
    api-key: ${OPENAI_API_KEY:your-api-key}
    model: gpt-4
    embedding-model: text-embedding-3-small
    temperature: 0.7
    max-tokens: 2000
    dimensions: 1536
    timeout: 60
  
  ollama:
    base-url: http://localhost:11434
    model: llama3
    temperature: 0.7
    timeout: 120
```

---

## 五、API 设计

### 5.1 Provider管理 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/llm/providers | Provider列表 |
| POST | /api/llm/providers | 添加Provider |
| GET | /api/llm/providers/{id} | Provider详情 |
| PUT | /api/llm/providers/{id} | 更新Provider |
| DELETE | /api/llm/providers/{id} | 删除Provider |
| POST | /api/llm/providers/{id}/test | 测试连接 |

### 5.2 模型 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/llm/models/chat | 对话模型列表 |
| GET | /api/llm/models/embedding | 嵌入模型列表 |

### 5.3 对话/嵌入 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/llm/chat | 对话 |
| POST | /api/llm/embed | 向量化 |

---

## 六、使用示例

### 6.1 添加Ollama Provider

```bash
curl -X POST http://localhost:8080/api/llm/providers \
  -H "Content-Type: application/json" \
  -d '{
    "id": "ollama-local",
    "name": "Ollama本地",
    "type": "OLLAMA",
    "modelType": "CHAT",
    "defaultModel": "llama3",
    "endpoint": "http://localhost:11434",
    "enabled": true
  }'
```

### 6.2 测试连接

```bash
curl -X POST http://localhost:8080/api/llm/providers/ollama-local/test
```

### 6.3 对话

```bash
curl -X POST http://localhost:8080/api/llm/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "你好，请介绍一下自己",
    "providerId": "ollama-local"
  }'
```

---

## 七、默认初始化

系统启动时自动创建默认Provider：

```java
public void initializeDefaultProviders() {
    createDefaultProvider("ollama-local", "Ollama Local", 
        LlmProvider.ProviderType.OLLAMA, 
        LlmProvider.ModelType.CHAT, 
        "llama3", 0.7, 2000);
}
```

---

## 八、注意事项

1. **Ollama必须提前启动**：`ollama serve`
2. **模型需要下载**：`ollama pull llama3`
3. **API Key配置**：生产环境请使用环境变量或密钥管理服务
4. **网络问题**：Ollama默认 `http://localhost:11434`

---

## 九、实施状态

| 功能 | 状态 |
|------|------|
| LlmProvider实体+CRUD | ✅ 完成 |
| Ollama HTTP客户端 | ✅ 完成 |
| OpenAI集成 | ✅ 完成 |
| 连接测试 | ✅ 完成 |
| 模型列表 | ✅ 完成 |
| 前端配置页面 | 🔄 待完善 |

---

> 模块版本：V2.0
> 最后更新：2026-03-10
