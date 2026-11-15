package com.coremart.payment;

/**
 * ADAPTER pattern - the <b>Target</b>.
 * <p>
 * The simple payment interface the store expects: charge an order in tenge and get back a
 * payment id, refund a payment by its id. The checkout works only with this interface.
 */
public interface PaymentGateway {

    String charge(String orderNumber, long amountTenge);

    void refund(String paymentId);
}
