

package com.example.demo.domain.port;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.example.demo.domain.model.Payment;
import com.example.demo.domain.model.PaymentStatus;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(Long id);
    List<Payment> findByOrderId(Long orderId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByCreatedAtBetween(Instant from, Instant to);
}


