package com.coremart.addon;

import com.coremart.bundle.StoreItem;

/**
 * DECORATOR pattern - a <b>Concrete Decorator</b>: extended warranty.
 * <p>
 * Costs 10% of the wrapped item's price per year (rounded up to 100 ₸), but at least 5 000 ₸.
 */
public class WarrantyAddOn extends AddOnDecorator {

    public static final int MAX_YEARS = 3;
    static final long MIN_PRICE = 5_000;

    private final int years;

    public WarrantyAddOn(StoreItem inner, int years) {
        super(inner);
        if (years < 1 || years > MAX_YEARS) {
            throw new IllegalArgumentException("Warranty can be extended by 1-" + MAX_YEARS + " years");
        }
        this.years = years;
    }

    public int getYears() {
        return years;
    }

    @Override
    public String getAddOnName() {
        return "Расширенная гарантия +" + years + (years == 1 ? " год" : " года");
    }

    @Override
    public long getAddOnPrice() {
        long price = inner.getPrice() * years / 10;
        long rounded = (price + 99) / 100 * 100;
        return Math.max(MIN_PRICE, rounded);
    }
}
