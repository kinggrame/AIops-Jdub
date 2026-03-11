package com.aiops.notification.controller;

import com.aiops.notification.model.NotificationChannel;
import com.aiops.notification.service.NotificationService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/channels")
    public List<NotificationChannel> listChannels() { return notificationService.findAll(); }

    @GetMapping("/channels/enabled")
    public List<NotificationChannel> enabledChannels() { return notificationService.findEnabled(); }

    @PostMapping("/channels")
    public NotificationChannel createChannel(@RequestBody NotificationChannel channel) { 
        return notificationService.save(channel); 
    }

    @PutMapping("/channels/{id}")
    public NotificationChannel updateChannel(@PathVariable Long id, @RequestBody NotificationChannel channel) {
        channel.setId(id);
        return notificationService.save(channel);
    }

    @DeleteMapping("/channels/{id}")
    public void deleteChannel(@PathVariable Long id) { notificationService.delete(id); }

    @PostMapping("/send/{channelId}")
    public Map<String, Object> send(@PathVariable Long channelId, @RequestBody Map<String, String> request) {
        return notificationService.send(channelId, request.get("title"), request.get("content"));
    }

    @PostMapping("/send/all")
    public void sendToAll(@RequestBody Map<String, String> request) {
        notificationService.sendToAll(request.get("title"), request.get("content"));
    }
}
