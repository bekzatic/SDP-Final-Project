package com.coremart.order;

import java.util.List;

/** Final status: the customer has the order. */
public class DeliveredState implements OrderState {

    @Override
    public String getCode() {
        return "DELIVERED";
    }

    @Override
    public String getName() {
        return "Delivered";
    }

    @Override
    public String getDescription() {
        return "Order received. Thank you for shopping with us!";
    }

    @Override
    public List<OrderAction> getAvailableActions() {
        return List.of();
    }
}
