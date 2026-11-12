package com.coremart.order;

import com.coremart.model.Product;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A placed order (State context).
 * <p>
 * The order does not check its status itself: {@link #pay()}, {@link #ship()} and the other
 * actions are passed to the current {@link OrderState}, which either moves the order on or
 * refuses.
 */
public class Order {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final long id;
    private final String number;
    private final CustomerInfo customer;
    private final DeliveryMethod delivery;
    private final PaymentMethod payment;
    private final List<OrderLine> lines;
    private final long subtotal;
    private final long discount;
    private final String promoCode;
    private final long deliveryCost;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private final List<StatusChange> history = new ArrayList<>();
    private OrderState state = new NewState();
    private String paymentId;

    public Order(long id, CustomerInfo customer, DeliveryMethod delivery, PaymentMethod payment,
                 List<OrderLine> lines, long discount, String promoCode) {
        if (customer == null || delivery == null || payment == null) {
            throw new IllegalArgumentException("Customer, delivery and payment are required");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("An order needs at least one line");
        }
        this.id = id;
        this.number = "CM-" + (1000 + id);
        this.customer = customer;
        this.delivery = delivery;
        this.payment = payment;
        this.lines = List.copyOf(lines);
        this.subtotal = this.lines.stream().mapToLong(OrderLine::getTotal).sum();
        if (discount < 0 || discount > subtotal) {
            throw new IllegalArgumentException("Discount must be between 0 and the order sum");
        }
        this.discount = discount;
        this.promoCode = promoCode;
        this.deliveryCost = delivery.costFor(subtotal - discount);
        history.add(new StatusChange(state.getName(), createdAt));
    }

    // ------------------------------------------------------------ actions (passed to the state)

    public void pay() {
        state.pay(this);
    }

    public void startAssembly() {
        state.startAssembly(this);
    }

    public void ship() {
        state.ship(this);
    }

    public void deliver() {
        state.deliver(this);
    }

    public void cancel() {
        state.cancel(this);
    }

    /** Runs an action chosen on a page, e.g. a button on the admin page. */
    public void perform(OrderAction action) {
        switch (action) {
            case PAY -> pay();
            case START_ASSEMBLY -> startAssembly();
            case SHIP -> ship();
            case DELIVER -> deliver();
            case CANCEL -> cancel();
        }
    }

    /** Called by the states only. */
    void changeState(OrderState next) {
        state = next;
        history.add(new StatusChange(next.getName(), LocalDateTime.now()));
    }

    // ------------------------------------------------------------ status

    public OrderState getState() {
        return state;
    }

    public String getStatusName() {
        return state.getName();
    }

    public List<StatusChange> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /** Buttons the customer sees on the order page. */
    public List<OrderAction> getCustomerActions() {
        return state.getAvailableActions().stream().filter(OrderAction::isForCustomer).toList();
    }

    /** Buttons the staff see on the admin page. */
    public List<OrderAction> getStaffActions() {
        return state.getAvailableActions().stream().filter(OrderAction::isForStaff).toList();
    }

    /** Id of the payment in the payment system; null while the order is not paid. */
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    // ------------------------------------------------------------ contents

    public long getId() { return id; }
    public String getNumber() { return number; }
    public CustomerInfo getCustomer() { return customer; }
    public DeliveryMethod getDelivery() { return delivery; }
    public PaymentMethod getPayment() { return payment; }
    public List<OrderLine> getLines() { return lines; }
    public String getPromoCode() { return promoCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getCreatedAtText() {
        return createdAt.format(DATE);
    }

    public int getItemCount() {
        return lines.stream().mapToInt(OrderLine::getQuantity).sum();
    }

    /** How many of each product the order takes from the warehouse. */
    public Map<Product, Integer> getRequiredStock() {
        Map<Product, Integer> needed = new LinkedHashMap<>();
        for (OrderLine line : lines) {
            for (Product p : line.getProducts()) {
                needed.merge(p, line.getQuantity(), Integer::sum);
            }
        }
        return needed;
    }

    // ------------------------------------------------------------ money

    public long getSubtotal() { return subtotal; }
    public long getDiscount() { return discount; }
    public long getDeliveryCost() { return deliveryCost; }

    public long getTotal() {
        return subtotal - discount + deliveryCost;
    }

    public String getFormattedSubtotal() { return Product.formatTenge(subtotal); }
    public String getFormattedDiscount() { return Product.formatTenge(discount); }
    public String getFormattedTotal() { return Product.formatTenge(getTotal()); }

    public String getFormattedDeliveryCost() {
        return deliveryCost == 0 ? "бесплатно" : Product.formatTenge(deliveryCost);
    }

    @Override
    public String toString() {
        return number + " (" + getStatusName() + ", " + getFormattedTotal() + ")";
    }
}
