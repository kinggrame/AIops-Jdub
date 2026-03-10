package com.aiops.agent.memory;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MemoryService {
    private final AgentMemoryRepository memoryRepository;
    private static final int DEFAULT_MAX_MESSAGES = 20;
    private static final int COMPRESSION_THRESHOLD = 50;

    public MemoryService(AgentMemoryRepository memoryRepository) {
        this.memoryRepository = memoryRepository;
    }

    public void saveSession(String sessionId, String userId, List<Message> messages) {
        AgentMemory memory = memoryRepository.findById(sessionId)
            .orElseGet(() -> {
                AgentMemory m = new AgentMemory();
                m.setId(sessionId);
                m.setSessionId(sessionId);
                m.setUserId(userId);
                return m;
            });

        String compressed = compressMessages(messages);
        memory.setCompressedHistory(compressed);
        memory.setMessageCount(messages.size());
        memory.setCompressionLevel(calculateCompressionLevel(messages.size()));
        memory.setLastUpdated(LocalDateTime.now());

        memoryRepository.save(memory);
    }

    public String loadSession(String sessionId) {
        return memoryRepository.findById(sessionId)
            .map(AgentMemory::getCompressedHistory)
            .orElse("");
    }

    public List<AgentMemory> loadUserMemories(String userId) {
        return memoryRepository.findByUserIdOrderByLastUpdatedDesc(userId);
    }

    private String compressMessages(List<Message> messages) {
        if (messages.size() <= DEFAULT_MAX_MESSAGES) {
            return messagesToString(messages);
        }

        List<Message> recent = messages.subList(messages.size() - DEFAULT_MAX_MESSAGES, messages.size());
        StringBuilder sb = new StringBuilder();
        
        sb.append("[早期摘要] ");
        sb.append(summarizeMessages(messages.subList(0, messages.size() - DEFAULT_MAX_MESSAGES)));
        sb.append("\n\n[最近对话]\n");
        sb.append(messagesToString(recent));
        
        return sb.toString();
    }

    private String summarizeMessages(List<Message> messages) {
        if (messages.isEmpty()) return "";
        
        Set<String> nodes = messages.stream()
            .map(m -> m.node)
            .collect(Collectors.toSet());
        
        long toolCalls = messages.stream()
            .filter(m -> m.content.contains("[TOOL:"))
            .count();
        
        return String.format("经过%d个节点处理, %d次工具调用", nodes.size(), toolCalls);
    }

    private String messagesToString(List<Message> messages) {
        StringBuilder sb = new StringBuilder();
        for (Message msg : messages) {
            sb.append(msg.node).append(": ").append(msg.content).append("\n");
        }
        return sb.toString();
    }

    private int calculateCompressionLevel(int messageCount) {
        if (messageCount < COMPRESSION_THRESHOLD) return 1;
        if (messageCount < 100) return 2;
        return 3;
    }

    public void deleteSession(String sessionId) {
        memoryRepository.deleteById(sessionId);
    }

    public void cleanupOldMemories(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        List<AgentMemory> oldMemories = memoryRepository.findAll().stream()
            .filter(m -> m.getLastUpdated().isBefore(cutoff))
            .collect(Collectors.toList());
        
        memoryRepository.deleteAll(oldMemories);
    }

    public static class Message {
        public String role;
        public String content;
        public String node;
        public long timestamp;

        public Message() {}

        public Message(String role, String content, String node) {
            this.role = role;
            this.content = content;
            this.node = node;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
