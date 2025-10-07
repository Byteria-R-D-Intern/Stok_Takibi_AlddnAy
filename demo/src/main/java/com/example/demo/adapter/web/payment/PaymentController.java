package com.example.demo.adapter.web.payment;


import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    public ResponseEntity<Payment> create(@RequestBody CreatePaymentRequest req,
                                          @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        Payment p = paymentService.create(
            req.orderId,
            req.userId,
            req.amount,
            req.currency,
            req.method,
            req.cardLast4,
            idempotencyKey,
            req.metadata
        );
        return new ResponseEntity<>(p, p.getStatus().name().equals("SUCCEEDED") ? HttpStatus.CREATED : HttpStatus.ACCEPTED);
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Challenge sonrası onayla")
    public ResponseEntity<Payment> confirm(@PathVariable Long id) {
        Payment p = paymentService.confirm(id);
        return ResponseEntity.ok(p);
    }

    @PostMapping("/{id}/fail")
    @Operation(summary = "Challenge başarısız")
    public ResponseEntity<Payment> fail(@PathVariable Long id) {
        Payment p = paymentService.fail(id);
        return ResponseEntity.ok(p);
    }

    public static class CreatePaymentRequest {
        @NotNull public Long orderId;
        public Long userId;
        @NotNull public BigDecimal amount;
        public String currency;
        public PaymentMethod method;
        public String cardLast4;
        public String metadata;
    }
}


