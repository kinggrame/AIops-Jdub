package com.aiops.llm.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class LlmConfig {

    private final Map<String, ChatLanguageModel> chatModels = new ConcurrentHashMap<>();
    private final Map<String, EmbeddingModel> embeddingModels = new ConcurrentHashMap<>();
    private ChatLanguageModel defaultChatModel;
    private EmbeddingModel defaultEmbeddingModel;

    @Bean
    @ConfigurationProperties(prefix = "llm.openai")
    public OpenAiProperties openAiProperties() {
        return new OpenAiProperties();
    }

    @Bean
    public ChatLanguageModel chatLanguageModel(OpenAiProperties props) {
        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(props.getApiKey())
                .modelName(props.getModel())
                .temperature(props.getTemperature())
                .maxTokens(props.getMaxTokens())
                .timeout(Duration.ofSeconds(props.getTimeout()))
                .build();
        defaultChatModel = model;
        return model;
    }

    @Bean
    public EmbeddingModel embeddingModel(OpenAiProperties props) {
        return OpenAiEmbeddingModel.builder()
                .apiKey(props.getApiKey())
                .modelName(props.getEmbeddingModel())
                .dimensions(props.getDimensions())
                .timeout(Duration.ofSeconds(props.getTimeout()))
                .build();
    }

    public static class OpenAiProperties {
        private String apiKey = "dummy-key";
        private String model = "gpt-4";
        private String embeddingModel = "text-embedding-3-small";
        private double temperature = 0.7;
        private int maxTokens = 2000;
        private int dimensions = 1536;
        private int timeout = 60;

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getEmbeddingModel() { return embeddingModel; }
        public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
        public int getDimensions() { return dimensions; }
        public void setDimensions(int dimensions) { this.dimensions = dimensions; }
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
    }
}
