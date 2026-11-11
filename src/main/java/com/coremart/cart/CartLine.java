package com.coremart.cart;

import com.coremart.bundle.StoreItem;
import com.coremart.model.Product;

/** One line of the cart: a store item (product, bundle or item with services) and its quantity. */
public final class CartLine {

    private final int id;
    private final StoreItem item;
    private int quantity;

    CartLine(int id, StoreItem item, int quantity) {
        this.id = id;
        this.item = item;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public StoreItem getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getTotal() {
        return item.getPrice() * quantity;
    }

    public String getFormattedTotal() {
        return Product.formatTenge(getTotal());
    }
}
