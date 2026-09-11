package com.example.orderservice.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InventoryService {

    private static final String DEFAULT_SKU = "PHONE-01";
    private static final int DEFAULT_STOCK = 10;

    private final ConcurrentHashMap<String, AtomicInteger> inventoryMap = new ConcurrentHashMap<>();

    public InventoryService() {
        inventoryMap.put(DEFAULT_SKU, new AtomicInteger(DEFAULT_STOCK));
    }

    public boolean reserveStock(String sku, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        AtomicBoolean reserved = new AtomicBoolean(false);
        inventoryMap.computeIfPresent(sku, (key, stock) -> {
            if (stock.get() >= quantity) {
                stock.addAndGet(-quantity);
                reserved.set(true);
            }
            return stock;
        });

        return reserved.get();
    }

    public int getStock(String sku) {
        AtomicInteger stock = inventoryMap.get(sku);
        return stock == null ? 0 : stock.get();
    }
}
