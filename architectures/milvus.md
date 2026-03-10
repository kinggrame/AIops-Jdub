# Milvus 向量数据库 / 知识库

> 模块：aiops-knowledge
> 版本：V1.0

---

## 一、架构概述

### 1.1 设计目标

本模块负责**知识库多仓库管理、文档向量化、语义搜索**，支持用户搜索和AI Tool调用。

### 1.2 双重用途

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Milvus/知识库 双重用途                            │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌─────────────────────┐    ┌─────────────────────┐               │
│   │     用户搜索          │    │    AI Tool 调用     │               │
│   │                     │    │                     │               │
│   │  • 语义搜索          │    │  • Milvus_Tool     │               │
│   │  • 问答             │    │  • 检索运维知识     │               │
│   │  • 推荐             │    │  • 生成解决方案参考 │               │
│   │                     │    │                     │               │
│   └──────────┬──────────┘    └──────────┬──────────┘               │
│              │                            │                          │
│              └────────────┬─────────────┘                          │
│                           │                                         │
│                           ▼                                         │
│                  ┌─────────────────┐                                │
│                  │ Milvus向量库     │                                │
│                  └─────────────────┘                                │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                     Milvus/知识库 架构                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    知识库多仓库                              │   │
│   │                                                             │   │
│   │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │   │
│   │  │ Nginx仓库     │  │ MySQL仓库    │  │ 自定义仓库   │       │   │
│   │  │              │  │              │  │              │       │   │
│   │  │ documents   │  │ documents   │  │ documents   │       │   │
│   │  │ vectors    │  │ vectors    │  │ vectors    │       │   │
│   │  │              │  │              │  │              │       │   │
│   │  │ embedding:   │  │ embedding:   │  │ embedding:   │       │   │
│   │  │ text-ada-002│  │ text-ada-002│  │ custom      │       │   │
│   │  └──────────────┘  └──────────────┘  └──────────────┘       │   │
│   │                                                             │   │
│   └───────────────────────────────────────────────────────────────┘   │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                    核心服务                                  │   │
│   │                                                             │   │
│   │   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │   │
│   │   │ RepoService  │  │ EmbeddingSvc  │  │ SearchSvc   │   │   │
│   │   │ (仓库管理)   │  │ (向量化)      │  │ (语义搜索)   │   │   │
│   │   └──────────────┘  └──────────────┘  └──────────────┘   │   │
│   │                                                             │   │
│   └───────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 三、数据模型

### 3.1 KnowledgeRepo (知识库仓库)

```java
@Entity
@Table(name = "knowledge_repos")
public class KnowledgeRepo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;                  // "Nginx知识库"
    private String description;          // "Nginx配置、故障处理"
    
    private Long ownerId;                 // 所有者
    
    // 每个仓库独立配置嵌入模型
    private String embeddingModelId;      // "openai-ada-002"
    private Integer embeddingDimensions;  // 1536
    
    @Enumerated(EnumType.STRING)
    private RepoVisibility visibility;    // PUBLIC / PRIVATE
    
    // Milvus collection名称
    private String collectionName;        // "knowledge_1"
    
    // 统计
    private Long documentCount;
    private Long vectorCount;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum RepoVisibility {
    PUBLIC,    // 公开
    PRIVATE    // 私有
}
```

### 3.2 KnowledgeDoc (文档)

```java
@Entity
@Table(name = "knowledge_docs")
public class KnowledgeDoc {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long repoId;                // 仓库ID
    
    private String title;              // 文档标题
    private String content;            // 文档内容
    
    @Enumerated(EnumType.STRING)
    private DocType docType;          // MARKDOWN / PDF / TXT / HTML
    
    private String filePath;           // 文件路径
    private Long fileSize;            // 文件大小
    
    // 向量ID (Milvus)
    private List<String> vectorIds;
    
    private String tags;              // 标签
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum DocType {
    MARKDOWN,
    PDF,
    TXT,
    HTML
}
```

---

## 四、核心服务

### 4.1 仓库服务

```java
@Service
public class RepoService {
    
    public KnowledgeRepo createRepo(CreateRepoRequest request) {
        // 1. 创建仓库
        KnowledgeRepo repo = new KnowledgeRepo();
        repo.setName(request.getName());
        repo.setDescription(request.getDescription());
        repo.setOwnerId(request.getOwnerId());
        repo.setEmbeddingModelId(request.getEmbeddingModelId());
        repo.setCollectionName("knowledge_" + repo.getId());
        
        // 2. 创建Milvus Collection
        milvusClient.createCollection(CreateCollectionRequest.builder()
            .collectionName(repo.getCollectionName())
            .dimension(repo.getEmbeddingDimensions())
            .build());
        
        // 3. 保存到数据库
        return repository.save(repo);
    }
    
    public void deleteRepo(Long repoId) {
        KnowledgeRepo repo = repository.findById(repoId);
        
        // 1. 删除Milvus Collection
        milvusClient.dropCollection(repo.getCollectionName());
        
        // 2. 删除文档
        docRepository.deleteByRepoId(repoId);
        
        // 3. 删除仓库
        repository.delete(repo);
    }
}
```

### 4.2 向量化服务

```java
@Service
public class EmbeddingService {
    
    @Autowired
    private LlmProvider llmProvider;
    
    // 文本向量化
    public List<Float> embed(String text) {
        // 调用嵌入模型
        EmbeddingResponse resp = llmProvider.embed(
            EmbeddingRequest.builder()
                .model(embeddingModel.getModel())
                .input(text)
                .build()
        );
        
        return resp.getData().get(0).getEmbedding();
    }
    
    // 文档向量化存储
    public void embedAndStore(Long repoId, KnowledgeDoc doc) {
        KnowledgeRepo repo = repoRepository.findById(repoId);
        
        // 1. 切分文档 (如果太长)
        List<String> chunks = splitIntoChunks(doc.getContent());
        
        // 2. 向量化每个chunk
        List<InsertParam.Field> fields = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            List<Float> vector = embed(chunk);
            
            ids.add(doc.getId() * 1000 + i);
            fields.add(new InsertParam.Field("id", ids));
            fields.add(new InsertParam.Field("chunk", chunk));
            fields.add(new InsertParam.Field("vector", vector));
        }
        
        // 3. 存储到Milvus
        milvusClient.insert(InsertRequest.builder()
            .collectionName(repo.getCollectionName())
            .fields(fields)
            .build());
        
        // 4. 更新统计
        repo.setVectorCount(repo.getVectorCount() + chunks.size());
        repo.setDocumentCount(repo.getDocumentCount() + 1);
        repoRepository.save(repo);
    }
}
```

### 4.3 搜索服务

```java
@Service
public class KnowledgeSearchService {
    
    public SearchResult search(Long repoId, String query, Integer topK) {
        KnowledgeRepo repo = repoRepository.findById(repoId);
        
        // 1. 向量化查询
        List<Float> vector = embeddingService.embed(query);
        
        // 2. 搜索向量数据库
        SearchResponse resp = milvusClient.search(SearchRequest.builder()
            .collectionName(repo.getCollectionName())
            .vector(vector)
            .topK(topK)
            .build());
        
        // 3. 转换结果
        List<SearchResult.Item> results = new ArrayList<>();
        for (SearchResult.Item item : resp.getResults()) {
            results.add(SearchResult.Item.builder()
                .chunk(item.get("chunk"))
                .score(item.getScore())
                .build());
        }
        
        return SearchResult.builder()
            .query(query)
            .repoId(repoId)
            .results(results)
            .build();
    }
}
```

---

## 五、AI Tool 集成

### 5.1 Milvus Tool 实现

```java
@Component
public class MilvusToolExecutor implements ToolExecutor {
    
    @Override
    public ToolResult execute(Map<String, Object> params) {
        String query = (String) params.get("query");
        Long repoId = (Long) params.get("repo_id");
        Integer topK = (Integer) params.getOrDefault("top_k", 5);
        
        // 搜索知识库
        SearchResult result = searchService.search(repoId, query, topK);
        
        return ToolResult.builder()
            .success(true)
            .data(Map.of(
                "results", result.getResults(),
                "query", query,
                "repo_id", repoId
            ))
            .build();
    }
    
    @Override
    public String getDefinition() {
        return """
        {
            "type": "function",
            "function": {
                "name": "milvus_search",
                "description": "搜索知识库，获取运维知识、解决方案、故障处理方法。",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "query": {
                            "type": "string",
                            "description": "搜索内容，如 'Nginx 502错误', 'MySQL连接失败', '磁盘满处理'"
                        },
                        "repo_id": {
                            "type": "integer",
                            "description": "知识库ID"
                        },
                        "top_k": {
                            "type": "integer",
                            "description": "返回条数，默认5"
                        }
                    },
                    "required": ["query", "repo_id"]
                }
            }
        }
        """;
    }
}
```

---

## 六、API 设计

### 6.1 仓库管理 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/knowledge/repos | 仓库列表 |
| POST | /api/v1/knowledge/repos | 创建仓库 |
| GET | /api/v1/knowledge/repos/{id} | 仓库详情 |
| PUT | /api/v1/knowledge/repos/{id} | 更新仓库 |
| DELETE | /api/v1/knowledge/repos/{id} | 删除仓库 |

### 6.2 文档管理 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/v1/knowledge/repos/{id}/documents | 上传文档 |
| GET | /api/v1/knowledge/repos/{id}/documents | 文档列表 |
| DELETE | /api/v1/knowledge/documents/{id} | 删除文档 |

### 6.3 搜索 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/v1/knowledge/repos/{id}/search | 语义搜索 |

---

## 七、实施计划

| 序号 | 工作内容 | 优先级 |
|------|----------|--------|
| 1 | KnowledgeRepo 实体 + CRUD | P0 |
| 2 | KnowledgeDoc 实体 + 上传 | P0 |
| 3 | EmbeddingService | P0 |
| 4 | Milvus Tool | P0 |
| 5 | 前端知识库页面 | P1 |

---

> 模块版本：V1.0
> 最后更新：2025-03-09
