package com.coremart.pricing;

import com.coremart.model.Product;

/** STRATEGY pattern - a <b>Concrete Strategy</b>: a percentage off, optionally capped. */
public class PercentageDiscount implements DiscountStrategy {

    private final int percent;
    private final long maxAmount;

    /** @param maxAmount the largest discount in tenge, or 0 for no limit */
    public PercentageDiscount(int percent, long maxAmount) {
        if (percent <= 0 || percent >= 100) {
            throw new IllegalArgumentException("Percent must be 1-99");
        }
        if (maxAmount < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
        }
        this.percent = percent;
        this.maxAmount = maxAmount;
    }

    @Override
    public String getDescription() {
        return "Discount " + percent + "%"
                + (maxAmount > 0 ? ", up to " + Product.formatTenge(maxAmount) : "");
    }

    @Override
    public long discountFor(long subtotal) {
        if (subtotal <= 0) {
            return 0;
        }
        long discount = subtotal * percent / 100;
        return maxAmount > 0 ? Math.min(discount, maxAmount) : discount;
    }
}
