package com.aiops.rag.entity;

import java.util.List;

public record KnowledgeDocument(
        String id,
        String title,
        String content,
        String category,
        List<String> keywords,
        double score
) {
}
