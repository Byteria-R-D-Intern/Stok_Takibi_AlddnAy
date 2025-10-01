package com.example.demo.adapter.persistence.order;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.model.Order;
import com.example.demo.domain.model.OrderStatus;

@Repository
public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUser_Id(Long userId, Pageable pageable);
    Page<Order> findByUser_IdAndStatus(Long userId, OrderStatus status, Pageable pageable);
    Page<Order> findByUser_IdAndCreatedAtBetween(Long userId, Instant from, Instant to, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findByCreatedAtBetween(Instant from, Instant to, Pageable pageable);
    Page<Order> findByStatusAndCreatedAtBetween(OrderStatus status, Instant from, Instant to, Pageable pageable);
}


