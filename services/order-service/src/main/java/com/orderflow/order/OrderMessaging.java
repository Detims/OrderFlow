package com.orderflow.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class OrderMessaging {
    private final RabbitTemplate rabbit; private final ObjectMapper json; private final OrderResultHandler resultHandler;
    private final String exchange; private final String requestRoutingKey;
    public OrderMessaging(RabbitTemplate rabbit, ObjectMapper json, OrderResultHandler resultHandler,
        @Value("${messaging.exchange}") String exchange, @Value("${messaging.request-routing-key}") String requestRoutingKey) {
        this.rabbit=rabbit; this.json=json; this.resultHandler=resultHandler;
        this.exchange=exchange; this.requestRoutingKey=requestRoutingKey;
    }
    public void send(OrderRequested event) {
        try { rabbit.convertAndSend(exchange, requestRoutingKey, json.writeValueAsString(event)); }
        catch (Exception e) { throw new IllegalStateException("Could not publish order request", e); }
    }
    @RabbitListener(queues = "${messaging.result-queue}")
    public void receiveResult(String body) throws Exception {
        resultHandler.apply(json.readValue(body, OrderResult.class));
    }
    public record OrderRequested(UUID orderId, String sku, int quantity) {}
    public record OrderResult(UUID orderId, boolean reserved) {}
}
