package com.example.demo.adapter.web.admin;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.model.AuditLog;
import com.example.demo.adapter.persistence.audit.AuditLogJpaRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/audit")
@Tag(name = "Admin Audit", description = "Yönetici audit log görüntüleme")
public class AdminAuditController {

    private final AuditLogJpaRepository auditRepo;

    public AdminAuditController(AuditLogJpaRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Son loglar", description = "Son N audit log kaydını döner")
    public List<AuditLog> last(@RequestParam(name = "limit", defaultValue = "50") int limit) {
        // Basit yol: tümünü alıp son N'i dön
        // Not: Gerçekte pageable/sort ile yapılmalı
        List<AuditLog> all = auditRepo.findAll();
        int size = all.size();
        int from = Math.max(0, size - Math.max(1, limit));
        return all.subList(from, size);
    }
}


