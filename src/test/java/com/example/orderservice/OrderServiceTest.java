package com.example.orderservice;

import com.example.orderservice.dto.OrderItemRequest;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.OrderStatus;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.service.OrderServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderServiceTest {

    private final OrderService orderService = new OrderServiceImpl();

    @Test
    void shouldCreateApprovedOrderWithRawTotalWithoutDiscount() {
        OrderRequest request = new OrderRequest(
                "customer-1",
                List.of(
                        new OrderItemRequest("SKU-1", 2, new BigDecimal("50.00")),
                        new OrderItemRequest("SKU-2", 1, new BigDecimal("40.00"))
                )
        );

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.customerId()).isEqualTo("customer-1");
        assertThat(response.items()).hasSize(2);
        assertThat(response.rawTotal()).isEqualByComparingTo("140.00");
        assertThat(response.discountedTotal()).isEqualByComparingTo("140.00");
        assertThat(response.status()).isEqualTo(OrderStatus.APPROVED);
        assertThat(response.rejectionReason()).isNull();
    }

    @Test
    void shouldApplyDiscountWhenRawTotalExceedsThreshold() {
        OrderRequest request = new OrderRequest(
                "customer-2",
                List.of(new OrderItemRequest("SKU-3", 3, new BigDecimal("200.00")))
        );

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.rawTotal()).isEqualByComparingTo("600.00");
        assertThat(response.discountedTotal()).isEqualByComparingTo("540.00");
        assertThat(response.status()).isEqualTo(OrderStatus.APPROVED);
    }

    @Test
    void shouldRejectOrderContainingDiscontinuedSku() {
        OrderRequest request = new OrderRequest(
                "customer-3",
                List.of(new OrderItemRequest("DISCONTINUED-123", 1, new BigDecimal("10.00")))
        );

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.status()).isEqualTo(OrderStatus.REJECTED);
        assertThat(response.rejectionReason()).isEqualTo("Order contains discontinued items");
    }

    @Test
    void shouldReturnStoredOrderById() {
        OrderRequest request = new OrderRequest(
                "customer-4",
                List.of(new OrderItemRequest("SKU-4", 1, new BigDecimal("20.00")))
        );

        OrderResponse created = orderService.createOrder(request);
        Optional<OrderResponse> fetched = orderService.getOrder(created.id());

        assertThat(fetched).isPresent();
        assertThat(fetched.orElseThrow()).isEqualTo(created);
    }

    @Test
    void shouldReturnEmptyWhenOrderDoesNotExist() {
        Optional<OrderResponse> fetched = orderService.getOrder(UUID.randomUUID());

        assertThat(fetched).isEmpty();
    }
}
