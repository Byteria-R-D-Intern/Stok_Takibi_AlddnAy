package com.example.demo.adapter.web.notification;


import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.service.NotificationService;
import com.example.demo.domain.model.Notification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Kullanıcı bildirimleri")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Bildirimleri listele")
    public ResponseEntity<List<Notification>> list(
            Principal principal,
            @RequestParam(name = "unreadOnly", defaultValue = "true") boolean unreadOnly,
            @RequestParam(name = "limit", defaultValue = "50") int limit) {
        Long userId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(service.list(userId, unreadOnly, Math.max(1, Math.min(limit, 200))));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Tek bildirimi okundu işaretle")
    public ResponseEntity<Void> markRead(Principal principal, @PathVariable Long id) {
        Long userId = Long.valueOf(principal.getName());
        service.markRead(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Tüm bildirimleri okundu işaretle")
    public ResponseEntity<Integer> markAllRead(Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        int updated = service.markAllRead(userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Bildirimi sil")
    public ResponseEntity<Void> delete(Principal principal, @PathVariable Long id) {
        Long userId = Long.valueOf(principal.getName());
        service.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}



