package com.aiops.rag.repository;

import com.aiops.rag.entity.KnowledgeDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocumentEntity, String> {
}
