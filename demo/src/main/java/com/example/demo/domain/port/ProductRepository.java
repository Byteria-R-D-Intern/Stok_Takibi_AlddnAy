package com.example.demo.domain.port;

import com.example.demo.domain.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
	Product save(Product product);
	Optional<Product> findById(Long id);
	List<Product> findAll();
	List<Product> findByNameContains(String namePart);
    boolean existsByName(String name);
    void deleteById(Long id);
}


