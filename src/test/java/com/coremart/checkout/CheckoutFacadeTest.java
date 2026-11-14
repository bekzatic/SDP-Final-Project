package com.coremart.checkout;

import com.coremart.addon.GiftWrapAddOn;
import com.coremart.bundle.ProductItem;
import com.coremart.cart.Cart;
import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Category;
import com.coremart.model.Product;
import com.coremart.notification.EmailNotifier;
import com.coremart.notification.Notification;
import com.coremart.notification.NotificationLog;
import com.coremart.notification.StockAlert;
import com.coremart.notification.StockRestorer;
import com.coremart.order.AssemblingState;
import com.coremart.order.CancelledState;
import com.coremart.order.DeliveredState;
import com.coremart.order.DeliveryMethod;
import com.coremart.order.NewState;
import com.coremart.order.Order;
import com.coremart.order.OrderAction;
import com.coremart.order.PaidState;
import com.coremart.order.PaymentMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckoutFacadeTest {

    private final ProductCatalog catalog = ProductCatalog.getInstance();
    private final Map<Long, Integer> stockBefore = new HashMap<>();

    private NotificationLog log;
    private PaymentService payments;
    private OrderRepository orders;
    private CheckoutFacade facade;
    private Cart cart;
    private Product mouse;
    private Product keyboard;

    @BeforeEach
    void setUp() {
        catalog.findAll().forEach(p -> stockBefore.put(p.getId(), p.getStock()));
        log = new NotificationLog();
        payments = new PaymentService();
        orders = new OrderRepository();
        facade = new CheckoutFacade(orders, payments,
                List.of(new EmailNotifier(log), new StockAlert(log), new StockRestorer(log)));
        cart = new Cart();
        mouse = accessory("M185");
        keyboard = accessory("MX Keys S");
    }

    /** Other tests use the same catalog, so the stock is put back after each test. */
    @AfterEach
    void restoreStock() {
        catalog.findAll().forEach(p -> p.setStock(stockBefore.get(p.getId())));
    }

    private Product accessory(String name) {
        return catalog.findByCategory(Category.ACCESSORY).stream()
                .filter(p -> p.getName().equals(name)).findFirst().orElseThrow();
    }

    private static CheckoutForm form(DeliveryMethod delivery, PaymentMethod payment) {
        return new CheckoutForm("Aigerim", "+7 701 123 45 67", "aigerim@example.com", "10 Abai St.",
                delivery, payment);
    }

    @Test
    void payNowTakesStockPaysAndEmptiesTheCart() {
        int mice = mouse.getStock();
        cart.add(new GiftWrapAddOn(new ProductItem(mouse)), 2);

        Order order = facade.placeOrder(cart, form(DeliveryMethod.COURIER, PaymentMethod.CARD_NOW));

        assertInstanceOf(PaidState.class, order.getState());
        assertEquals(mice - 2, mouse.getStock());
        assertTrue(cart.isEmpty());
        assertEquals(2 * 12_000 + 2_000, order.getTotal());
        assertNotNull(order.getPaymentId());
        assertEquals(order.getTotal(), payments.amountOf(order.getPaymentId()));
        assertEquals(order, facade.findOrder(order.getNumber()).orElseThrow());
        assertEquals(3, order.getObservers().size());
        assertEquals(2, log.forOrder(order.getNumber()).stream()
                .filter(n -> n.channel().equals(Notification.EMAIL)).count(), "placed + paid");
    }

    @Test
    void payLaterKeepsTheOrderWaiting() {
        cart.add(new ProductItem(keyboard), 1);
        Order order = facade.placeOrder(cart, form(DeliveryMethod.PICKUP, PaymentMethod.LATER));
        assertInstanceOf(NewState.class, order.getState());
        assertEquals(null, order.getPaymentId());
        assertEquals(0, order.getDeliveryCost());

        facade.pay(order.getNumber());
        assertInstanceOf(PaidState.class, order.getState());
        assertThrows(IllegalStateException.class, () -> facade.pay(order.getNumber()), "paid only once");
    }

    @Test
    void promoCodeDiscountGoesIntoTheOrder() {
        cart.add(new ProductItem(keyboard), 1);
        cart.applyPromoCode("CORE5000");
        Order order = facade.placeOrder(cart, form(DeliveryMethod.PICKUP, PaymentMethod.LATER));
        assertEquals(5_000, order.getDiscount());
        assertEquals("CORE5000", order.getPromoCode());
        assertEquals(keyboard.getPrice() - 5_000, order.getTotal());
        assertEquals(null, cart.getPromoCode(), "the cart is reset");
    }

    @Test
    void cancelReturnsStockAndMoney() {
        int mice = mouse.getStock();
        cart.add(new ProductItem(mouse), 3);
        Order order = facade.placeOrder(cart, form(DeliveryMethod.PICKUP, PaymentMethod.CARD_NOW));
        facade.advance(order.getNumber(), OrderAction.START_ASSEMBLY);

        facade.cancel(order.getNumber());

        assertInstanceOf(CancelledState.class, order.getState());
        assertEquals(mice, mouse.getStock());
        assertTrue(payments.isRefunded(order.getPaymentId()));
    }

    @Test
    void staffMoveTheOrderToTheCustomer() {
        cart.add(new ProductItem(mouse), 1);
        Order order = facade.placeOrder(cart, form(DeliveryMethod.COURIER, PaymentMethod.CARD_NOW));
        facade.advance(order.getNumber(), OrderAction.START_ASSEMBLY);
        assertInstanceOf(AssemblingState.class, order.getState());
        facade.advance(order.getNumber(), OrderAction.SHIP);
        assertThrows(IllegalStateException.class, () -> facade.cancel(order.getNumber()));
        facade.advance(order.getNumber(), OrderAction.DELIVER);
        assertInstanceOf(DeliveredState.class, order.getState());
        assertThrows(IllegalArgumentException.class, () -> facade.advance(order.getNumber(), OrderAction.PAY));
        assertThrows(IllegalArgumentException.class, () -> facade.advance("CM-999999", OrderAction.SHIP));
    }

    @Test
    void wrongFormIsRefusedAndNothingChanges() {
        int mice = mouse.getStock();
        cart.add(new ProductItem(mouse), 1);
        CheckoutForm bad = new CheckoutForm("", "123", "not-an-email", "", DeliveryMethod.COURIER,
                PaymentMethod.CARD_NOW);

        CheckoutException e = assertThrows(CheckoutException.class, () -> facade.placeOrder(cart, bad));

        assertEquals(List.of("Enter your name", "Enter a phone number, e.g. +7 701 123 45 67",
                "Enter an email for order notifications", "Enter a delivery address"), e.getErrors());
        assertEquals(mice, mouse.getStock());
        assertFalse(cart.isEmpty());
        assertEquals(0, orders.size());
    }

    @Test
    void emptyCartCannotBeOrdered() {
        CheckoutException e = assertThrows(CheckoutException.class,
                () -> facade.placeOrder(cart, form(DeliveryMethod.PICKUP, PaymentMethod.CARD_NOW)));
        assertEquals(List.of("Your cart is empty"), e.getErrors());
    }

    @Test
    void ifOneProductRanOutNothingIsTaken() {
        int mice = mouse.getStock();
        cart.add(new ProductItem(mouse), 1);
        cart.add(new ProductItem(keyboard), 2);
        catalog.decreaseStock(keyboard.getId(), keyboard.getStock() - 1); // someone else bought them

        CheckoutException e = assertThrows(CheckoutException.class,
                () -> facade.placeOrder(cart, form(DeliveryMethod.PICKUP, PaymentMethod.CARD_NOW)));

        assertTrue(e.getMessage().startsWith("“Logitech MX Keys S”: only 1 left in stock"));
        assertEquals(mice, mouse.getStock(), "the mouse taken first was put back");
        assertEquals(1, keyboard.getStock());
        assertEquals(0, orders.size());
    }

    @Test
    void lowStockAfterAnOrderAlertsTheWarehouse() {
        cart.add(new ProductItem(keyboard), keyboard.getStock() - 2);
        Order order = facade.placeOrder(cart, form(DeliveryMethod.PICKUP, PaymentMethod.LATER));
        assertEquals("Running low on “Logitech MX Keys S”: 2 left",
                log.latest(Notification.WAREHOUSE, 1).get(0).text());
        assertEquals(order.getNumber(), log.latest(Notification.WAREHOUSE, 1).get(0).orderNumber());
    }
}
