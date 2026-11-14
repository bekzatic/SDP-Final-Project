package com.coremart.addon;

import com.coremart.bundle.StoreItem;

/** DECORATOR pattern - a <b>Concrete Decorator</b>: gift wrapping for a fixed price. */
public class GiftWrapAddOn extends AddOnDecorator {

    public static final long PRICE = 3_000;

    public GiftWrapAddOn(StoreItem inner) {
        super(inner);
    }

    @Override
    public String getAddOnName() {
        return "Gift wrapping";
    }

    @Override
    public long getAddOnPrice() {
        return PRICE;
    }
}
