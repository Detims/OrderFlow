package com.orderflow.orderservice.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
public class OrderController {
    
    @GetMapping("/api/health")
    public String health() {
        return "order-service is running.";
    }
}
