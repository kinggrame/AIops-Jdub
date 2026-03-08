package com.aiops.api.controller;

import com.aiops.api.dto.request.KnowledgeSearchRequest;
import com.aiops.common.model.ApiResponse;
import com.aiops.rag.service.KnowledgeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @PostMapping("/search")
    public ApiResponse<?> search(@Valid @RequestBody KnowledgeSearchRequest request) {
        return ApiResponse.ok(knowledgeService.search(request.query(), request.topK()));
    }
}
