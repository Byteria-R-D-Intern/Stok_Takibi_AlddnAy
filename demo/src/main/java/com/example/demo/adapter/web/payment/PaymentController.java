package com.example.demo.adapter.web.payment;


import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.service.PaymentMockService;
import com.example.demo.domain.model.Payment;
import com.example.demo.domain.model.PaymentMethod;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Mock ödeme akışları")
public class PaymentController {

    private final PaymentMockService paymentService;

    public PaymentController(PaymentMockService paymentService) { this.paymentService = paymentService; }

    @PostMapping
    @Operation(summary = "Ödeme oluştur")
    public ResponseEntity<Payment> create(Authentication auth,
                                          @RequestBody CreatePaymentRequest req
                                          /*@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey*/) {
        Long actorId = null;
        try { 
            actorId = Long.valueOf(auth.getName()); } 
            catch (Exception ignored) {}

        if (req.userId != null && actorId != null && !req.userId.equals(actorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden: cannot pay others' order");
        }
        Payment p = paymentService.create(
            req.orderId,
            (actorId != null ? actorId : req.userId),
            req.amount,
            req.currency,
            req.method,
            /*idempotencyKey,*/
            req.metadata,
            req.paymentToken
        );
        return new ResponseEntity<>(p, p.getStatus().name().equals("SUCCEEDED") ? HttpStatus.CREATED : HttpStatus.ACCEPTED);
    }


    public static class CreatePaymentRequest {
        @NotNull public Long orderId;
        public Long userId;
        @NotNull public BigDecimal amount;
        public String currency;
        public PaymentMethod method;
        // cardLast4 is no longer accepted from client; derived from token for CARD
        // public String cardLast4;
        public String metadata;
        public String paymentToken;
    }
}


