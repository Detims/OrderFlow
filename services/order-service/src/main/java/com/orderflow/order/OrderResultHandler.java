package com.orderflow.order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class OrderResultHandler {
    private final OrderRepository orders;
    public OrderResultHandler(OrderRepository orders) { this.orders=orders; }
    @Transactional
    public void apply(OrderMessaging.OrderResult result) {
        orders.findById(result.orderId()).ifPresent(order -> {
            if (order.getStatus()==OrderStatus.PENDING) order.setStatus(result.reserved()?OrderStatus.CONFIRMED:OrderStatus.REJECTED);
        });
    }
}
