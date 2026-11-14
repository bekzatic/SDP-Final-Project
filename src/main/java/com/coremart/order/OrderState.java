package com.coremart.order;

import java.util.List;

/**
 * State: one status of an order.
 * <p>
 * The order passes every request to its current state. Each state allows only its own actions
 * and decides which state comes next; everything else throws {@link IllegalStateException}.
 * So there is no {@code switch} over the status anywhere in {@link Order}.
 * <pre>
 * NEW -&gt; PAID -&gt; ASSEMBLING -&gt; SHIPPED -&gt; DELIVERED
 *  |       |          |
 *  +-------+----------+--&gt; CANCELLED
 * </pre>
 */
public interface OrderState {

    /** Stable code, e.g. "PAID". */
    String getCode();

    /** Status as the customer sees it, e.g. "Paid". */
    String getName();

    /** One sentence about what happens now. */
    String getDescription();

    /** Actions allowed in this status. */
    List<OrderAction> getAvailableActions();

    default void pay(Order order) {
        throw notAllowed(OrderAction.PAY);
    }

    default void startAssembly(Order order) {
        throw notAllowed(OrderAction.START_ASSEMBLY);
    }

    default void ship(Order order) {
        throw notAllowed(OrderAction.SHIP);
    }

    default void deliver(Order order) {
        throw notAllowed(OrderAction.DELIVER);
    }

    default void cancel(Order order) {
        throw notAllowed(OrderAction.CANCEL);
    }

    default boolean allows(OrderAction action) {
        return getAvailableActions().contains(action);
    }

    /** Received and cancelled orders do not change any more. */
    default boolean isFinal() {
        return getAvailableActions().isEmpty();
    }

    private IllegalStateException notAllowed(OrderAction action) {
        return new IllegalStateException("Action “" + action.getLabel() + "” is not available: the order status is “"
                + getName() + "”");
    }
}
