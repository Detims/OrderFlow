package com.orderflow.order;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "orders", uniqueConstraints = @UniqueConstraint(name = "uk_order_idempotency", columnNames = "idempotency_key"))
public class OrderEntity {
    @Id private UUID id;
    @Column(name = "idempotency_key", nullable = false, length = 100) private String idempotencyKey;
    @Column(nullable = false, length = 80) private String sku;
    @Column(nullable = false) private int quantity;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private OrderStatus status;
    @Column(nullable = false) private Instant createdAt;
    protected OrderEntity() {}
    public OrderEntity(String key, String sku, int quantity) {
        this.id = UUID.randomUUID(); this.idempotencyKey = key; this.sku = sku;
        this.quantity = quantity; this.status = OrderStatus.PENDING; this.createdAt = Instant.now();
    }
    public UUID getId() { return id; } public String getIdempotencyKey() { return idempotencyKey; }
    public String getSku() { return sku; } public int getQuantity() { return quantity; }
    public OrderStatus getStatus() { return status; } public Instant getCreatedAt() { return createdAt; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
