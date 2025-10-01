package com.example.demo.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
		if (productRepository.existsByName(name)) throw new IllegalStateException("product name already exists");
		Product p = new Product();
		p.setName(name);
		p.setPrice(price);
		p.setStock(stock);
		p.setCreatedAt(Instant.now());
		return productRepository.save(p).getId();
	}

	public Optional<Product> getById(Long id) {
		return productRepository.findById(id);
	}

	public List<Product> listAll() {
		return productRepository.findAll();
	}

	public List<Product> searchByName(String query) {
		return productRepository.findByNameContains(query == null ? "" : query);
	}

	public void update(Long id, String name, BigDecimal price, Integer stock) {
		Product existing = productRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("product not found"));
		if (name != null && !name.isBlank()) existing.setName(name);
		if (price != null) {
			if (price.signum() <= 0) throw new IllegalArgumentException("price must be > 0");
			existing.setPrice(price);
		}
		if (stock != null) {
			if (stock < 0) throw new IllegalArgumentException("stock must be >= 0");
			existing.setStock(stock);
		}
		productRepository.save(existing);
	}

	public void delete(Long id) {
		productRepository.deleteById(id);
	}
}



