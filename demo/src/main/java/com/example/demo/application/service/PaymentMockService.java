package com.example.demo.application.service;


import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.model.Payment;
import com.example.demo.domain.model.PaymentMethod;
import com.example.demo.domain.model.PaymentStatus;
import com.example.demo.domain.port.PaymentRepository;

@Service
public class PaymentMockService {

    private final PaymentRepository paymentRepository;

    public PaymentMockService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment create(Long orderId, Long userId, BigDecimal amount, String currency,
                          PaymentMethod method, String cardLast4, String idempotencyKey, String metadata) {
        Payment p = new Payment();
        p.setOrderId(orderId);
        p.setUserId(userId);
        p.setAmount(amount);
        if (currency != null) p.setCurrency(currency);
        if (method != null) p.setMethod(method); else p.setMethod(PaymentMethod.CARD);
        p.setCardLast4(cardLast4);
        p.setIdempotencyKey(idempotencyKey);
        p.setMetadata(metadata);
        p.setStatus(shouldChallenge(orderId, amount, metadata) ? PaymentStatus.REQUIRES_ACTION : PaymentStatus.SUCCEEDED);
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        return paymentRepository.save(p);
    }

    @Transactional
    public Payment confirm(Long paymentId) {
        Payment p = paymentRepository.findById(paymentId).orElseThrow(java.util.NoSuchElementException::new);
        if (p.getStatus() == PaymentStatus.REQUIRES_ACTION) {
            p.setStatus(PaymentStatus.SUCCEEDED);
            p.setUpdatedAt(Instant.now());
            return paymentRepository.save(p);
        }
        return p;
    }

    @Transactional
    public Payment fail(Long paymentId) {
        Payment p = paymentRepository.findById(paymentId).orElseThrow(java.util.NoSuchElementException::new);
        p.setStatus(PaymentStatus.FAILED);
        p.setUpdatedAt(Instant.now());
        return paymentRepository.save(p);
    }

    private boolean shouldChallenge(Long orderId, BigDecimal amount, String metadata) {
        // Simple rule: if amount cents ends with 99, require action
        return amount != null && amount.scale() >= 2 && amount.remainder(new BigDecimal("1")).movePointRight(2).intValue() % 100 == 99;
    }
}


