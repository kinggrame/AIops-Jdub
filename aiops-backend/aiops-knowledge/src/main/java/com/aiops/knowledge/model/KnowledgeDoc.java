package com.aiops.knowledge.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_docs")
public class KnowledgeDoc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long repoId;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    @Enumerated(EnumType.STRING)
    private DocType docType = DocType.MARKDOWN;
    private String filePath;
    private Long fileSize;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public KnowledgeDoc() {
        this.createdAt = LocalDateTime.now();
    }

    public enum DocType {
        MARKDOWN, PDF, TXT, HTML
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getRepoId() { return repoId; } public void setRepoId(Long repoId) { this.repoId = repoId; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; } public void setContent(String content) { this.content = content; }
    public DocType getDocType() { return docType; } public void setDocType(DocType docType) { this.docType = docType; }
    public String getFilePath() { return filePath; } public void setFilePath(String filePath) { this.filePath = filePath; }
    public Long getFileSize() { return fileSize; } public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getTags() { return tags; } public void setTags(String tags) { this.tags = tags; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
