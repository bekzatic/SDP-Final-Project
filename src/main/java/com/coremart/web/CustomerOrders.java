package com.coremart.web;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Numbers of the orders placed in this browser session ("my orders"). */
@Component
@SessionScope
public class CustomerOrders {

    private final List<String> numbers = new ArrayList<>();

    public void add(String orderNumber) {
        if (!numbers.contains(orderNumber)) {
            numbers.add(orderNumber);
        }
    }

    public boolean contains(String orderNumber) {
        return orderNumber != null && numbers.contains(orderNumber.trim().toUpperCase());
    }

    /** Newest first. */
    public List<String> getNumbers() {
        List<String> copy = new ArrayList<>(numbers);
        Collections.reverse(copy);
        return copy;
    }
}
