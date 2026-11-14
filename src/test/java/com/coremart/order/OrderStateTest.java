package com.coremart.order;

import com.coremart.addon.GiftWrapAddOn;
import com.coremart.builder.ProductBuilder;
import com.coremart.bundle.ProductBundle;
import com.coremart.bundle.ProductItem;
import com.coremart.model.Category;
import com.coremart.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStateTest {

    private Product laptop;
    private Product mouse;

    @BeforeEach
    void setUp() {
        laptop = ProductBuilder.of(Category.LAPTOP).brand("Lenovo").name("IdeaPad Slim 5").price(389_000).stock(5)
                .cpu("AMD Ryzen 5 7530U").ramGb(16).storageGb(512).screenInches(15.6).weightKg(1.7).build();
        mouse = ProductBuilder.of(Category.ACCESSORY).brand("Logitech").name("M185").price(9_000).stock(25)
                .type("Mouse").build();
    }

    private Order order(DeliveryMethod delivery) {
        return new Order(1, new CustomerInfo("Aigerim", "+7 701 000 00 00", "a@example.com", "10 Abai St."),
                delivery, PaymentMethod.LATER, List.of(OrderLine.of(new ProductItem(mouse), 2)), 0, null);
    }

    @Test
    void newOrderWaitsForPayment() {
        Order order = order(DeliveryMethod.PICKUP);
        assertInstanceOf(NewState.class, order.getState());
        assertEquals("CM-1001", order.getNumber());
        assertEquals("Awaiting payment", order.getStatusName());
        assertEquals(List.of(OrderAction.PAY, OrderAction.CANCEL), order.getCustomerActions());
        assertEquals(List.of(OrderAction.CANCEL), order.getStaffActions());
        assertEquals(1, order.getHistory().size());
    }

    @Test
    void fullPathFromPaymentToDelivery() {
        Order order = order(DeliveryMethod.COURIER);
        order.pay();
        assertInstanceOf(PaidState.class, order.getState());
        order.startAssembly();
        assertInstanceOf(AssemblingState.class, order.getState());
        order.ship();
        assertInstanceOf(ShippedState.class, order.getState());
        assertEquals("With courier", order.getStatusName());
        order.deliver();
        assertInstanceOf(DeliveredState.class, order.getState());
        assertTrue(order.getState().isFinal());
        assertEquals(List.of("Awaiting payment", "Paid", "Assembling", "With courier", "Delivered"),
                order.getHistory().stream().map(StatusChange::status).toList());
    }

    @Test
    void pickupOrderIsReadyInTheShop() {
        Order order = order(DeliveryMethod.PICKUP);
        order.pay();
        order.startAssembly();
        order.ship();
        assertEquals("Ready for pickup", order.getStatusName());
    }

    @Test
    void eachStateAllowsOnlyItsOwnActions() {
        Order order = order(DeliveryMethod.PICKUP);
        IllegalStateException e = assertThrows(IllegalStateException.class, order::ship);
        assertEquals("Action “Ship” is not available: the order status is “Awaiting payment”", e.getMessage());
        assertThrows(IllegalStateException.class, order::deliver);
        assertThrows(IllegalStateException.class, order::startAssembly);

        order.pay();
        assertThrows(IllegalStateException.class, order::pay);
        assertThrows(IllegalStateException.class, order::ship);
        assertEquals(2, order.getHistory().size(), "refused actions do not change the history");
    }

    @Test
    void cancelOnlyBeforeTheOrderLeavesTheWarehouse() {
        Order fresh = order(DeliveryMethod.PICKUP);
        fresh.cancel();
        assertInstanceOf(CancelledState.class, fresh.getState());

        Order paid = order(DeliveryMethod.PICKUP);
        paid.pay();
        paid.cancel();
        assertInstanceOf(CancelledState.class, paid.getState());

        Order assembling = order(DeliveryMethod.PICKUP);
        assembling.pay();
        assembling.startAssembly();
        assembling.cancel();
        assertInstanceOf(CancelledState.class, assembling.getState());

        Order shipped = order(DeliveryMethod.COURIER);
        shipped.pay();
        shipped.startAssembly();
        shipped.ship();
        assertThrows(IllegalStateException.class, shipped::cancel);

        assertThrows(IllegalStateException.class, fresh::pay);
        assertTrue(fresh.getState().isFinal());
    }

    @Test
    void performRunsTheChosenAction() {
        Order order = order(DeliveryMethod.PICKUP);
        order.perform(OrderAction.PAY);
        order.perform(OrderAction.START_ASSEMBLY);
        assertTrue(order.getState().allows(OrderAction.SHIP));
        assertFalse(order.getState().allows(OrderAction.PAY));
        assertEquals(OrderAction.START_ASSEMBLY, OrderAction.parse("start_assembly").orElseThrow());
        assertTrue(OrderAction.parse("nope").isEmpty());
    }

    @Test
    void orderCopiesLinesAndCountsMoney() {
        ProductBundle bundle = new ProductBundle("For study", 10).add(laptop).add(mouse);
        Order order = new Order(2, new CustomerInfo(" Erlan ", "+7 702 000 00 00", "e@example.com", ""),
                DeliveryMethod.COURIER, PaymentMethod.CARD_NOW,
                List.of(OrderLine.of(bundle, 1), OrderLine.of(new GiftWrapAddOn(new ProductItem(mouse)), 2)),
                5_000, "CORE5000");

        assertEquals("Erlan", order.getCustomer().name());
        assertEquals(bundle.getPrice() + 2 * 12_000, order.getSubtotal());
        assertEquals(0, order.getDeliveryCost(), "courier is free from 300 000 tenge");
        assertEquals(order.getSubtotal() - 5_000, order.getTotal());
        assertEquals(3, order.getItemCount());
        assertEquals(Map.of(laptop, 1, mouse, 3), order.getRequiredStock());
        assertEquals("+ Gift wrapping (3,000 ₸)", order.getLines().get(1).getDetails().get(0));
    }

    @Test
    void courierCostsMoneyForSmallOrders() {
        Order order = order(DeliveryMethod.COURIER);
        assertEquals(2_000, order.getDeliveryCost());
        assertEquals(18_000 + 2_000, order.getTotal());
        assertEquals("free", order(DeliveryMethod.PICKUP).getFormattedDeliveryCost());
    }

    @Test
    void invalidOrdersAreRejected() {
        CustomerInfo customer = new CustomerInfo("A", "1", "a@b.c", "");
        assertThrows(IllegalArgumentException.class,
                () -> new Order(3, customer, DeliveryMethod.PICKUP, PaymentMethod.LATER, List.of(), 0, null));
        List<OrderLine> lines = List.of(OrderLine.of(new ProductItem(mouse), 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Order(3, customer, DeliveryMethod.PICKUP, PaymentMethod.LATER, lines, 10_000, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Order(3, null, DeliveryMethod.PICKUP, PaymentMethod.LATER, lines, 0, null));
    }
}
