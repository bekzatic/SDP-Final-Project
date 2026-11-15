package com.coremart.order;

import java.util.List;

/**
 * The statuses of an order and the actions allowed in each of them.
 * <pre>
 * NEW -&gt; PAID -&gt; ASSEMBLING -&gt; SHIPPED -&gt; DELIVERED
 *  |       |          |
 *  +-------+----------+--&gt; CANCELLED
 * </pre>
 */
public enum OrderStatus {

    NEW("Awaiting payment", "Order placed. Assembly starts after payment."),
    PAID("Paid", "Payment received. We will start assembling your order soon."),
    ASSEMBLING("Assembling", "Your order is being assembled and checked at the warehouse."),
    SHIPPED("With courier", "The courier will call ahead and bring your order."),
    DELIVERED("Delivered", "Order received. Thank you for shopping with us!"),
    CANCELLED("Cancelled", "The order has been cancelled.");

    private final String name;
    private final String description;

    OrderStatus(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    /** Status as the customer sees it; a shipped pickup order waits in the store. */
    public String getName(DeliveryMethod delivery) {
        return this == SHIPPED && delivery == DeliveryMethod.PICKUP ? "Ready for pickup" : name;
    }

    public String getDescription(DeliveryMethod delivery) {
        return this == SHIPPED && delivery == DeliveryMethod.PICKUP
                ? "Your order is waiting in the store. Tell the cashier your order number."
                : description;
    }

    public List<OrderAction> getAvailableActions() {
        return switch (this) {
            case NEW -> List.of(OrderAction.PAY, OrderAction.CANCEL);
            case PAID -> List.of(OrderAction.START_ASSEMBLY, OrderAction.CANCEL);
            case ASSEMBLING -> List.of(OrderAction.SHIP, OrderAction.CANCEL);
            case SHIPPED -> List.of(OrderAction.DELIVER);
            case DELIVERED, CANCELLED -> List.of();
        };
    }

    public boolean allows(OrderAction action) {
        return getAvailableActions().contains(action);
    }

    /** Delivered and cancelled orders do not change any more. */
    public boolean isFinal() {
        return getAvailableActions().isEmpty();
    }

    /** The status after an allowed action. */
    public OrderStatus after(OrderAction action) {
        if (!allows(action)) {
            throw new IllegalArgumentException(action + " is not allowed in status " + this);
        }
        return switch (action) {
            case PAY -> PAID;
            case START_ASSEMBLY -> ASSEMBLING;
            case SHIP -> SHIPPED;
            case DELIVER -> DELIVERED;
            case CANCEL -> CANCELLED;
        };
    }
}
