package com.aiops.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record CommandResultRequest(
        @NotBlank(message = "commandId is required") String commandId,
        @NotBlank(message = "agentId is required") String agentId,
        @NotBlank(message = "status is required") String status,
        String output
) implements Serializable {
}
