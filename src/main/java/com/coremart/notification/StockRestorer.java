package com.coremart.notification;

import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Product;
import com.coremart.order.Order;
import com.coremart.order.OrderObserver;
import com.coremart.order.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

/** OBSERVER pattern - a <b>Concrete Observer</b>: puts the products of a cancelled order back in stock. */
@Component
public class StockRestorer implements OrderObserver {

    private final ProductCatalog catalog;
    private final NotificationLog log;

    public StockRestorer(ProductCatalog catalog, NotificationLog log) {
        this.catalog = catalog;
        this.log = log;
    }

    @Override
    public void statusChanged(Order order, OrderStatus from, OrderStatus to) {
        if (to != OrderStatus.CANCELLED) {
            return;
        }
        for (Map.Entry<Product, Integer> e : order.getRequiredStock().entrySet()) {
            catalog.increaseStock(e.getKey().getId(), e.getValue());
        }
        log.send(new Notification(order.getNumber(), Notification.WAREHOUSE, "Warehouse manager",
                "Order " + order.getNumber() + " cancelled, items returned to stock."));
    }
}
