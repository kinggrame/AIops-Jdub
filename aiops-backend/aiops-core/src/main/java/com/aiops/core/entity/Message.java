package com.aiops.core.entity;

import java.time.Instant;

public record Message(String role, String content, Instant timestamp) {
}
