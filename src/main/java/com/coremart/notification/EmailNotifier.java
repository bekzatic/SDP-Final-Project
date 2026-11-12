package com.coremart.notification;

import com.coremart.order.CancelledState;
import com.coremart.order.Order;
import com.coremart.order.OrderObserver;
import com.coremart.order.OrderState;
import com.coremart.order.PaymentMethod;
import org.springframework.stereotype.Component;

/** Concrete observer: emails the customer when the order is placed and when its status changes. */
@Component
public class EmailNotifier implements OrderObserver {

    private final NotificationLog log;

    public EmailNotifier(NotificationLog log) {
        this.log = log;
    }

    @Override
    public void orderPlaced(Order order) {
        String text = "Заказ " + order.getNumber() + " оформлен. Сумма: " + order.getFormattedTotal() + ".";
        if (order.getPayment() == PaymentMethod.LATER) {
            text += " Оплатить его можно в разделе «Мои заказы».";
        }
        send(order, text);
    }

    @Override
    public void statusChanged(Order order, OrderState from, OrderState to) {
        String text = "Заказ " + order.getNumber() + ": «" + to.getName() + "». " + to.getDescription();
        if (to instanceof CancelledState && order.getPaymentId() != null) {
            text += " Деньги вернутся на карту в течение 3 рабочих дней.";
        }
        send(order, text);
    }

    private void send(Order order, String text) {
        log.send(new Notification(order.getNumber(), Notification.EMAIL, order.getCustomer().email(), text));
    }
}
