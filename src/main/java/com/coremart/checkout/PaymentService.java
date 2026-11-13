package com.coremart.checkout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Demo payment system. A real store would call a bank here; this one only records the payment
 * and returns its id. No card data is asked for or stored.
 */
@Component
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final AtomicLong nextId = new AtomicLong(5001);
    private final Map<String, Long> payments = new LinkedHashMap<>();
    private final Set<String> refunded = new HashSet<>();

    /** Takes the money for an order; returns the payment id. */
    public synchronized String charge(String orderNumber, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        String id = "PAY-" + nextId.getAndIncrement();
        payments.put(id, amount);
        log.info("Payment {} for order {}: {} tenge", id, orderNumber, amount);
        return id;
    }

    /** Returns the money of a payment. A payment is refunded only once. */
    public synchronized void refund(String paymentId) {
        if (!payments.containsKey(paymentId)) {
            throw new IllegalArgumentException("Unknown payment " + paymentId);
        }
        if (refunded.add(paymentId)) {
            log.info("Refund of payment {}: {} tenge", paymentId, payments.get(paymentId));
        }
    }

    public synchronized boolean isRefunded(String paymentId) {
        return refunded.contains(paymentId);
    }

    public synchronized Long amountOf(String paymentId) {
        return payments.get(paymentId);
    }
}
