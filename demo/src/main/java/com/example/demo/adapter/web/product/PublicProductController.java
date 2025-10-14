package com.example.demo.adapter.web.product;

import java.util.List;

import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.usecase.ProductUseCase;
import com.example.demo.domain.model.Product;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Ürün listeleme ve görüntüleme")
public class PublicProductController {

    private final ProductUseCase productUseCase;

    public PublicProductController(ProductUseCase productUseCase) {
        this.productUseCase = productUseCase;
    }

    @GetMapping
    @Operation(summary = "Ürünleri listele", description = "İsimde arama ile ürün listesini döner")
    public ResponseEntity<List<Product>> list(
            @Parameter(description = "İsimde arama ifadesi") @RequestParam(value = "query", required = false) 
            String q,
            @Parameter(description = "Sayfa numarası") @RequestParam(value = "page", required = false, defaultValue = "0") 
            int page,
            @Parameter(description = "Sayfa boyutu") @RequestParam(value = "size", required = false, defaultValue = "20") 
            int size,
            @Parameter(description = "Sıralama (örn: createdAt,desc)") @RequestParam(value = "sort", required = false, defaultValue = "createdAt,desc")
            String sort
    ) {

        if (q == null || q.isBlank()) return ResponseEntity.ok(productUseCase.listAll());
        return ResponseEntity.ok(productUseCase.searchByName(q));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ürün detayı", description = "ID ile ürün detayını döner")
    public ResponseEntity<Product> getById(@Parameter(description = "Ürün kimliği") @PathVariable Long id) {
        return productUseCase.getById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}


