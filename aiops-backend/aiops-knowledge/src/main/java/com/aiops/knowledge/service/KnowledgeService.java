package com.aiops.knowledge.service;

import com.aiops.knowledge.model.KnowledgeDoc;
import com.aiops.knowledge.model.KnowledgeRepo;
import com.aiops.knowledge.repository.KnowledgeDocRepository;
import com.aiops.knowledge.repository.KnowledgeRepoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class KnowledgeService {
    private final KnowledgeRepoRepository repoRepository;
    private final KnowledgeDocRepository docRepository;

    public KnowledgeService(KnowledgeRepoRepository repoRepository, KnowledgeDocRepository docRepository) {
        this.repoRepository = repoRepository;
        this.docRepository = docRepository;
    }

    public List<KnowledgeRepo> findAllRepos() { return repoRepository.findAll(); }
    public KnowledgeRepo findRepoById(Long id) { return repoRepository.findById(id).orElse(null); }
    public KnowledgeRepo createRepo(KnowledgeRepo repo) {
        repo.setCollectionName("knowledge_" + repo.getId());
        repo.setDocumentCount(0L);
        repo.setVectorCount(0L);
        return repoRepository.save(repo);
    }
    public KnowledgeRepo updateRepo(Long id, KnowledgeRepo repo) {
        repo.setId(id);
        repo.setUpdatedAt(java.time.LocalDateTime.now());
        return repoRepository.save(repo);
    }
    public void deleteRepo(Long id) {
        docRepository.deleteByRepoId(id);
        repoRepository.deleteById(id);
    }

    public List<KnowledgeDoc> findDocsByRepo(Long repoId) { return docRepository.findByRepoId(repoId); }
    public KnowledgeDoc createDoc(KnowledgeDoc doc) {
        KnowledgeRepo repo = findRepoById(doc.getRepoId());
        if (repo != null) {
            repo.setDocumentCount(repo.getDocumentCount() + 1);
            repoRepository.save(repo);
        }
        return docRepository.save(doc);
    }
    public void deleteDoc(Long id) {
        KnowledgeDoc doc = docRepository.findById(id).orElse(null);
        if (doc != null) {
            KnowledgeRepo repo = findRepoById(doc.getRepoId());
            if (repo != null) {
                repo.setDocumentCount(Math.max(0, repo.getDocumentCount() - 1));
                repoRepository.save(repo);
            }
        }
        docRepository.deleteById(id);
    }

    public Map<String, Object> search(Long repoId, String query, int topK) {
        KnowledgeRepo repo = findRepoById(repoId);
        if (repo == null) {
            throw new RuntimeException("Repository not found: " + repoId);
        }

        List<Map<String, Object>> results = new ArrayList<>();
        List<KnowledgeDoc> docs = docRepository.findByRepoId(repoId);
        
        int count = 0;
        for (KnowledgeDoc doc : docs) {
            if (count >= topK) break;
            if (doc.getContent() != null && doc.getContent().toLowerCase().contains(query.toLowerCase())) {
                Map<String, Object> item = new HashMap<>();
                item.put("title", doc.getTitle());
                item.put("content", doc.getContent());
                item.put("score", 1.0);
                item.put("docId", doc.getId());
                results.add(item);
                count++;
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("repoId", repoId);
        response.put("results", results);
        response.put("total", results.size());
        return response;
    }

    public Map<String, Object> searchAll(String query, int topK) {
        List<KnowledgeRepo> repos = repoRepository.findAll();
        List<Map<String, Object>> allResults = new ArrayList<>();

        for (KnowledgeRepo repo : repos) {
            Map<String, Object> results = search(repo.getId(), query, topK);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) results.get("results");
            allResults.addAll(items);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("results", allResults);
        response.put("total", allResults.size());
        return response;
    }
}
