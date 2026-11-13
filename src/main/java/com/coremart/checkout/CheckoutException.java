package com.coremart.checkout;

import java.util.List;

/** Checkout could not be finished; the messages are shown to the customer. */
public class CheckoutException extends RuntimeException {

    private final List<String> errors;

    public CheckoutException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public CheckoutException(String error) {
        this(List.of(error));
    }

    public List<String> getErrors() {
        return errors;
    }
}
