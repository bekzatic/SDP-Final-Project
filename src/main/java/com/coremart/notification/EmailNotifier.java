package com.coremart.notification;

import com.coremart.order.Order;
import com.coremart.order.OrderObserver;
import com.coremart.order.OrderStatus;
import com.coremart.order.PaymentMethod;
import org.springframework.stereotype.Component;

/** OBSERVER pattern - a <b>Concrete Observer</b>: emails the customer when the order is placed and when its status changes. */
@Component
public class EmailNotifier implements OrderObserver {

    private final NotificationLog log;

    public EmailNotifier(NotificationLog log) {
        this.log = log;
    }

    @Override
    public void orderPlaced(Order order) {
        String text = "Order " + order.getNumber() + " placed. Total: " + order.getFormattedTotal() + ".";
        if (order.getPayment() == PaymentMethod.LATER) {
            text += " You can pay for it in “My orders”.";
        }
        send(order, text);
    }

    @Override
    public void statusChanged(Order order, OrderStatus from, OrderStatus to) {
        String text = "Order " + order.getNumber() + ": “" + order.getStatusName() + "”. "
                + order.getStatusDescription();
        if (to == OrderStatus.CANCELLED && order.getPaymentId() != null) {
            text += " The money will be returned to your card within 3 business days.";
        }
        send(order, text);
    }

    private void send(Order order, String text) {
        log.send(new Notification(order.getNumber(), Notification.EMAIL, order.getCustomer().email(), text));
    }
}
