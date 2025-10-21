package com.example.demo.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.model.Order;
import com.example.demo.domain.model.OrderItem;
import com.example.demo.domain.model.OrderStatus;
import com.example.demo.domain.model.Product;
import com.example.demo.domain.port.OrderItemRepository;
import com.example.demo.domain.port.OrderRepository;
import com.example.demo.domain.port.ProductRepository;
import com.example.demo.application.service.NotificationService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OrderUseCase {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final AuditLogUseCase auditLogUseCase;


    @Transactional
    public CheckoutResult directCheckout(CheckoutCommand cmd){

        Map<Long,Integer> productIdToQty = mergeItems(cmd);
        BigDecimal total = BigDecimal.ZERO;
        Map<Long, Product> productsById = new HashMap<>();

        for (Map.Entry<Long,Integer> entry : productIdToQty.entrySet()){
            Long productId = entry.getKey();
            Integer qty = entry.getValue();

            Product product = productRepository.findById(productId).orElseThrow(java.util.NoSuchElementException::new);
            if (product.getStock() == null || product.getStock() < qty) {
                throw new IllegalStateException("ürün stokta yetersiz");
            }

            BigDecimal unitPriceSnapshot = product.getPrice();
            BigDecimal lineTotal = unitPriceSnapshot.multiply(BigDecimal.valueOf(qty));
            total = total.add(lineTotal);
            productsById.put(productId, product);
        }

        Order order = new Order();
        com.example.demo.domain.model.User userRef = new com.example.demo.domain.model.User();
        userRef.setId(cmd.userId);
        order.setUser(userRef);
        order.setShipping_name(cmd.shippingName);
        order.setShipping_phone(cmd.shippingPhone);
        order.setShipping_address(cmd.shippingAddress);
        order.setTotal(total);
        //Ödeme yapıldıktan sonra PAID durumuna geçecek 
        order.setStatus(OrderStatus.DRAFT);
        order.setCreatedAt(Instant.now());

        order = orderRepository.save(order);
        // Bildirim: Sipariş oluşturuldu
        notificationService.create(cmd.userId, "ORDER_CREATED", "Sipariş oluşturuldu", "Sipariş #" + order.getId() + " oluşturuldu.", null);


        auditLogUseCase.log(cmd.userId, "order", order.getId(), "create", "order created ", null);

        for (Map.Entry<Long,Integer> entry : productIdToQty.entrySet()) {
            Long productId = entry.getKey();
            Integer qty = entry.getValue();
            Product product = productsById.get(productId);

            BigDecimal unitPriceSnapshot = product.getPrice();
            BigDecimal lineTotal = unitPriceSnapshot.multiply(BigDecimal.valueOf(qty));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(qty);
            item.setUnitPriceSnapshot(unitPriceSnapshot);
            item.setLineTotal(lineTotal);
            orderItemRepository.save(item);

            product.setStock(product.getStock() - qty);
            productRepository.save(product);
        }

        return new CheckoutResult(order.getId(), total);
    }


    public List<Order> listUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order getUserOrder(Long userId, Long orderId) {

        Order o = orderRepository.findById(orderId).orElseThrow(java.util.NoSuchElementException::new);

        if (!o.getUser().getId().equals(userId)) throw new IllegalArgumentException("forbidden");
        return o;
    }

    public void cancelUserOrder(Long userId, Long orderId) {

        Order o = orderRepository.findById(orderId).orElseThrow(java.util.NoSuchElementException::new);

        if (!o.getUser().getId().equals(userId)) throw new IllegalArgumentException("forbidden");
        if (o.getStatus() == OrderStatus.SHIPPED || o.getStatus() == OrderStatus.DELIVERED)

            throw new IllegalStateException("cannot cancel after shipment");

        o.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(o);
        
        //log
        auditLogUseCase.log(userId, "order", orderId, "cancel", "order cancelled (service)", null);
        // notification
        notificationService.create(userId, "ORDER_CANCELLED", "Sipariş iptal edildi", "Sipariş #" + orderId + " iptal edildi.", null);
    }


    public void updateShippingInfo(Long userId, Long orderId, String name, String phone, String address) {

        Order o = orderRepository.findById(orderId).orElseThrow(java.util.NoSuchElementException::new);

        if (!o.getUser().getId().equals(userId)) throw new IllegalArgumentException("forbidden");

        if (o.getStatus() == OrderStatus.SHIPPED || o.getStatus() == OrderStatus.DELIVERED)
            throw new IllegalStateException("cannot update after shipment");

        if (isBlank(name) || isBlank(address)) throw new IllegalArgumentException("shippingName and shippingAddress are required");

        o.setShipping_name(name);
        o.setShipping_phone(phone);
        o.setShipping_address(address);
        orderRepository.save(o);
 
        auditLogUseCase.log(userId, "order", orderId, "update_shipping", "shipping info updated (service)", null);
    }


    public SalesStats stats(java.time.Instant from, java.time.Instant to) {
        java.util.List<Order> all = orderRepository.findByCreatedAtBetween(from, to);
        java.math.BigDecimal totalRevenue = java.math.BigDecimal.ZERO;
        long count = 0;
        long paid = 0, shipped = 0, delivered = 0, cancelled = 0;
        for (Order o : all) {
            if (o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.SHIPPED || o.getStatus() == OrderStatus.DELIVERED) {
                if (o.getTotal() != null) totalRevenue = totalRevenue.add(o.getTotal());
            }
            count++;
            switch (o.getStatus()) {
                case PAID -> paid++;
                case SHIPPED -> shipped++;
                case DELIVERED -> delivered++;
                case CANCELLED -> cancelled++;
                default -> {}
            }
        }
        return new SalesStats(count, totalRevenue, paid, shipped, delivered, cancelled);
    }

    public static class SalesStats {
        public final long totalOrders;
        public final java.math.BigDecimal totalRevenue;
        public final long paidOrders;
        public final long shippedOrders;
        public final long deliveredOrders;
        public final long cancelledOrders;
        public SalesStats(long totalOrders, java.math.BigDecimal totalRevenue, long paid, long shipped, long delivered, long cancelled) {
            this.totalOrders = totalOrders;
            this.totalRevenue = totalRevenue;
            this.paidOrders = paid;
            this.shippedOrders = shipped;
            this.deliveredOrders = delivered;
            this.cancelledOrders = cancelled;
        }
    }

   
    public void updateStatus(Long orderId, OrderStatus newStatus) {
        Order o = orderRepository.findById(orderId).orElseThrow(java.util.NoSuchElementException::new);
        o.setStatus(newStatus);
        orderRepository.save(o);
        // Service-level audit: status updated (actor = order owner)
        Long actorIdForAudit = (o.getUser() != null ? o.getUser().getId() : 0L);
        auditLogUseCase.log(actorIdForAudit, "order", orderId, "update_status", "order status=" + newStatus, null);
        // notification to owner
        Long ownerId = (o.getUser() != null ? o.getUser().getId() : null);
        if (ownerId != null) {
            notificationService.create(ownerId, "STATUS_CHANGED", "Sipariş durumu güncellendi", "Sipariş #" + orderId + " durumu: " + newStatus, null);
        }
    }

    // Admin: fetch by id without user checks
    public Order getByIdAdmin(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(java.util.NoSuchElementException::new);
    }


    private Map<Long,Integer> mergeItems(CheckoutCommand cmd){
        Map<Long,Integer> merged = new HashMap<>();
        for (CheckoutItem item :cmd.items){
            merged.merge(item.productId, item.quantity, Integer::sum);
        }
        return merged;
    }

    private static boolean isBlank(String str){
        return str == null || str.isBlank();
    }

    public static class CheckoutCommand{
        public final Long userId;
        public final String shippingName;
        public final String shippingPhone;
        public final String shippingAddress;
        public final CheckoutItem[] items;

        public CheckoutCommand(Long userId,
                               String shippingName,
                               String shippingPhone,
                               String shippingAddress,
                               CheckoutItem[] items) {
            this.userId = userId;
            this.shippingName = shippingName;
            this.shippingPhone = shippingPhone;
            this.shippingAddress = shippingAddress;
            this.items = items;
        }

    }

    public static class CheckoutItem {
        public final Long productId;
        public final Integer quantity;

        public CheckoutItem(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }

    public static class CheckoutResult {
        public final Long orderId;
        public final BigDecimal total;

        public CheckoutResult(Long orderId, BigDecimal total) {
            this.orderId = orderId;
            this.total = total;
        }
    }
}