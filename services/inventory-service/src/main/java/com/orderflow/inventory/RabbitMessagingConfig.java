package com.orderflow.inventory;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMessagingConfig {
    @Bean DirectExchange orderFlowExchange(@Value("${messaging.exchange}") String name) {
        return new DirectExchange(name, true, false);
    }
    @Bean Queue orderRequestQueue(@Value("${messaging.request-queue}") String name) {
        return QueueBuilder.durable(name).build();
    }
    @Bean Queue orderResultQueue(@Value("${messaging.result-queue}") String name) {
        return QueueBuilder.durable(name).build();
    }
    @Bean Binding requestBinding(Queue orderRequestQueue, DirectExchange orderFlowExchange,
                                 @Value("${messaging.request-routing-key}") String key) {
        return BindingBuilder.bind(orderRequestQueue).to(orderFlowExchange).with(key);
    }
    @Bean Binding resultBinding(Queue orderResultQueue, DirectExchange orderFlowExchange,
                                @Value("${messaging.result-routing-key}") String key) {
        return BindingBuilder.bind(orderResultQueue).to(orderFlowExchange).with(key);
    }
}
