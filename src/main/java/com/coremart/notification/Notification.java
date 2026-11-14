package com.coremart.notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** One message sent by an observer: to the customer by email or to the warehouse. */
public record Notification(String orderNumber, String channel, String recipient, String text, LocalDateTime time) {

    public static final String EMAIL = "Email";
    public static final String WAREHOUSE = "Warehouse";

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    public Notification(String orderNumber, String channel, String recipient, String text) {
        this(orderNumber, channel, recipient, text, LocalDateTime.now());
    }

    public String getTimeText() {
        return time.format(FORMAT);
    }
}
