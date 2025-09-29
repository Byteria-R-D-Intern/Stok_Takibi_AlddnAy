package com.example.demo.adapter.persistence.order;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.demo.domain.model.OrderItem;
import com.example.demo.domain.port.OrderItemRepository;

@Repository
public class OrderItemPersistenceAdapter implements OrderItemRepository {

	private final OrderItemJpaRepository jpa;

	public OrderItemPersistenceAdapter(OrderItemJpaRepository jpa) { this.jpa = jpa; }

	@Override
	public OrderItem save(OrderItem orderItem) { return jpa.save(orderItem); }

	@Override
	public Optional<OrderItem> findById(Long id) { return jpa.findById(id); }

	@Override
	public List<OrderItem> findByOrderId(Long orderId) { return jpa.findByOrderId(orderId); }

	@Override
	public void deleteById(Long id) { jpa.deleteById(id); }
}


