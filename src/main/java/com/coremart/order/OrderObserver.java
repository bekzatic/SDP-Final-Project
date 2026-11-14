package com.coremart.order;

/**
 * Observer: something that wants to know what happens to an order.
 * <p>
 * Observers subscribe to an {@link Order}; the order calls them when it is placed and on every
 * status change. New reactions (an SMS, a report, a bonus program) are added as new observers
 * without changing the order or its states.
 */
public interface OrderObserver {

    default void orderPlaced(Order order) {
    }

    void statusChanged(Order order, OrderState from, OrderState to);
}
