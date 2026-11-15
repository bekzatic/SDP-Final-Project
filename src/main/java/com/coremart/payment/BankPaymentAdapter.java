package com.coremart.payment;

import org.springframework.stereotype.Component;

/**
 * ADAPTER pattern - the <b>Adapter</b>.
 * <p>
 * Lets the checkout use the bank API through the store's {@link PaymentGateway} interface:
 * converts tenge to tiyn, adds the currency, maps "charge" and "refund" to the bank's
 * "authorize" and "reverse", and turns the bank's status codes into exceptions.
 */
@Component
public class BankPaymentAdapter implements PaymentGateway {

    private static final String CURRENCY = "KZT";
    private static final long TIYN_PER_TENGE = 100;

    private final DemoBankApi bank;

    public BankPaymentAdapter(DemoBankApi bank) {
        this.bank = bank;
    }

    @Override
    public String charge(String orderNumber, long amountTenge) {
        if (amountTenge <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        BankResponse response = bank.authorize(orderNumber, amountTenge * TIYN_PER_TENGE, CURRENCY);
        if (!response.approved()) {
            throw new IllegalStateException("Payment declined: " + response.message());
        }
        return response.transactionRef();
    }

    @Override
    public void refund(String paymentId) {
        BankResponse response = bank.reverse(paymentId);
        if (!response.approved()) {
            throw new IllegalArgumentException("Unknown payment " + paymentId);
        }
    }
}
