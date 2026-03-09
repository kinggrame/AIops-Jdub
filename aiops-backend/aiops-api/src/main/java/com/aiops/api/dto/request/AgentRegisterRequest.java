package com.aiops.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.util.List;

public record AgentRegisterRequest(
        @NotBlank(message = "hostname is required") String hostname,
        @NotBlank(message = "ip is required") String ip,
        @NotBlank(message = "token is required") String token,
        @NotEmpty(message = "capabilities are required") List<String> capabilities
) implements Serializable {
}
