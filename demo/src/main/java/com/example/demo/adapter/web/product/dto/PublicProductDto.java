package com.example.demo.adapter.web.product.dto;

import java.math.BigDecimal;

// NEDEN: Public ürün listesinden SKU/metadata/description'ı gizlemek için minimal DTO
public record PublicProductDto(
    Long id,
    String name,
    BigDecimal price,
    Integer stock
) {}



