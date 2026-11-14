package com.coremart.order;

import java.util.List;

/**
 * The order left the warehouse: it is with the courier or waits in the shop.
 * It cannot be cancelled any more.
 */
public class ShippedState implements OrderState {

    private final DeliveryMethod delivery;

    public ShippedState(DeliveryMethod delivery) {
        this.delivery = delivery;
    }

    @Override
    public String getCode() {
        return "SHIPPED";
    }

    @Override
    public String getName() {
        return delivery == DeliveryMethod.PICKUP ? "Ready for pickup" : "With courier";
    }

    @Override
    public String getDescription() {
        return delivery == DeliveryMethod.PICKUP
                ? "Your order is waiting in the store. Tell the cashier your order number."
                : "The courier will call ahead and bring your order.";
    }

    @Override
    public List<OrderAction> getAvailableActions() {
        return List.of(OrderAction.DELIVER);
    }

    @Override
    public void deliver(Order order) {
        order.changeState(new DeliveredState());
    }
}
