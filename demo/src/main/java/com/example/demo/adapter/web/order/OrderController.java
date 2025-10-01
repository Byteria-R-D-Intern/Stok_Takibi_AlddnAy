package com.example.demo.adapter.web.order;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.usecase.OrderUseCase;
import com.example.demo.application.usecase.OrderUseCase.CheckoutCommand;
import com.example.demo.application.usecase.OrderUseCase.CheckoutResult;
import com.example.demo.application.usecase.OrderUseCase.CheckoutItem;
import com.example.demo.application.usecase.AuditLogUseCase;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderUseCase orderUseCase;
    private final AuditLogUseCase auditLogUseCase;

    public OrderController(OrderUseCase orderUseCase, AuditLogUseCase auditLogUseCase) {
        this.orderUseCase = orderUseCase;
        this.auditLogUseCase = auditLogUseCase;
    }

    @GetMapping
    public ResponseEntity<java.util.List<com.example.demo.domain.model.Order>> myOrders(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(orderUseCase.listUserOrders(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<com.example.demo.domain.model.Order> myOrderById(Authentication auth, @org.springframework.web.bind.annotation.PathVariable Long id) {
        Long userId = Long.valueOf(auth.getName());
        try {
            return ResponseEntity.ok(orderUseCase.getUserOrder(userId, id));
        } catch (java.util.NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(Authentication auth, @org.springframework.web.bind.annotation.PathVariable Long id) {
        Long userId = Long.valueOf(auth.getName());
        orderUseCase.cancelUserOrder(userId, id);
        auditLogUseCase.log(userId, "order", id, "cancel", "order cancelled by user", null);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(Authentication auth, @RequestBody CheckoutRequest req) {
        Long actorId = null;
        try { actorId = Long.valueOf(auth.getName()); } catch (Exception ignored) {}

        if (req == null) throw new IllegalArgumentException("request is null");
        if (req.items == null || req.items.isEmpty()) throw new IllegalArgumentException("items must not be empty");
        if (actorId == null) throw new IllegalArgumentException("user not authenticated");

        CheckoutCommand cmd = new CheckoutCommand(
            actorId,
            req.shippingName,
            req.shippingPhone,
            req.shippingAddress,
            req.items.stream().map(i -> new CheckoutItem(i.productId, i.quantity)).toArray(CheckoutItem[]::new)
        );
        CheckoutResult result = orderUseCase.directCheckout(cmd);
        auditLogUseCase.log(actorId, "order", result.orderId, "checkout", "order created via checkout", null);
        return ResponseEntity.ok(new CheckoutResponse(result.orderId, result.total));
    }

    public static class CheckoutRequest {
        public Long userId;
        public String shippingName;
        public String shippingPhone;
        public String shippingAddress;
        public java.util.List<Item> items;
    }

    public static class Item {
        public Long productId;
        public Integer quantity;
    }

    public static class CheckoutResponse {
        public final Long orderId;
        public final java.math.BigDecimal total;
        public CheckoutResponse(Long orderId, java.math.BigDecimal total) {
            this.orderId = orderId;
            this.total = total;
        }
    }
}


