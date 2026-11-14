package com.coremart.pricing;

import com.coremart.model.Product;

/** STRATEGY pattern - a <b>Concrete Strategy</b>: a fixed sum off orders above a minimum. */
public class FixedAmountDiscount implements DiscountStrategy {

    private final long amount;
    private final long minSubtotal;

    public FixedAmountDiscount(long amount, long minSubtotal) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (minSubtotal < 0) {
            throw new IllegalArgumentException("Minimum cannot be negative");
        }
        this.amount = amount;
        this.minSubtotal = minSubtotal;
    }

    @Override
    public String getDescription() {
        return "Discount " + Product.formatTenge(amount)
                + (minSubtotal > 0 ? " on orders from " + Product.formatTenge(minSubtotal) : "");
    }

    @Override
    public long discountFor(long subtotal) {
        if (subtotal <= 0 || subtotal < minSubtotal) {
            return 0;
        }
        return Math.min(amount, subtotal);
    }
}
