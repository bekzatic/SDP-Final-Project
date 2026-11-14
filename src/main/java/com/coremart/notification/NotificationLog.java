package com.coremart.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sent messages. A real store would send emails; here they are kept in memory, written to the
 * console and shown on the order page and the admin page.
 */
@Component
public class NotificationLog {

    private static final Logger log = LoggerFactory.getLogger(NotificationLog.class);

    private final List<Notification> sent = new ArrayList<>();

    public synchronized void send(Notification notification) {
        sent.add(notification);
        log.info("[{}] to {}: {}", notification.channel(), notification.recipient(), notification.text());
    }

    public synchronized List<Notification> forOrder(String orderNumber) {
        return sent.stream().filter(n -> n.orderNumber().equals(orderNumber)).toList();
    }

    public synchronized List<Notification> latest(String channel, int limit) {
        List<Notification> result = new ArrayList<>();
        for (int i = sent.size() - 1; i >= 0 && result.size() < limit; i--) {
            if (sent.get(i).channel().equals(channel)) {
                result.add(sent.get(i));
            }
        }
        return result;
    }

    public synchronized int size() {
        return sent.size();
    }
}
