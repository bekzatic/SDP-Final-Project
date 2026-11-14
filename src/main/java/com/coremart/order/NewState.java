package com.coremart.order;

import java.util.List;

/** The order is placed but not paid yet. */
public class NewState implements OrderState {

    @Override
    public String getCode() {
        return "NEW";
    }

    @Override
    public String getName() {
        return "Awaiting payment";
    }

    @Override
    public String getDescription() {
        return "Order placed. Assembly starts after payment.";
    }

    @Override
    public List<OrderAction> getAvailableActions() {
        return List.of(OrderAction.PAY, OrderAction.CANCEL);
    }

    @Override
    public void pay(Order order) {
        order.changeState(new PaidState());
    }

    @Override
    public void cancel(Order order) {
        order.changeState(new CancelledState());
    }
}
