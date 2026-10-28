package com.orderflow.inventory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.util.UUID;
@Component
public class InventoryMessaging {
    private final RabbitTemplate rabbit; private final ObjectMapper json; private final ReservationService reservations;
    private final String exchange,resultRoutingKey;
    public InventoryMessaging(RabbitTemplate rabbit,ObjectMapper json,ReservationService reservations,
      @Value("${messaging.exchange}") String exchange,@Value("${messaging.result-routing-key}") String resultRoutingKey){
        this.rabbit=rabbit;this.json=json;this.reservations=reservations;
        this.exchange=exchange;this.resultRoutingKey=resultRoutingKey;
    }
    @RabbitListener(queues="${messaging.request-queue}")
    public void receiveRequest(String body) throws Exception {
        var request=json.readValue(body,OrderRequested.class);
        boolean reserved=reservations.reserve(request.orderId(),request.sku(),request.quantity());
        var result=new OrderResult(request.orderId(),reserved);
        rabbit.convertAndSend(exchange,resultRoutingKey,json.writeValueAsString(result));
    }
    public record OrderRequested(UUID orderId,String sku,int quantity){}
    public record OrderResult(UUID orderId,boolean reserved){}
}
