package com.coremart.order;

import java.util.List;

/** Payment received; the order waits for the warehouse. */
public class PaidState implements OrderState {

    @Override
    public String getCode() {
        return "PAID";
    }

    @Override
    public String getName() {
        return "Paid";
    }

    @Override
    public String getDescription() {
        return "Payment received. We will start assembling your order soon.";
    }

    @Override
    public List<OrderAction> getAvailableActions() {
        return List.of(OrderAction.START_ASSEMBLY, OrderAction.CANCEL);
    }

    @Override
    public void startAssembly(Order order) {
        order.changeState(new AssemblingState());
    }

    @Override
    public void cancel(Order order) {
        order.changeState(new CancelledState());
    }
}
