package com.aiops.llm.controller;

import com.aiops.llm.model.LlmProvider;
import com.aiops.llm.service.LlmService;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/llm")
public class LlmController {
    private final LlmService llmService;

    public LlmController(LlmService llmService) {
        this.llmService = llmService;
    }

    @PostConstruct
    public void init() {
        llmService.initializeDefaultProviders();
    }

    @GetMapping("/providers")
    public List<LlmProvider> listProviders() { return llmService.findAllProviders(); }

    @PostMapping("/providers")
    public LlmProvider createProvider(@RequestBody LlmProvider provider) { 
        return llmService.saveProvider(provider); 
    }

    @GetMapping("/providers/{id}")
    public LlmProvider getProvider(@PathVariable String id) { return llmService.findProviderById(id); }

    @PutMapping("/providers/{id}")
    public LlmProvider updateProvider(@PathVariable String id, @RequestBody LlmProvider provider) {
        provider.setId(id);
        return llmService.saveProvider(provider);
    }

    @DeleteMapping("/providers/{id}")
    public void deleteProvider(@PathVariable String id) { llmService.deleteProvider(id); }

    @PostMapping("/providers/{id}/test")
    public Map<String, Object> testConnection(@PathVariable String id) {
        boolean success = llmService.testConnection(id);
        return Map.of("success", success, "message", success ? "Connection successful" : "Connection failed");
    }

    @GetMapping("/models/chat")
    public List<LlmProvider> chatModels() { return llmService.findChatProviders(); }

    @GetMapping("/models/embedding")
    public List<LlmProvider> embeddingModels() { return llmService.findEmbeddingProviders(); }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        String providerId = (String) request.get("providerId");
        String response = llmService.chat(message, providerId);
        return Map.of("message", response, "provider", providerId != null ? providerId : "default");
    }

    @PostMapping("/embed")
    public Map<String, Object> embed(@RequestBody Map<String, Object> request) {
        String text = (String) request.get("text");
        String providerId = (String) request.get("providerId");
        List<Double> embedding = llmService.embed(text, providerId);
        return Map.of("embedding", embedding, "dimensions", embedding.size());
    }
}
