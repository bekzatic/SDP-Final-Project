package com.coremart.order;

/** When the customer pays: right at checkout or later from the "my orders" page. */
public enum PaymentMethod {

    CARD_NOW("Картой сейчас"),
    LATER("Позже, в разделе «Мои заказы»");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
