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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/admin/products")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Products", description = "Yönetici ürün işlemleri")
public class AdminProductController {

    private final ProductUseCase productUseCase;
    private final AuditLogUseCase auditLogUseCase;

    public AdminProductController(ProductUseCase productUseCase, AuditLogUseCase auditLogUseCase) {
        this.productUseCase = productUseCase;
        this.auditLogUseCase = auditLogUseCase;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Ürün oluştur", description = "Yeni ürün ekler")
    public ResponseEntity<CreateProductResponse> create(Authentication auth, @RequestBody CreateProductRequest req) {
        Long id = productUseCase.create(req.name, req.price, req.stock);
        Long actorId = null;
        try { actorId = Long.valueOf(auth.getName()); } catch (Exception ignored) {}
        auditLogUseCase.log(actorId, "product", id, "create", "product created", null);
        return ResponseEntity.ok(new CreateProductResponse(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Ürünleri listele", description = "Tüm ürünleri döner")
    public ResponseEntity<List<Product>> listAll() {
        return ResponseEntity.ok(productUseCase.listAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Ürün getir", description = "ID ile ürün detayını döner")
    public ResponseEntity<Product> getById(@Parameter(description = "Ürün kimliği") @PathVariable Long id) {
        return productUseCase.getById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Ürün güncelle", description = "Ürün bilgilerini günceller")
    public ResponseEntity<Void> update(Authentication auth, @Parameter(description = "Ürün kimliği") @PathVariable Long id, @RequestBody UpdateProductRequest req) {
        productUseCase.update(id, req.name, req.price, req.stock);
        Long actorId = null;
        try { actorId = Long.valueOf(auth.getName()); } catch (Exception ignored) {}
        auditLogUseCase.log(actorId, "product", id, "update", "product updated", null);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Ürün sil", description = "Ürünü siler")
    public ResponseEntity<Void> delete(Authentication auth, @Parameter(description = "Ürün kimliği") @PathVariable Long id) {
        productUseCase.delete(id);
        Long actorId = null;
        try { actorId = Long.valueOf(auth.getName()); } catch (Exception ignored) {}
        auditLogUseCase.log(actorId, "product", id, "delete", "product deleted", null);
        return ResponseEntity.noContent().build();
    }

    public static class CreateProductRequest {
        @Schema(description = "Ürün adı")
        @NotBlank public String name;
        @Schema(description = "Stok")
        @NotNull @Min(0) public Integer stock;
        @Schema(description = "Fiyat")
        @NotNull public BigDecimal price;
    }

    public static class UpdateProductRequest {
        @Schema(description = "Ürün adı")
        public String name;
        @Schema(description = "Stok")
        public Integer stock;
        @Schema(description = "Fiyat")
        public BigDecimal price;
    }

    public static class CreateProductResponse {
        public final Long id;
        public CreateProductResponse(Long id) { this.id = id; }
    }
}


