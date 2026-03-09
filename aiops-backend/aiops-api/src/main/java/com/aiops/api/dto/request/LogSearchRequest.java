package com.aiops.api.dto.request;

import jakarta.validation.constraints.Min;

import java.io.Serializable;

public record LogSearchRequest(String query, @Min(value = 1, message = "limit must be greater than 0") int limit) implements Serializable {
}
