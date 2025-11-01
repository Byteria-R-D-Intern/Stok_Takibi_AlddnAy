package com.example.demo.adapter.persistence.order;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	public List<Order> findByUserId(Long userId) { return jpa.findByUser_Id(userId); }

	@Override
	public List<Order> findByStatus(OrderStatus status) { return jpa.findByStatus(status); }

	@Override
	public List<Order> findByCreatedAtBetween(Instant from, Instant to) { return jpa.findByCreatedAtBetween(from, to); }

    

	// Paging destekli ek metotlar
	public Page<Order> findPageByUserId(Long userId, Pageable pageable) {
		return jpa.findByUser_Id(userId, pageable);
	}

	public Page<Order> findPageByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable) {
		return jpa.findByUser_IdAndStatus(userId, status, pageable);
	}

	public Page<Order> findPageByUserIdAndCreatedBetween(Long userId, Instant from, Instant to, Pageable pageable) {
		return jpa.findByUser_IdAndCreatedAtBetween(userId, from, to, pageable);
	}

    public Page<Order> findPageByStatus(OrderStatus status, Pageable pageable) {
        return jpa.findByStatus(status, pageable);
    }

    public Page<Order> findPageByCreatedBetween(Instant from, Instant to, Pageable pageable) {
        return jpa.findByCreatedAtBetween(from, to, pageable);
    }

    public Page<Order> findPageByStatusAndCreatedBetween(OrderStatus status, Instant from, Instant to, Pageable pageable) {
        return jpa.findByStatusAndCreatedAtBetween(status, from, to, pageable);
    }
}


