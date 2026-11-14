package com.coremart.order;

import com.coremart.model.Product;

/** How the customer gets the order. Courier delivery is free for large orders. */
public enum DeliveryMethod {

    PICKUP("Store pickup", 0),
    COURIER("City courier", 2_000);

    public static final long FREE_COURIER_FROM = 300_000;

    private final String label;
    private final long price;

    DeliveryMethod(String label, long price) {
        this.label = label;
        this.price = price;
    }

    public String getLabel() {
        return label;
    }

    public long costFor(long orderAmount) {
        return this == COURIER && orderAmount >= FREE_COURIER_FROM ? 0 : price;
    }

    public String getPriceText() {
        if (price == 0) {
            return "free";
        }
        return Product.formatTenge(price) + ", free from " + Product.formatTenge(FREE_COURIER_FROM);
    }

    public boolean needsAddress() {
        return this == COURIER;
    }
}
