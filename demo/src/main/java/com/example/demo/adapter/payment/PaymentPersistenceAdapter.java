package com.example.demo.adapter.payment;


import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.demo.domain.model.Payment;
import com.example.demo.domain.model.PaymentStatus;
import com.example.demo.domain.port.PaymentRepository;

@Repository
public class PaymentPersistenceAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpa;

    public PaymentPersistenceAdapter(PaymentJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public Payment save(Payment payment) { return jpa.save(payment); }

    @Override
    public Optional<Payment> findById(Long id) { return jpa.findById(id); }

    @Override
    public List<Payment> findByOrderId(Long orderId) { return jpa.findByOrderId(orderId); }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) { return jpa.findByStatus(status); }

    @Override
    public List<Payment> findByCreatedAtBetween(Instant from, Instant to) { return jpa.findByCreatedAtBetween(from, to); }
}

