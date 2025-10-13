package com.example.demo.domain.port;


import java.util.List;

import com.example.demo.domain.model.Notification;

public interface NotificationRepository {
    Notification save(Notification notification);
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, int limit);
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, boolean isRead, int limit);
    void markRead(Long id, Long userId);
    int markAllRead(Long userId);
    void deleteByIdAndUserId(Long id, Long userId);
}




