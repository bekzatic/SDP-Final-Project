package com.coremart.order;

import java.util.Arrays;
import java.util.Optional;

/** What can be done with an order. Which actions are allowed depends on its {@link OrderState}. */
public enum OrderAction {

    PAY("Pay", true, false),
    START_ASSEMBLY("Start assembly", false, true),
    SHIP("Ship", false, true),
    DELIVER("Mark as delivered", false, true),
    CANCEL("Cancel order", true, true);

    private final String label;
    private final boolean forCustomer;
    private final boolean forStaff;

    OrderAction(String label, boolean forCustomer, boolean forStaff) {
        this.label = label;
        this.forCustomer = forCustomer;
        this.forStaff = forStaff;
    }

    public String getLabel() {
        return label;
    }

    public boolean isForCustomer() {
        return forCustomer;
    }

    public boolean isForStaff() {
        return forStaff;
    }

    public static Optional<OrderAction> parse(String value) {
        return Arrays.stream(values()).filter(a -> a.name().equalsIgnoreCase(value)).findFirst();
    }
}
