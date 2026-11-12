package com.coremart.notification;

import com.coremart.model.Product;
import com.coremart.order.Order;
import com.coremart.order.OrderObserver;
import com.coremart.order.OrderState;
import org.springframework.stereotype.Component;

/** Concrete observer: warns the warehouse when an order leaves few items of a product. */
@Component
public class StockAlert implements OrderObserver {

    /** Products with this many items or fewer need restocking. */
    public static final int THRESHOLD = 3;

    private final NotificationLog log;

    public StockAlert(NotificationLog log) {
        this.log = log;
    }

    @Override
    public void orderPlaced(Order order) {
        for (Product product : order.getRequiredStock().keySet()) {
            int left = product.getStock();
            if (left <= THRESHOLD) {
                String text = left == 0
                        ? "«" + product.getFullName() + "» закончился после заказа " + order.getNumber() + "."
                        : "Заканчивается «" + product.getFullName() + "»: осталось " + left + " шт.";
                log.send(new Notification(order.getNumber(), Notification.WAREHOUSE, "Менеджер склада", text));
            }
        }
    }

    @Override
    public void statusChanged(Order order, OrderState from, OrderState to) {
        // only the stock level matters here
    }
}
