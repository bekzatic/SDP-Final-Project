package com.coremart.order;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** One line of the order history: which status and when. */
public record StatusChange(String status, LocalDateTime time) {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public String getTimeText() {
        return time.format(FORMAT);
    }
}
