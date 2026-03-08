package com.aiops.core.llm;

import com.aiops.core.config.LlmProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AnthropicProvider implements LlmProvider {

    private final LlmProperties properties;

    public AnthropicProvider(LlmProperties properties) {
        this.properties = properties;
    }

    @Override
    public String name() {
        return "Claude";
    }

    @Override
    public boolean available() {
        return properties.isAnthropicEnabled();
    }

    @Override
    public String generate(String prompt, Map<String, Object> context) {
        return "[Claude simulated] " + prompt + " | context=" + context;
    }
}
