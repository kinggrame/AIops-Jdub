package com.aiops.notification.repository;

import com.aiops.notification.model.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, Long> {
    List<NotificationChannel> findByEnabledTrue();
    List<NotificationChannel> findByType(NotificationChannel.ChannelType type);
}
