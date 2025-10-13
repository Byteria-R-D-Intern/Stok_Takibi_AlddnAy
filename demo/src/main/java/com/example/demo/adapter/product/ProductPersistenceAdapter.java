package com.example.demo.adapter.product;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.demo.domain.model.Product;
import com.example.demo.domain.port.ProductRepository;

@Repository
public class ProductPersistenceAdapter implements ProductRepository {

	private final ProductJpaRepository jpa;

	public ProductPersistenceAdapter(ProductJpaRepository jpa) { this.jpa = jpa; }

	@Override
	public Product save(Product product) { return jpa.save(product); }

	@Override
	public Optional<Product> findById(Long id) { return jpa.findById(id); }

	@Override
	public List<Product> findAll() { return jpa.findAll(); }

	@Override
	public List<Product> findByNameContains(String namePart) { return jpa.findByNameContainingIgnoreCase(namePart); }

	@Override
	public boolean existsByName(String name) { return jpa.existsByName(name); }

	@Override
	public void deleteById(Long id) { jpa.deleteById(id); }
}


