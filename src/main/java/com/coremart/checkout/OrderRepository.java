package com.coremart.checkout;

import com.coremart.order.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** All placed orders, kept in memory while the application runs. */
@Component
public class OrderRepository {

    private final AtomicLong nextId = new AtomicLong(1);
    private final Map<String, Order> orders = new LinkedHashMap<>();

    public long nextId() {
        return nextId.getAndIncrement();
    }

    public synchronized void save(Order order) {
        orders.put(order.getNumber(), order);
    }

    public synchronized Optional<Order> findByNumber(String number) {
        return Optional.ofNullable(number == null ? null : orders.get(number.trim().toUpperCase()));
    }

    public synchronized List<Order> findAll() {
        List<Order> all = new ArrayList<>(orders.values());
        Collections.reverse(all);
        return all;
    }

    public synchronized int size() {
        return orders.size();
    }
}
