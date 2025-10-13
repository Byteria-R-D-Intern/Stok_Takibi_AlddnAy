package com.example.demo.adapter.web.admin;

import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.demo.application.usecase.OrderUseCase;
import com.example.demo.domain.model.OrderStatus;

@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "Admin Orders", description = "Yönetici sipariş işlemleri")
public class AdminOrderController {

    private final OrderUseCase orderUseCase;

    public AdminOrderController(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Sipariş getir", description = "ID ile siparişi döner")
    public ResponseEntity<com.example.demo.domain.model.Order> getById(@Parameter(description = "Sipariş kimliği") @PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderUseCase.getByIdAdmin(id));
        } catch (java.util.NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    @Operation(summary = "Durum güncelle", description = "Sipariş durumunu değiştir")
    public ResponseEntity<Void> updateStatus(@Parameter(description = "Sipariş kimliği") @PathVariable Long id, @RequestBody StatusRequest req) {
        orderUseCase.updateStatus(id, OrderStatus.valueOf(req.status));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    @Operation(summary = "Satış istatistikleri", description = "Verilen tarih aralığı için özet metrikleri döner")
    public ResponseEntity<OrderUseCase.SalesStats> stats(
            @Parameter(description = "Başlangıç zamanı (ISO)") @RequestParam("from") java.time.Instant from,
            @Parameter(description = "Bitiş zamanı (ISO)") @RequestParam("to") java.time.Instant to) {
        return ResponseEntity.ok(orderUseCase.stats(from, to));
    }

    public static class StatusRequest { public String status; }
}


