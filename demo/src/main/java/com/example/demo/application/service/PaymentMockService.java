package com.example.demo.application.service;


import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
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
    private final NotificationService notificationService;

    public PaymentMockService(PaymentRepository paymentRepository,
                              OrderRepository orderRepository,
                              OrderUseCase orderUseCase,
                              TokenizationService tokenizationService,
                              NotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.orderUseCase = orderUseCase;
        this.tokenizationService = tokenizationService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Payment create(Long orderId, Long userId, BigDecimal amount, String currency,
                          PaymentMethod method, /*String idempotencyKey,*/ String metadata,
                          String paymentToken) {
                            
        Order order = orderRepository.findById(orderId).orElseThrow(java.util.NoSuchElementException::new);
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException("order_not_payable");
        }
       
        if (order.getUser() == null || order.getUser().getId() == null || userId == null || !order.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden: cannot pay others' order");
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



        if (p.getMethod() == PaymentMethod.CARD) {
            if (paymentToken == null || paymentToken.isBlank()) {
                throw new IllegalArgumentException("payment_token_required");
            }

            try {
                var dt = tokenizationService.detokenize(paymentToken);
                String pan = dt.pan();
                p.setCardLast4(pan.substring(Math.max(0, pan.length() - 4)));
                //  token detokenize edildikten sonra iptal edilir
                tokenizationService.revoke(paymentToken);
            } catch (IllegalStateException ex) {
             
                if ("token_expired".equals(ex.getMessage())) {
                    throw new IllegalArgumentException("token_expired");
                }
                throw new IllegalArgumentException("invalid_payment_token");
            } catch (IllegalArgumentException ex) {
                
                throw new IllegalArgumentException("invalid_payment_token");
            }
        } else {
            p.setCardLast4(null);
        }
        //p.setIdempotencyKey(idempotencyKey);
        p.setMetadata(metadata);
        p.setStatus(PaymentStatus.SUCCEEDED);
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        p = paymentRepository.save(p);
        if (p.getStatus() == PaymentStatus.SUCCEEDED) {
            orderUseCase.updateStatus(orderId, OrderStatus.PAID);
            // bildirim
            notificationService.create(userId, "PAYMENT_SUCCEEDED", "Ödeme başarılı", "Sipariş #" + orderId + " ödendi.", null);
        }
        return p;
    }

 
}


