package com.example.demo.adapter.persistence.order;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.demo.domain.model.Order;
import com.example.demo.domain.model.OrderStatus;
import com.example.demo.domain.port.OrderRepository;

@Repository
public class OrderPersistenceAdapter implements OrderRepository {

	private final OrderJpaRepository jpa;

	public OrderPersistenceAdapter(OrderJpaRepository jpa) { this.jpa = jpa; }

	@Override
	public Order save(Order order) { return jpa.save(order); }

	@Override
	public Optional<Order> findById(Long id) { return jpa.findById(id); }

	@Override
	public List<Order> findByUserId(Long userId) { throw new UnsupportedOperationException(); }

	@Override
	public List<Order> findByStatus(OrderStatus status) { throw new UnsupportedOperationException(); }

	@Override
	public List<Order> findByCreatedAtBetween(Instant from, Instant to) { throw new UnsupportedOperationException(); }
}


