package com.aiops.core.llm;

import com.aiops.core.config.LlmProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OllamaProvider implements LlmProvider {

    private final LlmProperties properties;

    public OllamaProvider(LlmProperties properties) {
        this.properties = properties;
    }

    @Override
    public String name() {
        return "Ollama";
    }

    @Override
    public boolean available() {
        return properties.isOllamaEnabled();
    }

    @Override
    public String generate(String prompt, Map<String, Object> context) {
        return "[Ollama simulated] " + prompt + " | context=" + context;
    }
}
