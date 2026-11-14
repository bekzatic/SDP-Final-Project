package com.coremart.notification;

import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Product;
import com.coremart.order.CancelledState;
import com.coremart.order.Order;
import com.coremart.order.OrderObserver;
import com.coremart.order.OrderState;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Concrete observer: puts the products of a cancelled order back in stock. */
@Component
public class StockRestorer implements OrderObserver {

    private final ProductCatalog catalog = ProductCatalog.getInstance();
    private final NotificationLog log;

    public StockRestorer(NotificationLog log) {
        this.log = log;
    }

    @Override
    public void statusChanged(Order order, OrderState from, OrderState to) {
        if (!(to instanceof CancelledState)) {
            return;
        }
        for (Map.Entry<Product, Integer> e : order.getRequiredStock().entrySet()) {
            catalog.increaseStock(e.getKey().getId(), e.getValue());
        }
        log.send(new Notification(order.getNumber(), Notification.WAREHOUSE, "Warehouse manager",
                "Order " + order.getNumber() + " cancelled, items returned to stock."));
    }
}
