package com.orderflow.orderservice.order;

public class Order {
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        REJECTED
    }

    private final String sku;
    private final int quantity;
    private OrderStatus status;

    private record OrderRequest (String sku, int quantity) {
        public OrderRequest(String sku, int quantity) {
            this.sku = sku;
            this.quantity = quantity;
        }
    }

    public Order(String sku, int quantity) {
        if (sku == null || sku.isBlank() || quantity <= 0) {
            throw new java.lang.IllegalArgumentException(
                String.format("Invalid sku or quantity: %s, %d", sku, quantity));
        }
        OrderRequest request = new OrderRequest(sku, quantity);

        this.sku = request.sku;
        this.quantity = request.quantity;
        this.status = OrderStatus.PENDING;
    }

    public String getSku() {
        return this.sku;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public OrderStatus getStatus() {
        return this.status;
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
    }

    public void reject() {
        this.status = OrderStatus.REJECTED;
    }
}
