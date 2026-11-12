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
        return delivery == DeliveryMethod.PICKUP ? "Готов к выдаче" : "Передан курьеру";
    }

    @Override
    public String getDescription() {
        return delivery == DeliveryMethod.PICKUP
                ? "Заказ ждёт вас в магазине. Назовите номер заказа на кассе."
                : "Курьер привезёт заказ и заранее позвонит.";
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
