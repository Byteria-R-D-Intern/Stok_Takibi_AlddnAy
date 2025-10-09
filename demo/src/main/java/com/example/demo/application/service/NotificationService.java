package com.example.demo.application.service;


import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.model.Notification;
import com.example.demo.domain.port.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) { this.repository = repository; }

    public List<Notification> list(Long userId, boolean unreadOnly, int limit) {
        return unreadOnly
            ? repository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false, limit)
            : repository.findByUserIdOrderByCreatedAtDesc(userId, limit);
    }

    @Transactional
    public Notification create(Long userId, String type, String title, String message, String data) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setData(data);
        n.setRead(false);
        n.setCreatedAt(Instant.now());
        return repository.save(n);
    }

    @Transactional
    public void markRead(Long userId, Long notificationId) { repository.markRead(notificationId, userId); }

    @Transactional
    public int markAllRead(Long userId) { return repository.markAllRead(userId); }

    @Transactional
    public void delete(Long userId, Long notificationId) { repository.deleteByIdAndUserId(notificationId, userId); }
}


