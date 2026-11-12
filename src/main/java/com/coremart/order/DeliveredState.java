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
        return "Получен";
    }

    @Override
    public String getDescription() {
        return "Заказ получен. Спасибо за покупку!";
    }

    @Override
    public List<OrderAction> getAvailableActions() {
        return List.of();
    }
}
