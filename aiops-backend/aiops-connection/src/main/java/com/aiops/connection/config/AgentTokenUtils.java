package com.aiops.connection.config;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class AgentTokenUtils {

    private AgentTokenUtils() {
    }

    public static String issueAgentToken(String seed, String hostname, String ip) {
        String raw = String.join(":",
                seed == null || seed.isBlank() ? "aiops-mvp-seed" : seed,
                hostname == null || hostname.isBlank() ? "unknown-host" : hostname,
                ip == null || ip.isBlank() ? "unknown-ip" : ip,
                IdUtil.fastSimpleUUID());
        String digest = SecureUtil.sha256(raw);
        String tokenValue = "agt:" + digest;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenValue.getBytes(StandardCharsets.UTF_8));
    }

    public static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        String normalized = authorizationHeader.trim();
        if (!normalized.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = normalized.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
