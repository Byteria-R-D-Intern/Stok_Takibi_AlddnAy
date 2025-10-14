package com.example.demo.adapter.web.order;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.usecase.OrderUseCase;
import com.example.demo.application.usecase.OrderUseCase.CheckoutCommand;
import com.example.demo.application.usecase.OrderUseCase.CheckoutResult;
import com.example.demo.application.usecase.OrderUseCase.CheckoutItem;
import com.example.demo.application.usecase.AuditLogUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/orders")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Orders", description = "Kullanıcı sipariş işlemleri")
public class OrderController {

    private final OrderUseCase orderUseCase;
    private final AuditLogUseCase auditLogUseCase;

    public OrderController(OrderUseCase orderUseCase, AuditLogUseCase auditLogUseCase) {
        this.orderUseCase = orderUseCase;
        this.auditLogUseCase = auditLogUseCase;
    }

    @GetMapping
    @Operation(summary = "Kendi siparişlerini listele", description = "Oturumdaki kullanıcının tüm siparişlerini döner")
    public ResponseEntity<java.util.List<com.example.demo.domain.model.Order>> myOrders(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(orderUseCase.listUserOrders(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Siparişi görüntüle", description = "Kullanıcı yalnızca kendi siparişini görebilir")
    public ResponseEntity<com.example.demo.domain.model.Order> myOrderById(Authentication auth, @Parameter(description = "Sipariş kimliği") @org.springframework.web.bind.annotation.PathVariable Long id) {
        Long userId = Long.valueOf(auth.getName());
        try {
            return ResponseEntity.ok(orderUseCase.getUserOrder(userId, id));
        } catch (java.util.NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Siparişi iptal et", description = "Kargolanmadan önce kullanıcı kendi siparişini iptal edebilir")
    public ResponseEntity<Void> cancel(Authentication auth, @Parameter(description = "Sipariş kimliği") @org.springframework.web.bind.annotation.PathVariable Long id) {
        Long userId = Long.valueOf(auth.getName());
        orderUseCase.cancelUserOrder(userId, id);
        auditLogUseCase.log(userId, "order", id, "cancel", "order cancelled by user", null);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/address")
    @Operation(summary = "Gönderim bilgisi güncelle", description = "Kargolanmadan önce isim/telefon/adres güncellenebilir")
    public ResponseEntity<Void> updateAddress(Authentication auth, @Parameter(description = "Sipariş kimliği") @org.springframework.web.bind.annotation.PathVariable Long id, @Valid @RequestBody UpdateAddressRequest req) {
        Long userId = Long.valueOf(auth.getName());
        orderUseCase.updateShippingInfo(userId, id, req.shippingName, req.shippingPhone, req.shippingAddress);
        auditLogUseCase.log(userId, "order", id, "update_address", "shipping info updated", null);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/checkout")
    @Operation(summary = "Checkout", description = "Sepetteki ürünlerle sipariş oluşturur")
    public ResponseEntity<CheckoutResponse> checkout(Authentication auth, @Valid @RequestBody CheckoutRequest req) {
        Long actorId = Long.valueOf(auth.getName());

        CheckoutCommand cmd = new CheckoutCommand(
            actorId,
            req.shippingName,
            req.shippingPhone,
            req.shippingAddress,
            req.items.stream().map(i -> new CheckoutItem(i.productId, i.quantity)).toArray(CheckoutItem[]::new)
        );
        CheckoutResult result = orderUseCase.directCheckout(cmd);

        auditLogUseCase.log(actorId, "order", result.orderId, "checkout", "order created via checkout", null);
        // Ödeme idempotency anahtarı pay:{orderId}:{UUID} şeklinde oluşturuluyor
        String paymentIdempotencyKey = "pay:" + result.orderId + ":" + java.util.UUID.randomUUID().toString();
        return ResponseEntity.ok(new CheckoutResponse(result.orderId, result.total, paymentIdempotencyKey));
    }

    public static class CheckoutRequest {
        @Schema(description = "Alıcı adı")
        @NotBlank(message = "shippingName is required") 
        public String shippingName;

        @Schema(description = "Alıcı telefon")
        public String shippingPhone;

        @Schema(description = "Teslimat adresi")
        @NotBlank(message = "shippingAddress is required") 
        public String shippingAddress;

        @Schema(description = "Sipariş kalemleri")
        @NotNull @Size(min = 1, message = "items must not be empty")
        public java.util.List<@Valid Item> items;
    }

    public static class Item {
        @Schema(description = "Ürün kimliği")
        @NotNull(message = "productId is required")
        public Long productId;
        @Schema(description = "Adet")
        @NotNull(message = "quantity is required") @Min(value = 1, message = "quantity must be greater than 0") 
        public Integer quantity;
    }

    public static class UpdateAddressRequest {
        @Schema(description = "Alıcı adı")
        @NotBlank public String shippingName;
        @Schema(description = "Alıcı telefon")
        public String shippingPhone;
        @Schema(description = "Teslimat adresi")
        @NotBlank public String shippingAddress;
    }

    public static class CheckoutResponse {
        public final Long orderId;
        public final java.math.BigDecimal total;
        @Schema(description = "Ödeme çağrısında Idempotency-Key header'ında kullanmanız için önerilen anahtar")
        public final String paymentIdempotencyKey;

        public CheckoutResponse(Long orderId, java.math.BigDecimal total, String paymentIdempotencyKey) {
            this.orderId = orderId;
            this.total = total;
            this.paymentIdempotencyKey = paymentIdempotencyKey;
        }
    }
}


