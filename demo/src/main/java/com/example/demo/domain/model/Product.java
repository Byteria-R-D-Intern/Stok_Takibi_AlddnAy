package com.example.demo.domain.model;


import java.time.Instant;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "name",nullable = false)
    private String name;

    @Column(name = "price",nullable = false)
    private BigDecimal price;

    @Column(name = "stock",nullable = false)
    private Integer stock;

    @Column(name = "created_at",nullable = false)
    private Instant createdAt;
    
    //Ürün Kodu
    @Column(name = "sku", length = 64, unique = true)
    private String sku;

    
    @Lob
    @Column(name = "description")
    private String description;

    
    @Lob
    @Column(name = "metadata")
    private String metadata;
}
