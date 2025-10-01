package com.example.demo.adapter.web.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.demo.application.usecase.OrderUseCase;
import com.example.demo.domain.model.OrderStatus;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderUseCase orderUseCase;

    public AdminOrderController(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<com.example.demo.domain.model.Order> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderUseCase.getUserOrder(0L, id));
        } catch (java.util.NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody StatusRequest req) {
        orderUseCase.updateStatus(id, OrderStatus.valueOf(req.status));
        return ResponseEntity.noContent().build();
    }

    public static class StatusRequest { public String status; }
}


