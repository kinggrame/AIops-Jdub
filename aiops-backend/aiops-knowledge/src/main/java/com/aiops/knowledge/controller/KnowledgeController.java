package com.aiops.knowledge.controller;

import com.aiops.knowledge.model.KnowledgeDoc;
import com.aiops.knowledge.model.KnowledgeRepo;
import com.aiops.knowledge.service.KnowledgeService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @GetMapping("/repos")
    public List<KnowledgeRepo> listRepos() { return knowledgeService.findAllRepos(); }

    @PostMapping("/repos")
    public KnowledgeRepo createRepo(@RequestBody KnowledgeRepo repo) { return knowledgeService.createRepo(repo); }

    @GetMapping("/repos/{id}")
    public KnowledgeRepo getRepo(@PathVariable Long id) { return knowledgeService.findRepoById(id); }

    @PutMapping("/repos/{id}")
    public KnowledgeRepo updateRepo(@PathVariable Long id, @RequestBody KnowledgeRepo repo) {
        return knowledgeService.updateRepo(id, repo);
    }

    @DeleteMapping("/repos/{id}")
    public void deleteRepo(@PathVariable Long id) { knowledgeService.deleteRepo(id); }

    @GetMapping("/repos/{id}/documents")
    public List<KnowledgeDoc> listDocs(@PathVariable Long id) { return knowledgeService.findDocsByRepo(id); }

    @PostMapping("/repos/{id}/documents")
    public KnowledgeDoc createDoc(@PathVariable Long id, @RequestBody KnowledgeDoc doc) {
        doc.setRepoId(id);
        return knowledgeService.createDoc(doc);
    }

    @DeleteMapping("/documents/{id}")
    public void deleteDoc(@PathVariable Long id) { knowledgeService.deleteDoc(id); }

    @PostMapping("/repos/{id}/search")
    public Map<String, Object> search(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        Integer topK = (Integer) request.getOrDefault("top_k", 5);
        return knowledgeService.search(id, query, topK != null ? topK : 5);
    }

    @PostMapping("/search")
    public Map<String, Object> searchAll(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        Integer topK = (Integer) request.getOrDefault("top_k", 5);
        return knowledgeService.searchAll(query, topK != null ? topK : 5);
    }
}
