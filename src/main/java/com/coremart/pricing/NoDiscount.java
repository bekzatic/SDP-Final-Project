package com.coremart.pricing;

/** STRATEGY pattern - a <b>Concrete Strategy</b>: no discount (the default). */
public class NoDiscount implements DiscountStrategy {

    @Override
    public String getDescription() {
        return "Без скидки";
    }

    @Override
    public long discountFor(long subtotal) {
        return 0;
    }
}
