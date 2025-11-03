package com.example.demo.adapter.persistence.payment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.model.SavedPaymentMethod;

// NEDEN: Kalıcı yöntemler için Spring Data JPA repo
public interface SavedPaymentMethodJpaRepository extends JpaRepository<SavedPaymentMethod, Long> {
    List<SavedPaymentMethod> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<SavedPaymentMethod> findByIdAndUserId(Long id, Long userId);

    @Transactional
    @Modifying
    @Query("update SavedPaymentMethod m set m.active=false where m.id=?1 and m.userId=?2")
    int deactivate(Long id, Long userId);

    @Transactional
    void deleteByIdAndUserId(Long id, Long userId);
}


