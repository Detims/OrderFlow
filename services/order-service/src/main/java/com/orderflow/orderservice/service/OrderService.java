package com.orderflow.orderservice.service;

import org.springframework.stereotype.Service;

import com.orderflow.orderservice.order.Order;

@Service 
public class OrderService {
    public Order createOrder(String sku, int quantity) {
        return new Order(sku, quantity);
    }
}
