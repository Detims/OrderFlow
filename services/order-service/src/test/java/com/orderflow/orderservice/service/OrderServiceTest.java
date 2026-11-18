package com.orderflow.orderservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.orderflow.orderservice.order.Order;
import com.orderflow.orderservice.order.Order.OrderStatus;

public class OrderServiceTest {
    
    @Test
    void checkOrderService() {
        OrderService orderService = new OrderService();
        Order order = orderService.createOrder("SKU-100", 2);
        
        assertEquals("SKU-100", order.getSku());
        assertEquals(2, order.getQuantity());
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }
}
