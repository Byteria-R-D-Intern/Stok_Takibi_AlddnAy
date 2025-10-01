package com.example.demo.adapter.persistence.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.model.Product;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, Long> {
	List<Product> findByNameContainingIgnoreCase(String namePart);
	Optional<Product> findByName(String name);
    boolean existsByName(String name);
}



