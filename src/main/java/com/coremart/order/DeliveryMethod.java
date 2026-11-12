package com.coremart.order;

import com.coremart.model.Product;

/** How the customer gets the order. Courier delivery is free for large orders. */
public enum DeliveryMethod {

    PICKUP("Самовывоз из магазина", 0),
    COURIER("Курьер по городу", 2_000);

    /** Courier delivery costs nothing from this order sum. */
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

    /** Text for the checkout form, e.g. "2 000 ₸, бесплатно от 300 000 ₸". */
    public String getPriceText() {
        if (price == 0) {
            return "бесплатно";
        }
        return Product.formatTenge(price) + ", бесплатно от " + Product.formatTenge(FREE_COURIER_FROM);
    }

    public boolean needsAddress() {
        return this == COURIER;
    }
}
