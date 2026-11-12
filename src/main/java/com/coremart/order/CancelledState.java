package com.coremart.order;

import java.util.List;

/** Final status: the order was cancelled before it left the warehouse. */
public class CancelledState implements OrderState {

    @Override
    public String getCode() {
        return "CANCELLED";
    }

    @Override
    public String getName() {
        return "Отменён";
    }

    @Override
    public String getDescription() {
        return "Заказ отменён.";
    }

    @Override
    public List<OrderAction> getAvailableActions() {
        return List.of();
    }
}
