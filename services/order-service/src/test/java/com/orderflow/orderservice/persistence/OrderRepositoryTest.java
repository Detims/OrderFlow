package com.orderflow.orderservice.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.orderflow.orderservice.order.Order.OrderStatus;

@SpringBootTest 
@Transactional 
public class OrderRepositoryTest {
    @Autowired 
    private OrderRepository repository;

    @Test 
    void testOrderEntity() {
        OrderEntity entity = new OrderEntity("SKU-100", 1, OrderStatus.PENDING);

        repository.save(entity);
        Long id = entity.id;

        OrderEntity found = repository.findById(id).orElseThrow();

        assertEquals("SKU-100", found.sku);
        assertEquals(1, found.quantity);
        assertEquals(OrderStatus.PENDING, found.status);
    }
}
