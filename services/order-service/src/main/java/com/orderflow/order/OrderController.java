package com.orderflow.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.UUID;

@RestController @RequestMapping("/orders")
public class OrderController {
    private final OrderRepository orders; private final OrderMessaging messaging;
    public OrderController(OrderRepository orders, OrderMessaging messaging) { this.orders = orders; this.messaging = messaging; }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OrderResponse create(@RequestHeader("Idempotency-Key") @NotBlank String key, @Valid @RequestBody CreateOrderRequest request) {
        if (key.length() > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key must be at most 100 characters");
        var existing = orders.findByIdempotencyKey(key);
        if (existing.isPresent()) {
            var order = existing.get();
            if (!order.getSku().equals(request.sku()) || order.getQuantity() != request.quantity())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency-Key was already used for a different request");
            // A client retry can republish an order left pending after a transient queue failure.
            if (order.getStatus() == OrderStatus.PENDING)
                messaging.send(new OrderMessaging.OrderRequested(order.getId(), order.getSku(), order.getQuantity()));
            return OrderResponse.from(order);
        }
        var order = orders.saveAndFlush(new OrderEntity(key, request.sku(), request.quantity()));
        messaging.send(new OrderMessaging.OrderRequested(order.getId(), order.getSku(), order.getQuantity()));
        return OrderResponse.from(order);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {
        return OrderResponse.from(orders.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    public record CreateOrderRequest(@NotBlank String sku, @Positive int quantity) {}
    public record OrderResponse(UUID id, String sku, int quantity, OrderStatus status, Instant createdAt) {
        static OrderResponse from(OrderEntity o) { return new OrderResponse(o.getId(), o.getSku(), o.getQuantity(), o.getStatus(), o.getCreatedAt()); }
    }
}
