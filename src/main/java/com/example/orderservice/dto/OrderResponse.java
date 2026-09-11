package com.example.orderservice.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerId,
        List<OrderItemRequest> items,
        BigDecimal rawTotal,
        BigDecimal discountedTotal,
        OrderStatus status,
        String rejectionReason
) {
}
