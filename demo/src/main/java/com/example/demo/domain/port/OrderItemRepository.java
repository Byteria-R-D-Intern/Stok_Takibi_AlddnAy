package com.example.demo.domain.port;

import com.example.demo.domain.model.OrderItem;
import java.util.List;
import java.util.Optional;

public interface OrderItemRepository {
	OrderItem save(OrderItem orderItem);
	Optional<OrderItem> findById(Long id);
	List<OrderItem> findByOrderId(Long orderId);
	void deleteById(Long id);
}


