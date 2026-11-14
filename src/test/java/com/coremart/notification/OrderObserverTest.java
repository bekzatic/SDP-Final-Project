package com.coremart.notification;

import com.coremart.builder.ProductBuilder;
import com.coremart.bundle.ProductItem;
import com.coremart.catalog.ProductCatalog;
import com.coremart.model.Category;
import com.coremart.model.Product;
import com.coremart.order.CancelledState;
import com.coremart.order.CustomerInfo;
import com.coremart.order.DeliveryMethod;
import com.coremart.order.NewState;
import com.coremart.order.Order;
import com.coremart.order.OrderLine;
import com.coremart.order.OrderObserver;
import com.coremart.order.OrderState;
import com.coremart.order.PaidState;
import com.coremart.order.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderObserverTest {

    private NotificationLog log;

    @BeforeEach
    void setUp() {
        log = new NotificationLog();
    }

    private static Product accessory(int stock) {
        return ProductBuilder.of(Category.ACCESSORY).brand("Logitech").name("M185").price(9_000).stock(stock)
                .type("Mouse").build();
    }

    private static Order order(Product product, int quantity) {
        return new Order(7, new CustomerInfo("Aigerim", "+7 701 000 00 00", "a@example.com", ""),
                DeliveryMethod.PICKUP, PaymentMethod.LATER,
                List.of(OrderLine.of(new ProductItem(product), quantity)), 0, null);
    }

    /** Remembers what it was told, to check the order calls its observers. */
    private static class Recorder implements OrderObserver {
        final List<String> events = new ArrayList<>();

        @Override
        public void orderPlaced(Order order) {
            events.add("placed " + order.getNumber());
        }

        @Override
        public void statusChanged(Order order, OrderState from, OrderState to) {
            events.add(from.getCode() + " -> " + to.getCode());
        }
    }

    @Test
    void orderTellsAllSubscribedObservers() {
        Order order = order(accessory(10), 1);
        Recorder first = new Recorder();
        Recorder second = new Recorder();
        order.subscribe(first);
        order.subscribe(second);
        order.subscribe(first);

        order.place();
        order.pay();
        order.unsubscribe(second);
        order.startAssembly();

        assertEquals(List.of("placed CM-1007", "NEW -> PAID", "PAID -> ASSEMBLING"), first.events);
        assertEquals(List.of("placed CM-1007", "NEW -> PAID"), second.events);
        assertEquals(1, order.getObservers().size());
    }

    @Test
    void refusedActionNotifiesNobody() {
        Order order = order(accessory(10), 1);
        Recorder recorder = new Recorder();
        order.subscribe(recorder);
        assertThrows(IllegalStateException.class, order::deliver);
        assertTrue(recorder.events.isEmpty());
    }

    @Test
    void orderIsPlacedOnlyOnce() {
        Order order = order(accessory(10), 1);
        order.place();
        assertTrue(order.isPlaced());
        assertThrows(IllegalStateException.class, order::place);
    }

    @Test
    void emailNotifierWritesToTheCustomer() {
        Order order = order(accessory(10), 2);
        order.subscribe(new EmailNotifier(log));
        order.place();
        order.setPaymentId("PAY-1");
        order.pay();
        order.cancel();

        List<Notification> mails = log.forOrder("CM-1007");
        assertEquals(3, mails.size());
        assertTrue(mails.stream().allMatch(n -> n.channel().equals(Notification.EMAIL)
                && n.recipient().equals("a@example.com")));
        assertEquals("Order CM-1007 placed. Total: 18,000 ₸. You can pay for it in “My orders”.",
                mails.get(0).text());
        assertTrue(mails.get(1).text().startsWith("Order CM-1007: “Paid”."));
        assertTrue(mails.get(2).text().contains("The money will be returned to your card"));
    }

    @Test
    void stockAlertWarnsWhenFewItemsAreLeft() {
        StockAlert alert = new StockAlert(log);
        Order few = order(accessory(2), 1);
        few.subscribe(alert);
        few.place();
        Order many = order(accessory(10), 1);
        many.subscribe(alert);
        many.place();
        Order none = order(accessory(0), 1);
        none.subscribe(alert);
        none.place();

        List<Notification> warehouse = log.latest(Notification.WAREHOUSE, 10);
        assertEquals(2, warehouse.size());
        assertEquals("“Logitech M185” sold out after order CM-1007.", warehouse.get(0).text());
        assertEquals("Running low on “Logitech M185”: 2 left", warehouse.get(1).text());
    }

    @Test
    void cancelledOrderGoesBackToStock() {
        ProductCatalog catalog = ProductCatalog.getInstance();
        Product mouse = catalog.findByCategory(Category.ACCESSORY).stream()
                .filter(p -> p.getStock() >= 2).findFirst().orElseThrow();
        int before = mouse.getStock();
        catalog.decreaseStock(mouse.getId(), 2);

        Order order = order(mouse, 2);
        order.subscribe(new StockRestorer(log));
        order.place();
        order.pay();
        assertEquals(before - 2, mouse.getStock(), "paying does not touch the stock");
        order.cancel();

        assertInstanceOf(CancelledState.class, order.getState());
        assertEquals(before, mouse.getStock());
        assertEquals("Order CM-1007 cancelled, items returned to stock.",
                log.latest(Notification.WAREHOUSE, 1).get(0).text());
    }

    @Test
    void observersDoNotChangeTheStateFlow() {
        Order order = order(accessory(10), 1);
        order.subscribe(new EmailNotifier(log));
        order.subscribe(new StockAlert(log));
        assertInstanceOf(NewState.class, order.getState());
        order.pay();
        assertInstanceOf(PaidState.class, order.getState());
    }
}
