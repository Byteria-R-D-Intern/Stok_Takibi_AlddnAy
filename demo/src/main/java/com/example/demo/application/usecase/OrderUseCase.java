package com.example.demo.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.model.Order;
import com.example.demo.domain.model.OrderItem;
import com.example.demo.domain.model.OrderStatus;
import com.example.demo.domain.model.Product;
import com.example.demo.domain.model.User;
import com.example.demo.domain.port.OrderItemRepository;
import com.example.demo.domain.port.OrderRepository;
import com.example.demo.domain.port.ProductRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OrderUseCase {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;


    @Transactional
    public CheckoutResult directCheckout(CheckoutCommand cmd){
        validate(cmd); // doğrulamalr burada yapılıyor quantity veya gerekli parametrelerin null olması duruömunda hataalr fırlatılıyor.

        // ürünleri birleştirme işlemi burada yapılıyor.
        Map<Long,Integer> productIdToQty = mergeItems(cmd);
        
        BigDecimal total = BigDecimal.ZERO;        // toplam başlangıcı
        Map<Long, Product> productsById = new HashMap<>();  
        
        //Stok ve Fiyat doğrulamsı 
        for (Map.Entry<Long,Integer> entry : productIdToQty.entrySet()){
            Long productId = entry.getKey();
            Integer qty = entry.getValue();
        

        
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("ürün bulunamadı" + productId));

        if (product.getStock() == null || product.getStock() < qty){   
            throw new IllegalStateException("ürün stokta yetersiz");
        }   
        
        //Sipariş geçişinde ürnünün fiyatı değişmemesi lazım.Siparis anınadaki fiyat tutuluyor
        BigDecimal unitPriceSnapshot = product.getPrice();

        BigDecimal lineTotal = unitPriceSnapshot.multiply(BigDecimal.valueOf(qty));

        total = total.add(lineTotal);

        productsById.put(productId, product);

    }
    Order order = new Order();             
    {
        // user_id set (sadece id set edilmesi yeterli; JPA user_id olarak yazar)
        com.example.demo.domain.model.User userRef = new com.example.demo.domain.model.User();
        userRef.setId(cmd.userId);         

        order.setUser(userRef);
        order.setShipping_name(cmd.shippingName);       
        order.setShipping_phone(cmd.shippingPhone);
        order.setShipping_address(cmd.shippingAddress);
        order.setTotal(total);                         
        order.setStatus(OrderStatus.PAID);             
        order.setCreatedAt(Instant.now());             
    }

    // Önce order’ı kaydet ki kalemlerde order_id’yi kullanabilelim
    order = orderRepository.save(order); 

    // Her ürün için OrderItem oluştur ve stok düş
    for (Map.Entry<Long,Integer> entry : productIdToQty.entrySet()) {
        Long productId = entry.getKey();
        Integer qty = entry.getValue();
        Product product = productsById.get(productId);

        BigDecimal unitPriceSnapshot = product.getPrice();                           
        BigDecimal lineTotal = unitPriceSnapshot.multiply(BigDecimal.valueOf(qty)); 

        // OrderItem oluştur
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(qty);
        item.setUnitPriceSnapshot(unitPriceSnapshot);
        item.setLineTotal(lineTotal);

        orderItemRepository.save(item);  

        // Stok düş
        product.setStock(product.getStock() - qty); 
        productRepository.save(product);            
    }




    return new CheckoutResult(order.getId(), total);

    }

    private void validate(CheckoutCommand cmd){
        if (cmd == null ) throw new IllegalArgumentException("command is null");

        if ( cmd.userId == null) throw new IllegalArgumentException("userId is required");

        if (isBlank(cmd.shippingName) || isBlank(cmd.shippingAddress)) 
            throw new IllegalArgumentException("shippingName and shippingAddress are required");

        if (cmd.items == null || cmd.items.length == 0) 
           throw new IllegalArgumentException("items must not be empty");

        for (CheckoutItem item : cmd.items){
            if (item == null || item.productId == null || item.quantity == null || item.quantity < 1)
                throw new IllegalArgumentException("ürün id si veya quantity boş olamaz");
        }   
        
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