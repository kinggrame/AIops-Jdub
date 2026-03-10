package com.aiops.knowledge.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_repos")
public class KnowledgeRepo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private String embeddingModelId = "text-embedding-ada-002";
    private Integer embeddingDimensions = 1536;
    @Enumerated(EnumType.STRING)
    private RepoVisibility visibility = RepoVisibility.PRIVATE;
    private String collectionName;
    private Long documentCount = 0L;
    private Long vectorCount = 0L;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public KnowledgeRepo() {
        this.createdAt = LocalDateTime.now();
    }

    public enum RepoVisibility {
        PUBLIC, PRIVATE
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public Long getOwnerId() { return ownerId; } public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getEmbeddingModelId() { return embeddingModelId; } public void setEmbeddingModelId(String embeddingModelId) { this.embeddingModelId = embeddingModelId; }
    public Integer getEmbeddingDimensions() { return embeddingDimensions; } public void setEmbeddingDimensions(Integer embeddingDimensions) { this.embeddingDimensions = embeddingDimensions; }
    public RepoVisibility getVisibility() { return visibility; } public void setVisibility(RepoVisibility visibility) { this.visibility = visibility; }
    public String getCollectionName() { return collectionName; } public void setCollectionName(String collectionName) { this.collectionName = collectionName; }
    public Long getDocumentCount() { return documentCount; } public void setDocumentCount(Long documentCount) { this.documentCount = documentCount; }
    public Long getVectorCount() { return vectorCount; } public void setVectorCount(Long vectorCount) { this.vectorCount = vectorCount; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
