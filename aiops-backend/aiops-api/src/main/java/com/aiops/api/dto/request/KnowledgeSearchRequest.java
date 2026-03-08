package com.aiops.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record KnowledgeSearchRequest(
        @NotBlank(message = "query is required") String query,
        @Min(value = 1, message = "topK must be greater than 0") int topK
) {
}
