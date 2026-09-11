package com.example.orderservice.service;

import com.example.orderservice.dto.OrderItemRequest;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.OrderStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("500.00");
    private static final BigDecimal DISCOUNT_MULTIPLIER = new BigDecimal("0.90");
    private final Map<UUID, OrderResponse> orders = new ConcurrentHashMap<>();

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        UUID orderId = UUID.randomUUID();
        BigDecimal rawTotal = request.items()
                .stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean containsDiscontinuedSku = request.items()
                .stream()
                .map(OrderItemRequest::sku)
                .anyMatch(sku -> sku.startsWith("DISCONTINUED-"));

        OrderStatus status = containsDiscontinuedSku ? OrderStatus.REJECTED : OrderStatus.APPROVED;
        String rejectionReason = containsDiscontinuedSku ? "Order contains discontinued items" : null;

        BigDecimal discountedTotal = rawTotal;
        if (rawTotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            discountedTotal = rawTotal.multiply(DISCOUNT_MULTIPLIER).setScale(2, RoundingMode.HALF_UP);
        }

        OrderResponse response = new OrderResponse(
                orderId,
                request.customerId(),
                request.items(),
                rawTotal,
                discountedTotal,
                status,
                rejectionReason
        );
        orders.put(orderId, response);
        return response;
    }

    @Override
    public Optional<OrderResponse> getOrder(UUID id) {
        return Optional.ofNullable(orders.get(id));
    }
}
