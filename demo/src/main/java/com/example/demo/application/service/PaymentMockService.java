package com.example.demo.application.service;


import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.model.Payment;
import com.example.demo.domain.model.PaymentMethod;
import com.example.demo.domain.model.PaymentStatus;
import com.example.demo.domain.port.PaymentRepository;
import com.example.demo.domain.port.OrderRepository;
import com.example.demo.domain.model.Order;
import com.example.demo.domain.model.OrderStatus;
import com.example.demo.application.usecase.OrderUseCase;
import com.example.demo.application.service.tokenization.TokenizationService;

@Service
public class PaymentMockService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderUseCase orderUseCase;
    private final TokenizationService tokenizationService;

    public PaymentMockService(PaymentRepository paymentRepository,
                              OrderRepository orderRepository,
                              OrderUseCase orderUseCase,
                              TokenizationService tokenizationService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.orderUseCase = orderUseCase;
        this.tokenizationService = tokenizationService;
    }

    @Transactional
    public Payment create(Long orderId, Long userId, BigDecimal amount, String currency,
                          PaymentMethod method, String cardLast4, String idempotencyKey, String metadata,
                          String paymentToken) {
        // Validate order exists and is payable
        Order order = orderRepository.findById(orderId).orElseThrow(java.util.NoSuchElementException::new);
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException("order_not_payable");
        }
        if (order.getTotal() == null || amount == null || order.getTotal().compareTo(amount) != 0) {
            throw new IllegalArgumentException("amount_mismatch");
        }
        Payment p = new Payment();
        p.setOrderId(orderId);
        p.setUserId(userId != null ? userId : (order.getUser() != null ? order.getUser().getId() : null));
        p.setAmount(amount);
        if (currency != null) p.setCurrency(currency);
        if (method != null) p.setMethod(method); else p.setMethod(PaymentMethod.CARD);
        // If token provided, detokenize and derive last4; do not persist PAN
        if (paymentToken != null && !paymentToken.isBlank()) {
            try {
                var dt = tokenizationService.detokenize(paymentToken);
                String pan = dt.pan();
                p.setCardLast4(pan.substring(Math.max(0, pan.length() - 4)));
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("invalid_payment_token");
            }
        } else {
            p.setCardLast4(cardLast4);
        }
        p.setIdempotencyKey(idempotencyKey);
        p.setMetadata(metadata);
        p.setStatus(shouldChallenge(orderId, amount, metadata) ? PaymentStatus.REQUIRES_ACTION : PaymentStatus.SUCCEEDED);
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        p = paymentRepository.save(p);
        if (p.getStatus() == PaymentStatus.SUCCEEDED) {
            orderUseCase.updateStatus(orderId, OrderStatus.PAID);
        }
        return p;
    }

    @Transactional
    public Payment confirm(Long paymentId) {
        Payment p = paymentRepository.findById(paymentId).orElseThrow(java.util.NoSuchElementException::new);
        if (p.getStatus() == PaymentStatus.REQUIRES_ACTION) {
            p.setStatus(PaymentStatus.SUCCEEDED);
            p.setUpdatedAt(Instant.now());
            p = paymentRepository.save(p);
            // On success, mark order as PAID
            if (p.getOrderId() != null) {
                orderUseCase.updateStatus(p.getOrderId(), OrderStatus.PAID);
            }
            return p;
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


