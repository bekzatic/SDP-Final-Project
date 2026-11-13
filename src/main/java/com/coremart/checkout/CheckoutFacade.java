package com.coremart.checkout;

import com.coremart.cart.Cart;
import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Product;
import com.coremart.order.Order;
import com.coremart.order.OrderAction;
import com.coremart.order.OrderLine;
import com.coremart.order.OrderObserver;
import com.coremart.order.PaymentMethod;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Facade: checkout and order handling in one simple interface.
 * <p>
 * Placing an order touches many parts of the store: the cart, the stock in the catalog, the
 * payment system, the order list and the notifications. Controllers call one method here
 * instead of knowing all of them and the right order of steps.
 */
@Service
public class CheckoutFacade {

    private final ProductCatalog catalog = ProductCatalog.getInstance();
    private final OrderRepository orders;
    private final PaymentService payments;
    private final List<OrderObserver> observers;

    public CheckoutFacade(OrderRepository orders, PaymentService payments, List<OrderObserver> observers) {
        this.orders = orders;
        this.payments = payments;
        this.observers = List.copyOf(observers);
    }

    /**
     * Turns the cart into an order: checks the form, takes the products from stock, creates the
     * order, subscribes the observers, takes the payment (if paying now) and empties the cart.
     */
    public Order placeOrder(Cart cart, CheckoutForm form) {
        if (cart.isEmpty()) {
            throw new CheckoutException("Корзина пуста");
        }
        List<String> errors = form.validate();
        if (!errors.isEmpty()) {
            throw new CheckoutException(errors);
        }

        reserveStock(cart.getRequiredStock());

        List<OrderLine> lines = cart.getLines().stream()
                .map(line -> OrderLine.of(line.getItem(), line.getQuantity()))
                .toList();
        Order order = new Order(orders.nextId(), form.toCustomer(), form.getDelivery(), form.getPayment(),
                lines, cart.getDiscount(), cart.getPromoCode());
        observers.forEach(order::subscribe);
        orders.save(order);
        order.place();

        if (form.getPayment() == PaymentMethod.CARD_NOW) {
            takePayment(order);
        }
        cart.clear();
        return order;
    }

    /** Pays an order that was placed with "pay later". */
    public Order pay(String orderNumber) {
        Order order = find(orderNumber);
        if (!order.getState().allows(OrderAction.PAY)) {
            order.pay(); // the state refuses with its own message
        }
        takePayment(order);
        return order;
    }

    /** Cancels an order; the money is returned if it was paid, the stock comes back (observer). */
    public Order cancel(String orderNumber) {
        Order order = find(orderNumber);
        order.cancel();
        if (order.getPaymentId() != null) {
            payments.refund(order.getPaymentId());
        }
        return order;
    }

    /** Staff action from the admin page: assemble, ship, mark received or cancel. */
    public Order advance(String orderNumber, OrderAction action) {
        if (action == null || !action.isForStaff()) {
            throw new IllegalArgumentException("Это действие недоступно сотрудникам");
        }
        if (action == OrderAction.CANCEL) {
            return cancel(orderNumber);
        }
        Order order = find(orderNumber);
        order.perform(action);
        return order;
    }

    public Optional<Order> findOrder(String orderNumber) {
        return orders.findByNumber(orderNumber);
    }

    public List<Order> allOrders() {
        return orders.findAll();
    }

    // ------------------------------------------------------------ steps

    private Order find(String orderNumber) {
        return orders.findByNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Заказ " + orderNumber + " не найден"));
    }

    /** Takes everything or nothing: if one product is missing, the ones already taken go back. */
    private void reserveStock(Map<Product, Integer> needed) {
        List<Map.Entry<Product, Integer>> taken = new ArrayList<>();
        try {
            for (Map.Entry<Product, Integer> e : needed.entrySet()) {
                catalog.decreaseStock(e.getKey().getId(), e.getValue());
                taken.add(e);
            }
        } catch (IllegalStateException | IllegalArgumentException ex) {
            taken.forEach(e -> catalog.increaseStock(e.getKey().getId(), e.getValue()));
            throw new CheckoutException(missingMessage(needed));
        }
    }

    private String missingMessage(Map<Product, Integer> needed) {
        for (Map.Entry<Product, Integer> e : needed.entrySet()) {
            int left = catalog.findById(e.getKey().getId()).map(Product::getStock).orElse(0);
            if (left < e.getValue()) {
                return "«" + e.getKey().getFullName() + "»: в наличии только " + left + " шт., измените количество в корзине";
            }
        }
        return "Часть товаров закончилась, проверьте корзину";
    }

    private void takePayment(Order order) {
        order.setPaymentId(payments.charge(order.getNumber(), order.getTotal()));
        order.pay();
    }
}
