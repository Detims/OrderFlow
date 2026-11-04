package com.orderflow.orderservice.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.orderflow.orderservice.order.Order.OrderStatus;

@DisplayName("Order Tests")
class OrderTest {

    @Test 
    @DisplayName("New valid Order should have PENDING status")
    void orderPending() {
        Order order = new Order("1", 1);
        OrderStatus status = order.getStatus();

        assertEquals(OrderStatus.PENDING, status);
    }

    @Test
    @DisplayName("Confirming an Order should change status to CONFIRMED")
    void orderConfirm() {
        Order order = new Order("1", 1);
        order.confirm();
        OrderStatus status = order.getStatus();

        assertEquals(OrderStatus.CONFIRMED, status);
    }

    @Test 
    @DisplayName("Rejecting an Order should change status to REJECTED")
    void orderReject() {
        Order order = new Order("1", 1);
        order.reject();
        OrderStatus status = order.getStatus();

        assertEquals(OrderStatus.REJECTED, status);
    }

    @Test
    @DisplayName("Order should reject a blank SKU")
    void blankSku() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Order("", 1);
        });
    }

    @Test
    @DisplayName("Order should reject an SKU with only whitespace")
    void whiteSpaceSku() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Order("          ", 1);
        });
    }

    @Test 
    @DisplayName("Order should reject a quantity of 0")
    void zeroQuantityOrder() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Order("1", 0);
        });
    }

    @Test 
    @DisplayName("Order should reject a negative quantity")
    void negativeQuantityOrder() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Order("1", -1);
        });
    }
}
