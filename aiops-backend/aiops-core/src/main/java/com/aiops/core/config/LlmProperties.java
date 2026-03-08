package com.aiops.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aiops.llm")
public class LlmProperties {

    private boolean openAiEnabled;
    private boolean anthropicEnabled;
    private boolean ollamaEnabled = true;

    public boolean isOpenAiEnabled() {
        return openAiEnabled;
    }

    public void setOpenAiEnabled(boolean openAiEnabled) {
        this.openAiEnabled = openAiEnabled;
    }

    public boolean isAnthropicEnabled() {
        return anthropicEnabled;
    }

    public void setAnthropicEnabled(boolean anthropicEnabled) {
        this.anthropicEnabled = anthropicEnabled;
    }

    public boolean isOllamaEnabled() {
        return ollamaEnabled;
    }

    public void setOllamaEnabled(boolean ollamaEnabled) {
        this.ollamaEnabled = ollamaEnabled;
    }
}
