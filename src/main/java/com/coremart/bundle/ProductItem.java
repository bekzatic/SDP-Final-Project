package com.coremart.bundle;

import com.coremart.model.Product;
import com.coremart.visitor.StoreItemVisitor;

/**
 * VISITOR pattern - a <b>Concrete Element</b>: one catalog product as a {@link StoreItem}.
 * Price and stock are read from the product itself, so the item always shows the current values.
 */
public final class ProductItem implements StoreItem {

    private final Product product;

    public ProductItem(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product is required");
        }
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }

    @Override
    public String getTitle() {
        return product.getFullName();
    }

    @Override
    public long getPrice() {
        return product.getPrice();
    }

    @Override
    public boolean isAvailable() {
        return product.isInStock();
    }

    @Override
    public <R> R accept(StoreItemVisitor<R> visitor) {
        return visitor.visitProduct(this);
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
