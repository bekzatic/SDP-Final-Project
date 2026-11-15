package com.coremart.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ADAPTER pattern - the <b>Adaptee</b>.
 * <p>
 * Stands in for a bank's client library with its own interface that does not match the store:
 * amounts are in tiyn (1 tenge = 100 tiyn) with a currency code, a payment is "authorized"
 * and "reversed", and errors come back as status codes instead of exceptions.
 * Demo only: no card data is asked for or stored.
 */
@Component
public class DemoBankApi {

    public static final int INVALID_AMOUNT = 13;
    public static final int UNKNOWN_TRANSACTION = 25;
    public static final int WRONG_CURRENCY = 51;

    private static final Logger log = LoggerFactory.getLogger(DemoBankApi.class);

    private final AtomicLong nextRef = new AtomicLong(5001);
    private final Map<String, Long> authorized = new LinkedHashMap<>();
    private final Set<String> reversed = new HashSet<>();

    public synchronized BankResponse authorize(String merchantOrderId, long amountMinor, String currency) {
        if (!"KZT".equals(currency)) {
            return new BankResponse(null, WRONG_CURRENCY, "Currency not supported");
        }
        if (amountMinor <= 0) {
            return new BankResponse(null, INVALID_AMOUNT, "Invalid amount");
        }
        String ref = "TX-" + nextRef.getAndIncrement();
        authorized.put(ref, amountMinor);
        log.info("Bank: {} authorized for {}, {} tiyn", ref, merchantOrderId, amountMinor);
        return new BankResponse(ref, BankResponse.APPROVED, "Approved");
    }

    public synchronized BankResponse reverse(String transactionRef) {
        if (!authorized.containsKey(transactionRef)) {
            return new BankResponse(transactionRef, UNKNOWN_TRANSACTION, "Unknown transaction");
        }
        if (reversed.add(transactionRef)) {
            log.info("Bank: {} reversed", transactionRef);
        }
        return new BankResponse(transactionRef, BankResponse.APPROVED, "Reversed");
    }

    public synchronized boolean isReversed(String transactionRef) {
        return reversed.contains(transactionRef);
    }

    public synchronized Long amountMinorOf(String transactionRef) {
        return authorized.get(transactionRef);
    }
}
