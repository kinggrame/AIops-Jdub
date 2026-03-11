package com.aiops.notification.service;

import com.aiops.notification.model.NotificationChannel;
import com.aiops.notification.repository.NotificationChannelRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class NotificationService {
    private final NotificationChannelRepository channelRepository;

    public NotificationService(NotificationChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    public List<NotificationChannel> findAll() { return channelRepository.findAll(); }
    public List<NotificationChannel> findEnabled() { return channelRepository.findByEnabledTrue(); }
    public NotificationChannel findById(Long id) { return channelRepository.findById(id).orElse(null); }
    public NotificationChannel save(NotificationChannel channel) {
        channel.setUpdatedAt(java.time.LocalDateTime.now());
        return channelRepository.save(channel);
    }
    public void delete(Long id) { channelRepository.deleteById(id); }

    public Map<String, Object> send(Long channelId, String title, String content) {
        NotificationChannel channel = findById(channelId);
        if (channel == null || !channel.getEnabled()) {
            throw new RuntimeException("Channel not found or disabled");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("channel", channel.getName());
        result.put("type", channel.getType());
        result.put("title", title);
        result.put("message", "Notification sent (simulated)");
        return result;
    }

    public void sendToAll(String title, String content) {
        List<NotificationChannel> channels = findEnabled();
        for (NotificationChannel channel : channels) {
            try {
                send(channel.getId(), title, content);
            } catch (Exception e) {
                channel.setStatus(NotificationChannel.ChannelStatus.ERROR);
                channelRepository.save(channel);
            }
        }
    }
}
