package com.example.demo.adapter.web.product;

import java.util.List;
import java.util.stream.Collectors;

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
import com.example.demo.adapter.web.product.dto.PublicProductDto;

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
    public ResponseEntity<List<PublicProductDto>> list(
            @Parameter(description = "İsimde arama ifadesi") @RequestParam(value = "query", required = false) 
            String q,
            @Parameter(description = "Sayfa numarası") @RequestParam(value = "page", required = false, defaultValue = "0") 
            int page,
            @Parameter(description = "Sayfa boyutu") @RequestParam(value = "size", required = false, defaultValue = "20") 
            int size,
            @Parameter(description = "Sıralama (örn: createdAt,desc)") @RequestParam(value = "sort", required = false, defaultValue = "createdAt,desc")
            String sort
    ) {

        List<Product> list = (q == null || q.isBlank()) ? productUseCase.listAll() : productUseCase.searchByName(q);
        List<PublicProductDto> dtos = list.stream()
            .map(p -> new PublicProductDto(p.getId(), p.getName(), p.getPrice(), p.getStock()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ürün detayı", description = "ID ile ürün detayını döner")
    public ResponseEntity<PublicProductDto> getById(@Parameter(description = "Ürün kimliği") @PathVariable Long id) {
        return productUseCase.getById(id)
            .map(p -> new PublicProductDto(p.getId(), p.getName(), p.getPrice(), p.getStock()))
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}


