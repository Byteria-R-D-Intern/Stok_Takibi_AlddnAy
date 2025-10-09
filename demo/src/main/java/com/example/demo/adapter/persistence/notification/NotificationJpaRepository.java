package com.example.demo.adapter.persistence.notification;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.model.Notification;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, boolean isRead);

    @Transactional
    @Modifying
    @Query("update Notification n set n.isRead = true, n.readAt = CURRENT_TIMESTAMP where n.id = ?1 and n.userId = ?2")
    int markRead(Long id, Long userId);

    @Transactional
    @Modifying
    @Query("update Notification n set n.isRead = true, n.readAt = CURRENT_TIMESTAMP where n.userId = ?1 and n.isRead = false")
    int markAllRead(Long userId);

    @Transactional
    void deleteByIdAndUserId(Long id, Long userId);
}


