package com.aiops.llm.service;

import com.aiops.llm.model.LlmProvider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.AiMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LlmClientService {

    private final Map<String, ChatLanguageModel> chatModels = new ConcurrentHashMap<>();
    private final Map<String, EmbeddingModel> embeddingModels = new ConcurrentHashMap<>();
    private final LlmService llmService;
    private ChatLanguageModel defaultChatModel;
    private EmbeddingModel defaultEmbeddingModel;

    public LlmClientService(LlmService llmService) {
        this.llmService = llmService;
    }

    public void registerChatModel(String providerId, ChatLanguageModel model) {
        chatModels.put(providerId, model);
    }

    public void registerEmbeddingModel(String providerId, EmbeddingModel model) {
        embeddingModels.put(providerId, model);
    }

    public void setDefaultChatModel(ChatLanguageModel model) {
        this.defaultChatModel = model;
    }

    public void setDefaultEmbeddingModel(EmbeddingModel model) {
        this.defaultEmbeddingModel = model;
    }

    public ChatLanguageModel getChatModel(String providerId) {
        if (providerId == null || providerId.isEmpty()) {
            return defaultChatModel;
        }
        return chatModels.getOrDefault(providerId, defaultChatModel);
    }

    public EmbeddingModel getEmbeddingModel(String providerId) {
        if (providerId == null || providerId.isEmpty()) {
            return defaultEmbeddingModel;
        }
        return embeddingModels.getOrDefault(providerId, defaultEmbeddingModel);
    }

    public String chat(String message, String providerId) {
        ChatLanguageModel model = getChatModel(providerId);
        if (model == null) {
            throw new RuntimeException("No chat model available");
        }
        AiMessage response = model.chat(dev.langchain4j.service.UserMessage.userMessage(message));
        return response.singleText();
    }

    public void initializeFromProviders() {
        List<LlmProvider> chatProviders = llmService.findChatProviders();
        List<LlmProvider> embedProviders = llmService.findEmbeddingProviders();

        if (!chatProviders.isEmpty()) {
            LlmProvider defaultProvider = chatProviders.get(0);
            System.out.println("Default chat provider: " + defaultProvider.getName());
        }
    }
}
