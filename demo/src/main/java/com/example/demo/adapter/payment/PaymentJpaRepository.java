package com.example.demo.adapter.persistence.payment;


import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.model.Payment;
import com.example.demo.domain.model.PaymentStatus;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderId(Long orderId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByCreatedAtBetween(Instant from, Instant to);
}

