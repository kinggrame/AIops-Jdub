package com.aiops.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateAlertRequest(
        @NotBlank(message = "hostname is required") String hostname,
        @NotBlank(message = "source is required") String source,
        @NotBlank(message = "metric is required") String metric,
        @NotBlank(message = "severity is required") String severity,
        @PositiveOrZero(message = "currentValue must be positive") double currentValue,
        @PositiveOrZero(message = "threshold must be positive") double threshold,
        @NotBlank(message = "description is required") String description,
        String status
) {
}
