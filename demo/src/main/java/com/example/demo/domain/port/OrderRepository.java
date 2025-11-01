package com.example.demo.domain.port;

import com.example.demo.domain.model.Order;
import com.example.demo.domain.model.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
	Order save(Order order);
	Optional<Order> findById(Long id);
	List<Order> findByUserId(Long userId);
	List<Order> findByStatus(OrderStatus status);
	List<Order> findByCreatedAtBetween(Instant from, Instant to);
	List<Order> findAll();
    
}


