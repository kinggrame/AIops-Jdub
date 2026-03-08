package com.aiops.core.llm;

import com.aiops.core.config.LlmProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OpenAiProvider implements LlmProvider {

    private final LlmProperties properties;

    public OpenAiProvider(LlmProperties properties) {
        this.properties = properties;
    }

    @Override
    public String name() {
        return "OpenAI";
    }

    @Override
    public boolean available() {
        return properties.isOpenAiEnabled();
    }

    @Override
    public String generate(String prompt, Map<String, Object> context) {
        return "[OpenAI simulated] " + prompt + " | context=" + context;
    }
}
