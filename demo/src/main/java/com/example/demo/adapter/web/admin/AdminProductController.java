package com.example.demo.adapter.web.admin;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.demo.application.usecase.ProductUseCase;
import com.example.demo.application.usecase.AuditLogUseCase;
import com.example.demo.domain.model.Product;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/admin/products")
@SecurityRequirement(name = "bearerAuth")
public class AdminProductController {

    private final ProductUseCase productUseCase;
    private final AuditLogUseCase auditLogUseCase;

    public AdminProductController(ProductUseCase productUseCase, AuditLogUseCase auditLogUseCase) {
        this.productUseCase = productUseCase;
        this.auditLogUseCase = auditLogUseCase;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CreateProductResponse> create(Authentication auth, @RequestBody CreateProductRequest req) {
        Long id = productUseCase.create(req.name, req.price, req.stock);
        Long actorId = null;
        try { actorId = Long.valueOf(auth.getName()); } catch (Exception ignored) {}
        auditLogUseCase.log(actorId, "product", id, "create", "product created", null);
        return ResponseEntity.ok(new CreateProductResponse(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Product>> listAll() {
        return ResponseEntity.ok(productUseCase.listAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return productUseCase.getById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(Authentication auth, @PathVariable Long id, @RequestBody UpdateProductRequest req) {
        productUseCase.update(id, req.name, req.price, req.stock);
        Long actorId = null;
        try { actorId = Long.valueOf(auth.getName()); } catch (Exception ignored) {}
        auditLogUseCase.log(actorId, "product", id, "update", "product updated", null);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        productUseCase.delete(id);
        Long actorId = null;
        try { actorId = Long.valueOf(auth.getName()); } catch (Exception ignored) {}
        auditLogUseCase.log(actorId, "product", id, "delete", "product deleted", null);
        return ResponseEntity.noContent().build();
    }

    public static class CreateProductRequest {
        @NotBlank public String name;
        @NotNull @Min(0) public Integer stock;
        @NotNull public BigDecimal price;
    }

    public static class UpdateProductRequest {
        public String name;
        public Integer stock;
        public BigDecimal price;
    }

    public static class CreateProductResponse {
        public final Long id;
        public CreateProductResponse(Long id) { this.id = id; }
    }
}


