package com.example.orderservice;

import com.example.orderservice.service.InventoryService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryServiceTest {

    @Test
    void shouldAllowOnlyTenSuccessfulReservationsUnderConcurrentLoad() throws InterruptedException {
        InventoryService inventoryService = new InventoryService();
        int threads = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    boolean reserved = inventoryService.reserveStock("PHONE-01", 1);
                    if (reserved) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertThat(doneLatch.await(5, TimeUnit.SECONDS)).isTrue();
        executorService.shutdownNow();

        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failureCount.get()).isEqualTo(10);
        assertThat(inventoryService.getStock("PHONE-01")).isEqualTo(0);
    }
}
