package com.example.demo.adapter.web.admin;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.usecase.ProductUseCase;
import com.example.demo.application.usecase.AuditLogUseCase;

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

	public static class CreateProductRequest {
		@NotBlank public String name;
		@NotNull @Min(0) public Integer stock;
		@NotNull public BigDecimal price;
	}

	public static class CreateProductResponse {
		public final Long id;
		public CreateProductResponse(Long id) { this.id = id; }
	}
}


