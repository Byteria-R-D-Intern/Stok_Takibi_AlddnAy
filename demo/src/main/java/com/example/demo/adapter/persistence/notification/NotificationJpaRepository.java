package com.example.demo.adapter.persistence.notification;


import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.model.Notification;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, boolean isRead);

    @Transactional
    @Modifying
    @Query("update Notification n set n.isRead = true, n.readAt = :now where n.id = :id and n.userId = :userId")
    int markRead(@Param("id") Long id, @Param("userId") Long userId, @Param("now") Instant now);

    @Transactional
    @Modifying
    @Query("update Notification n set n.isRead = true, n.readAt = :now where n.userId = :userId and n.isRead = false")
    int markAllRead(@Param("userId") Long userId, @Param("now") Instant now);

    @Transactional
    void deleteByIdAndUserId(Long id, Long userId);
}



