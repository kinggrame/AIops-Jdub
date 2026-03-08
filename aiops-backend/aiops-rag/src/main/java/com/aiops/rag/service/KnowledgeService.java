package com.aiops.rag.service;

import com.aiops.rag.entity.KnowledgeDocument;

import java.util.List;

/**
 * Knowledge retrieval service.
 *
 * <p>Abstracts MVP retrieval over AIOps knowledge documents. The in-memory implementation
 * simulates vector retrieval so the agent analysis flow can already use RAG-style context.</p>
 *
 * <p><b>Asynchronous notes:</b></p>
 * <ul>
 *   <li>Retrieval is synchronous in the MVP.</li>
 *   <li>TODO: integrate Milvus and support asynchronous vector search.</li>
 * </ul>
 *
 * @author AI Ops Team
 * @since 1.0.0
 */
public interface KnowledgeService {

    /**
     * Searches knowledge documents.
     *
     * @param query query text
     * @param topK max number of documents
     * @return ranked documents
     */
    List<KnowledgeDocument> search(String query, int topK);
}
