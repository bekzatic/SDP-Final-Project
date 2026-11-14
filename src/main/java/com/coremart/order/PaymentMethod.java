package com.coremart.order;

/** When the customer pays: right at checkout or later from the "my orders" page. */
public enum PaymentMethod {

    CARD_NOW("Card now"),
    LATER("Later, in “My orders”");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
