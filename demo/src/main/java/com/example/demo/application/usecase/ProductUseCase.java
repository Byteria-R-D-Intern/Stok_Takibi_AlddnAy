package com.example.demo.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Service;

import com.example.demo.domain.model.Product;
import com.example.demo.domain.port.ProductRepository;

@Service
public class ProductUseCase {

	private final ProductRepository productRepository;

	public ProductUseCase(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public Long create(String name, BigDecimal price, Integer stock) {
		if (name == null || name.isBlank()) throw new IllegalArgumentException("name required");
		if (price == null || price.signum() <= 0) throw new IllegalArgumentException("price must be > 0");
		if (stock == null || stock < 0) throw new IllegalArgumentException("stock must be >= 0");
		Product p = new Product();
		p.setName(name);
		p.setPrice(price);
		p.setStock(stock);
		p.setCreatedAt(Instant.now());
		return productRepository.save(p).getId();
	}
}



