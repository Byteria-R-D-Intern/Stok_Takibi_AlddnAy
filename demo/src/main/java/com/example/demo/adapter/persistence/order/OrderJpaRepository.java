package com.example.demo.adapter.persistence.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.model.Order;

@Repository
public interface OrderJpaRepository extends JpaRepository<Order, Long> {}


