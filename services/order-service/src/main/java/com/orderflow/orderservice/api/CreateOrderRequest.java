package com.orderflow.orderservice.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(@NotBlank String sku, @Positive int quantity) {}
