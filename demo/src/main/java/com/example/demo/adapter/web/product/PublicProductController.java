package com.example.demo.adapter.web.product;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.usecase.ProductUseCase;
import com.example.demo.domain.model.Product;

@RestController
@RequestMapping("/api/products")
public class PublicProductController {

    private final ProductUseCase productUseCase;

    public PublicProductController(ProductUseCase productUseCase) {
        this.productUseCase = productUseCase;
    }

    @GetMapping
    public ResponseEntity<List<Product>> list(
            @RequestParam(value = "query", required = false) String q,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
            @RequestParam(value = "sort", required = false, defaultValue = "createdAt,desc") String sort
    ) {
        // Not: UseCase katmanında Page desteği yok; şimdilik in-memory slice yerine tüm listeyi dönüyoruz.
        if (q == null || q.isBlank()) return ResponseEntity.ok(productUseCase.listAll());
        return ResponseEntity.ok(productUseCase.searchByName(q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return productUseCase.getById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}


