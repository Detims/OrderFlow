package com.orderflow.orderservice.persistence;

import com.orderflow.orderservice.order.Order.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "orders")
public class OrderEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(nullable = false) String sku;
    int quantity;
    @Column(nullable = false) @Enumerated(EnumType.STRING) OrderStatus status;

    protected OrderEntity() {}
}
