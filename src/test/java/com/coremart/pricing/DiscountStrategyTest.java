package com.coremart.pricing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscountStrategyTest {

    @Test
    void noDiscountByDefault() {
        assertEquals(0, new NoDiscount().discountFor(500_000));
    }

    @Test
    void percentageDiscountRespectsItsLimit() {
        DiscountStrategy student = new PercentageDiscount(10, 50_000);
        assertEquals(38_900, student.discountFor(389_000));
        assertEquals(50_000, student.discountFor(899_000));
        assertEquals(0, student.discountFor(0));
        assertEquals(15_000, new PercentageDiscount(15, 0).discountFor(100_000));
        assertTrue(student.getDescription().startsWith("Discount 10%, up to"));
    }

    @Test
    void fixedDiscountNeedsAMinimumSubtotal() {
        DiscountStrategy core = new FixedAmountDiscount(5_000, 50_000);
        assertEquals(0, core.discountFor(45_000));
        assertEquals(5_000, core.discountFor(50_000));
        assertEquals(3_000, new FixedAmountDiscount(5_000, 0).discountFor(3_000));
    }

    @Test
    void invalidDiscountsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PercentageDiscount(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new PercentageDiscount(100, 0));
        assertThrows(IllegalArgumentException.class, () -> new PercentageDiscount(10, -1));
        assertThrows(IllegalArgumentException.class, () -> new FixedAmountDiscount(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new FixedAmountDiscount(1_000, -5));
    }

    @Test
    void promoCodesPickTheStrategy() {
        assertInstanceOf(PercentageDiscount.class, PromoCodes.find("student10").orElseThrow());
        assertInstanceOf(FixedAmountDiscount.class, PromoCodes.find(" CORE5000 ").orElseThrow());
        assertTrue(PromoCodes.find("FREE").isEmpty());
        assertTrue(PromoCodes.find(" ").isEmpty());
        assertTrue(PromoCodes.find(null).isEmpty());
        assertEquals("SALE15", PromoCodes.normalize(" sale15"));
    }
}
