package com.example.demo.adapter.persistence.notification;


import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.demo.domain.model.Notification;
import com.example.demo.domain.port.NotificationRepository;

@Repository
public class NotificationPersistenceAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpa;

    public NotificationPersistenceAdapter(NotificationJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public Notification save(Notification notification) { return jpa.save(notification); }

    @Override
    public List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, int limit) {
        List<Notification> all = jpa.findByUserIdOrderByCreatedAtDesc(userId);
        return all.size() > limit ? all.subList(0, limit) : all;
    }

    @Override
    public List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, boolean isRead, int limit) {
        List<Notification> all = jpa.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, isRead);
        return all.size() > limit ? all.subList(0, limit) : all;
    }

    @Override
    public void markRead(Long id, Long userId) { jpa.markRead(id, userId, Instant.now()); }

    @Override
    public int markAllRead(Long userId) { return jpa.markAllRead(userId, Instant.now()); }

    @Override
    public void deleteByIdAndUserId(Long id, Long userId) { jpa.deleteByIdAndUserId(id, userId); }
}



