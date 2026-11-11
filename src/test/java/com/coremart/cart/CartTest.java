package com.coremart.cart;

import com.coremart.addon.GiftWrapAddOn;
import com.coremart.builder.ProductBuilder;
import com.coremart.bundle.ProductBundle;
import com.coremart.bundle.ProductItem;
import com.coremart.model.Category;
import com.coremart.model.Product;
import com.coremart.pricing.NoDiscount;
import com.coremart.pricing.PercentageDiscount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartTest {

    private Cart cart;
    private Product laptop;
    private Product mouse;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        laptop = ProductBuilder.of(Category.LAPTOP).brand("Lenovo").name("IdeaPad Slim 5").price(389_000).stock(2)
                .cpu("AMD Ryzen 5 7530U").ramGb(16).storageGb(512).screenInches(15.6).weightKg(1.7).build();
        mouse = ProductBuilder.of(Category.ACCESSORY).brand("Logitech").name("M185").price(9_000).stock(25)
                .type("Мышь").build();
    }

    @Test
    void cartHoldsProductsBundlesAndDecoratedItemsAlike() {
        cart.add(new ProductItem(mouse), 2);
        cart.add(new GiftWrapAddOn(new ProductItem(mouse)), 1);
        cart.add(new ProductBundle("Для учёбы", 10).add(laptop).add(mouse), 1);

        assertEquals(3, cart.getLines().size());
        assertEquals(4, cart.getItemCount());
        assertEquals(18_000 + 12_000 + 358_200, cart.getSubtotal());
        assertEquals(cart.getSubtotal(), cart.getTotal());
        assertEquals(4, cart.getRequiredStock().get(mouse));
        assertEquals(1, cart.getRequiredStock().get(laptop));
    }

    @Test
    void quantityCanBeChangedAndLinesRemoved() {
        CartLine line = cart.add(new ProductItem(mouse), 1);
        cart.changeQuantity(line.getId(), 5);
        assertEquals(45_000, cart.getSubtotal());
        assertTrue(cart.remove(line.getId()));
        assertFalse(cart.remove(line.getId()));
        assertTrue(cart.isEmpty());
        assertThrows(IllegalArgumentException.class, () -> cart.changeQuantity(99, 1));
    }

    @Test
    void cartNeverAsksForMoreThanIsInStock() {
        cart.add(new ProductItem(laptop), 2);
        assertThrows(IllegalStateException.class, () -> cart.add(new ProductItem(laptop), 1));
        assertThrows(IllegalStateException.class,
                () -> cart.add(new ProductBundle("Комплект", 5).add(laptop).add(mouse), 1));
        CartLine line = cart.getLines().get(0);
        assertThrows(IllegalStateException.class, () -> cart.changeQuantity(line.getId(), 3));
        assertEquals(2, line.getQuantity());

        laptop.setStock(0);
        assertThrows(IllegalStateException.class, () -> new Cart().add(new ProductItem(laptop), 1));
        assertThrows(IllegalArgumentException.class, () -> cart.add(new ProductItem(mouse), 0));
        assertThrows(IllegalArgumentException.class, () -> cart.add(new ProductItem(mouse), Cart.MAX_QUANTITY + 1));
        assertThrows(IllegalArgumentException.class, () -> cart.add(null, 1));
    }

    @Test
    void promoCodeSwapsTheDiscountStrategy() {
        cart.add(new ProductItem(laptop), 1);
        assertInstanceOf(NoDiscount.class, cart.getDiscountStrategy());
        assertEquals(0, cart.getDiscount());

        assertTrue(cart.applyPromoCode("student10"));
        assertInstanceOf(PercentageDiscount.class, cart.getDiscountStrategy());
        assertEquals("STUDENT10", cart.getPromoCode());
        assertEquals(38_900, cart.getDiscount());
        assertEquals(350_100, cart.getTotal());

        assertFalse(cart.applyPromoCode("FREE"));
        assertEquals("STUDENT10", cart.getPromoCode());

        cart.removePromoCode();
        assertNull(cart.getPromoCode());
        assertEquals(389_000, cart.getTotal());
    }

    @Test
    void discountDependsOnTheCurrentSubtotal() {
        cart.applyPromoCode("CORE5000");
        CartLine line = cart.add(new ProductItem(mouse), 1);
        assertEquals(0, cart.getDiscount());
        cart.changeQuantity(line.getId(), 6);
        assertEquals(5_000, cart.getDiscount());
        assertEquals(49_000, cart.getTotal());

        cart.clear();
        assertTrue(cart.isEmpty());
        assertNull(cart.getPromoCode());
    }
}
