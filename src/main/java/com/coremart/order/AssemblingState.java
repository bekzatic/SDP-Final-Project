package com.coremart.order;

import java.util.List;

/** The warehouse is packing the order (and setting up software, if it was ordered). */
public class AssemblingState implements OrderState {

    @Override
    public String getCode() {
        return "ASSEMBLING";
    }

    @Override
    public String getName() {
        return "Assembling";
    }

    @Override
    public String getDescription() {
        return "Your order is being assembled and checked at the warehouse.";
    }

    @Override
    public List<OrderAction> getAvailableActions() {
        return List.of(OrderAction.SHIP, OrderAction.CANCEL);
    }

    @Override
    public void ship(Order order) {
        order.changeState(new ShippedState(order.getDelivery()));
    }

    @Override
    public void cancel(Order order) {
        order.changeState(new CancelledState());
    }
}
